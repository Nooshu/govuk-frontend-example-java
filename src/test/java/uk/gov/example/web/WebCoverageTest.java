package uk.gov.example.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import uk.gov.example.baseline.Policy;
import uk.gov.example.config.AppProperties;
import uk.gov.example.session.InMemorySessionStore;
import uk.gov.example.session.SessionData;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.demos-enabled=true")
class WebCoverageTest {

  private static final Pattern CSRF = Pattern.compile("name=\"csrf\"\\s+value=\"([^\"]+)\"");

  @Autowired private MockMvc mockMvc;
  @Autowired private AssetRegistry assets;
  @Autowired private GovukViewHelper govuk;
  @Autowired private PageChrome chrome;
  @Autowired private Policy policy;
  @Autowired private AppProperties properties;
  @Autowired private InMemorySessionStore store;

  @Test
  void govukViewHelperBranches() {
    assertThat(govuk.render("tag", (Map<String, Object>) null).value()).contains("govuk-tag");
    assertThat(
            govuk
                .render(
                    "notification-banner",
                    Map.of(
                        "titleText",
                        "Title",
                        "html",
                        Map.of("ignored", "x"),
                        "list",
                        List.of(Map.of("text", "a"))))
                .value())
        .isNotBlank();
    assertThatThrownBy(() -> GovukViewHelper.map("only-one"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void assetRegistryBranches(@TempDir Path temp) throws Exception {
    Files.writeString(temp.resolve("plain.txt"), "hello");
    Files.writeString(temp.resolve("app.css"), "body{}");
    Files.writeString(temp.resolve("app.js"), "1");
    Files.writeString(temp.resolve("mod.mjs"), "1");
    Files.writeString(temp.resolve("f.woff2"), "1");
    Files.writeString(temp.resolve("f.woff"), "1");
    Files.writeString(temp.resolve("i.svg"), "<svg/>");
    Files.writeString(temp.resolve("i.png"), "1");
    Files.writeString(temp.resolve("i.ico"), "1");
    Files.writeString(temp.resolve("d.json"), "{}");
    AssetRegistry registry =
        new AssetRegistry(
            "/assets/a.css",
            "/assets/a.mjs",
            temp,
            Map.of("/assets/a.css", new AssetRegistry.Asset(new byte[] {1}, "text/css", true)));
    assertThat(registry.resolve("/assets/a.css")).isPresent();
    assertThat(registry.resolve("/nope")).isEmpty();
    assertThat(registry.resolve("/assets/")).isEmpty();
    assertThat(registry.resolve("/assets/../plain.txt")).isEmpty();
    assertThat(registry.resolve("/assets//plain.txt")).isEmpty();
    assertThat(registry.resolve("/assets/plain.txt")).isPresent();
    assertThat(registry.resolve("/assets/app.css").orElseThrow().contentType()).contains("text/css");
    assertThat(registry.resolve("/assets/app.js").orElseThrow().contentType()).contains("javascript");
    assertThat(registry.resolve("/assets/mod.mjs").orElseThrow().contentType()).contains("javascript");
    assertThat(registry.resolve("/assets/f.woff2").orElseThrow().contentType()).contains("woff2");
    assertThat(registry.resolve("/assets/f.woff").orElseThrow().contentType()).contains("woff");
    assertThat(registry.resolve("/assets/i.svg").orElseThrow().contentType()).contains("svg");
    assertThat(registry.resolve("/assets/i.png").orElseThrow().contentType()).contains("png");
    assertThat(registry.resolve("/assets/i.ico").orElseThrow().contentType()).contains("icon");
    assertThat(registry.resolve("/assets/d.json").orElseThrow().contentType()).contains("json");
    assertThat(registry.resolve("/assets/missing.nope")).isEmpty();

    Path dir = temp.resolve("subdir");
    Files.createDirectory(dir);
    assertThat(registry.resolve("/assets/subdir")).isEmpty();
  }

  @Test
  void pageChromeWelshErrorsAndCookieBanners() throws Exception {
    SessionData session = new SessionData();
    Model model = new ConcurrentModel();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/help");
    request.setQueryString("x=1");
    chrome.apply(model, request, session, "Help", true, "cy", true, null);
    assertThat(model.getAttribute("pageTitle").toString()).startsWith("Error:");
    assertThat(model.getAttribute("htmlLang")).isEqualTo("cy");
    assertThat(model.getAttribute("cookieBannerHtml")).isNotNull();

    session.setCookieBannerConfirm("accept");
    chrome.apply(model, request, session, "Help", false, "en", false, "extra");
    assertThat(model.getAttribute("cookieBannerHtml").toString())
        .contains("accepted analytics cookies");

    session.setCookieBannerConfirm("reject");
    chrome.apply(model, request, session, "Help", false, "en", false, "");
    assertThat(model.getAttribute("cookieBannerHtml").toString())
        .contains("rejected analytics cookies");

    session.setCookieBannerConfirm(null);
    session.setCookieAnalytics("yes");
    chrome.apply(model, request, session, "Help", false, "en", false, "");
    assertThat(model.getAttribute("cookieBannerHtml")).isNull();
  }

  @Test
  void webSessionsHelpersAndSessionFilterHostCookie() throws Exception {
    assertThatThrownBy(() -> WebSessions.require(new MockHttpServletRequest()))
        .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    MockHttpServletRequest request = new MockHttpServletRequest();
    assertThat(WebSessions.id(request)).isEmpty();
    request.setAttribute(SessionFilter.ATTR_SESSION_ID, "abc");
    assertThat(WebSessions.id(request)).isEqualTo("abc");

    SessionData session = new SessionData();
    assertThat(WebSessions.csrfOk(session, null)).isFalse();
    assertThat(WebSessions.csrfOk(session, "")).isFalse();
    session.rotateCsrf();
    // empty csrf via reflection-ish: rotate then clear by constructing new and setting via field
    SessionData emptyCsrf = new SessionData();
    var field = SessionData.class.getDeclaredField("csrfToken");
    field.setAccessible(true);
    field.set(emptyCsrf, "");
    assertThat(WebSessions.csrfOk(emptyCsrf, "x")).isFalse();
    field.set(emptyCsrf, null);
    assertThat(WebSessions.csrfOk(emptyCsrf, "x")).isFalse();

    assertThat(WebSessions.safeReturn(null)).isEqualTo("/");
    assertThat(WebSessions.safeReturn("https://evil")).isEqualTo("/");
    assertThat(WebSessions.safeReturn("/ok\\bad")).isEqualTo("/");
    assertThat(WebSessions.safeReturn("/ok\r")).isEqualTo("/");
    assertThat(WebSessions.safeReturn("/ok\n")).isEqualTo("/");
    assertThat(WebSessions.safeReturn("relative")).isEqualTo("/");
    assertThat(WebSessions.safeReturn("/path://still-bad")).isEqualTo("/");

    SessionFilter filter = new SessionFilter(store, policy);
    MockHttpServletRequest secure = new MockHttpServletRequest("GET", "/help");
    secure.setSecure(true);
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(
        secure,
        response,
        (req, res) -> {
          /* no-op */
        });
    assertThat(response.getHeader("Set-Cookie")).contains("__Host-session");

    // stale cookie creates a new session
    MockHttpServletRequest stale = new MockHttpServletRequest("GET", "/help");
    stale.setCookies(new Cookie(SessionFilter.COOKIE_NAME, "missing-id"));
    MockHttpServletResponse staleResponse = new MockHttpServletResponse();
    filter.doFilter(stale, staleResponse, (req, res) -> {});
    assertThat(staleResponse.getHeader("Set-Cookie")).contains(SessionFilter.COOKIE_NAME);

    // null cookie value
    MockHttpServletRequest nullValue = new MockHttpServletRequest("GET", "/help");
    Cookie nullCookie = new Cookie(SessionFilter.COOKIE_NAME, null);
    nullValue.setCookies(nullCookie);
    filter.doFilter(nullValue, new MockHttpServletResponse(), (req, res) -> {});
  }

  @Test
  void supportCookieFailuresAndRejectChoice() throws Exception {
    Cookie session = mockMvc.perform(get("/")).andReturn().getResponse().getCookie(SessionFilter.COOKIE_NAME);
    mockMvc
        .perform(
            post("/cookies")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", "bad")
                .param("analytics", "yes"))
        .andExpect(redirectedUrl("/session-expired"));

    String csrf = csrf(mockMvc.perform(get("/cookies").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/cookies")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("analytics", "maybe"))
        .andExpect(redirectedUrl("/cookies"));
    mockMvc.perform(get("/cookies").cookie(session)).andExpect(status().isOk());

    csrf = csrf(mockMvc.perform(get("/cookies").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/cookie-choices")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", "bad")
                .param("cookies", "accept"))
        .andExpect(redirectedUrl("/session-expired"));

    csrf = csrf(mockMvc.perform(get("/cookies").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/cookie-choices")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("cookies", "reject")
                .param("returnPath", "/about"))
        .andExpect(redirectedUrl("/about"));
    mockMvc.perform(get("/about").cookie(session)).andExpect(status().isOk());

    csrf = csrf(mockMvc.perform(get("/cookies").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/cookie-choices")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("cookies", "unknown")
                .param("returnPath", "/help"))
        .andExpect(redirectedUrl("/help"));

    mockMvc.perform(get("/updates").cookie(session)).andExpect(status().isOk());
  }

  @Test
  void componentsUnknownFixtureAndCatalogueTitle() throws Exception {
    mockMvc.perform(get("/components/not-a-real-component")).andExpect(status().isNotFound());
    mockMvc
        .perform(get("/components/button").param("fixture", "does-not-exist"))
        .andExpect(status().isNotFound());
    mockMvc
        .perform(get("/components/button/fixture").param("fixture", "does-not-exist"))
        .andExpect(status().isNotFound());
    mockMvc.perform(get("/components/missing/fixture")).andExpect(status().isNotFound());
    mockMvc
        .perform(get("/components/button").param("fixture", "default"))
        .andExpect(status().isOk());

    assertThat(ComponentCatalogue.describe("weird--name").title()).contains("Weird");
    assertThat(properties.demosEnabled()).isTrue();
  }

  @Test
  void assetControllerLeadingSlashPath() throws Exception {
    mockMvc.perform(get(assets.stylesheetHref())).andExpect(status().isOk());
    // favicon already covered; hit another content type via GOV.UK assets if present
    mockMvc.perform(get("/assets/images/favicon.ico")).andExpect(status().isOk());
  }

  private static String csrf(MvcResult result) throws Exception {
    Matcher matcher = CSRF.matcher(result.getResponse().getContentAsString());
    assertThat(matcher.find()).isTrue();
    return matcher.group(1);
  }
}
