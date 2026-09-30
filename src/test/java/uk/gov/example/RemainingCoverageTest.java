package uk.gov.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
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
import org.springframework.web.server.ResponseStatusException;
import uk.gov.example.baseline.HeaderOptions;
import uk.gov.example.baseline.Policy;
import uk.gov.example.baseline.PreloadLink;
import uk.gov.example.baseline.ResponseHeaders;
import uk.gov.example.baseline.ResponseKind;
import uk.gov.example.config.AppConfig;
import uk.gov.example.config.AppProperties;
import uk.gov.example.govuk.Fixtures;
import uk.gov.example.govuk.Params;
import uk.gov.example.service.Answers;
import uk.gov.example.service.LicenceApplication;
import uk.gov.example.service.Validation;
import uk.gov.example.session.InMemorySessionStore;
import uk.gov.example.session.SessionData;
import uk.gov.example.web.AssetController;
import uk.gov.example.web.AssetRegistry;
import uk.gov.example.web.ComponentsController;
import uk.gov.example.web.PageChrome;
import uk.gov.example.web.SessionFilter;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.demos-enabled=true")
class RemainingCoverageTest {

  private static final Pattern CSRF = Pattern.compile("name=\"csrf\"\\s+value=\"([^\"]+)\"");

  @Autowired private MockMvc mockMvc;
  @Autowired private PageChrome chrome;
  @Autowired private Policy policy;
  @Autowired private AssetRegistry assets;
  @Autowired private InMemorySessionStore store;

  @Test
  void policyPermissionsAndSensitiveCookieCache() {
    assertThat(policy.permissionsPolicy()).isNotEmpty();

    Map<String, String> headers = new LinkedHashMap<>();
    ResponseHeaders.applyHeaders(
        policy,
        headers,
        new HeaderOptions(ResponseKind.SENSITIVE_DOCUMENT, false, true));
    assertThat(headers.get("Cache-Control")).isEqualTo("no-store");

    headers.clear();
    ResponseHeaders.applyHeaders(
        policy,
        headers,
        new HeaderOptions(
            ResponseKind.DOCUMENT,
            false,
            false,
            null,
            List.of(new PreloadLink("/assets/a.css", "style", ""))));
    assertThat(headers.get("Link")).doesNotContain("type=");
  }

  @Test
  void policyNewlineContentTypeAndMissingDirectives() throws Exception {
    assertThatThrownBy(
            () ->
                ResponseHeaders.applyHeaders(
                    policy,
                    new LinkedHashMap<>(),
                    new HeaderOptions(
                        ResponseKind.DOWNLOAD, false, false, "text/plain\n", List.of())))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
                    "cacheControl":{"document":"no-cache"},\
                    "csp":{}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("csp.directives");

    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
                    "cacheControl":{"document":"no-cache"},\
                    "csp":{"directives":{"default-src":null}}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
                    "cacheControl":{"document":"no-cache"},\
                    "csp":{"directives":null}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledSnippet":1,"jsEnabledScriptHash":"h",\
                    "cacheControl":{"document":"no-cache"},\
                    "csp":{"directives":{"default-src":["'self'"]}}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class);

    Policy emptyType =
        Policy.parse(
            """
            {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
            "cacheControl":{"document":"no-cache","static-asset":"no-cache",\
            "fingerprinted-asset":"public","sensitive-document":"no-store",\
            "download":"private","sensitive-download":"no-store"},\
            "contentTypes":{"static-asset":""},\
            "csp":{"directives":{"default-src":["'self'"]}}}\
            """
                .getBytes(StandardCharsets.UTF_8));
    Map<String, String> emptyTypeHeaders = new LinkedHashMap<>();
    ResponseHeaders.applyHeaders(
        emptyType, emptyTypeHeaders, new HeaderOptions(ResponseKind.STATIC_ASSET, false));
    assertThat(emptyTypeHeaders).doesNotContainKey("Content-Type");

    // Force cookie defaults path to null so setCookie's null-path branch runs.
    Policy forCookie = Policy.load(Path.of("baseline", "policy.json"));
    var cookieField = Policy.class.getDeclaredField("cookie");
    cookieField.setAccessible(true);
    cookieField.set(forCookie, new Policy.CookieSettings("Lax", true, true, null));
    assertThat(forCookie.setCookie("ok", "v", Policy.CookieOptions.defaults())).contains("Path=/");
  }

  @Test
  void appConfigMissingStylesheet(@TempDir Path temp) {
    AppProperties props =
        new AppProperties(
            false,
            temp.toString(),
            temp.toString(),
            temp.resolve("missing.js").toString(),
            temp.resolve("missing.css").toString(),
            "baseline/policy.json",
            "6.5.1");
    AppConfig config = new AppConfig(props);
    assertThatThrownBy(config::assetRegistry).isInstanceOf(Exception.class);
  }

  @Test
  void assetRegistrySymlinkAndUnreadable(@TempDir Path temp) throws Exception {
    Path outside = Files.createTempFile("outside", ".txt");
    Files.writeString(outside, "secret");
    Path link = temp.resolve("link.txt");
    try {
      Files.createSymbolicLink(link, outside);
    } catch (UnsupportedOperationException | java.nio.file.FileSystemException e) {
      // skip symlink case on restricted FS
      Files.writeString(link, "x");
    }
    Path locked = temp.resolve("locked.bin");
    Files.writeString(locked, "data");
    locked.toFile().setReadable(false);

    AssetRegistry registry = new AssetRegistry("/a", "/b", temp, Map.of());
    registry.resolve("/assets/link.txt");
    assertThat(registry.resolve("/assets/locked.bin")).isEmpty();
    locked.toFile().setReadable(true);
  }

  @Test
  void sessionFilterPrefersHostCookie() throws Exception {
    String id = store.create();
    SessionFilter filter = new SessionFilter(store, policy);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/help");
    request.setSecure(true);
    request.setCookies(new Cookie(SessionFilter.HOST_COOKIE_NAME, id));
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, (req, res) -> {});
    assertThat(request.getAttribute(SessionFilter.ATTR_SESSION_ID)).isEqualTo(id);
  }

  @Test
  void journeyCountryValidationRejectsEmpty() throws Exception {
    Cookie session = mockMvc.perform(get("/")).andReturn().getResponse().getCookie(SessionFilter.COOKIE_NAME);
    String csrf = csrf(mockMvc.perform(get("/where-you-will-fish").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/where-you-will-fish")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf))
        .andExpect(redirectedUrl("/where-you-will-fish"));

    csrf = csrf(mockMvc.perform(get("/where-you-will-fish").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/where-you-will-fish")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("country", "England"))
        .andExpect(redirectedUrl("/email"));
  }

  @Test
  void updatesPageOne() throws Exception {
    Cookie session = mockMvc.perform(get("/")).andReturn().getResponse().getCookie(SessionFilter.COOKIE_NAME);
    mockMvc.perform(get("/updates").param("page", "1").cookie(session)).andExpect(status().isOk());
  }

  @Test
  void pageChromeDemosOffAndUnsafeReturnPaths() {
    AppProperties demosOff =
        new AppProperties(
            false,
            "node_modules/govuk-frontend/dist/govuk/components",
            "node_modules/govuk-frontend/dist/govuk/assets",
            "node_modules/govuk-frontend/dist/govuk/govuk-frontend.min.js",
            "dist/stylesheets/application.css",
            "baseline/policy.json",
            "6.5.1");
    PageChrome local = new PageChrome(demosOff, assets, policy);
    SessionData session = new SessionData();
    ConcurrentModel model = new ConcurrentModel();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "//evil");
    local.apply(model, request, session, "Home", false, "en", false, "");
    assertThat(model.getAttribute("returnPath")).isEqualTo("/");
    assertThat(model.getAttribute("footerHtml").toString()).doesNotContain("/components");

    request = new MockHttpServletRequest("GET", "/ok");
    request.setQueryString("");
    local.apply(model, request, session, "Home", false, "en", false, "");

    for (String uri : List.of("/has://x", "/has\\x", "/has\rx", "/has\nx", "relative")) {
      request = new MockHttpServletRequest("GET", uri.startsWith("/") ? uri : "/" + uri);
      // force URI via setRequestURI
      request.setRequestURI(uri);
      local.apply(model, request, session, "Home", false, "en", false, "");
      assertThat(model.getAttribute("returnPath")).isEqualTo("/");
    }
  }

  @Test
  void assetControllerLeadingSlash() {
    AssetController controller = new AssetController(assets, policy);
    assertThat(controller.asset("images/favicon.ico").getStatusCode().value()).isIn(200, 404);
    assertThat(controller.asset("/images/favicon.ico").getStatusCode().value()).isIn(200, 404);
  }

  @Test
  void componentsControllerIoAndSelectFixture(@TempDir Path temp) throws Exception {
    Path file = temp.resolve("not-a-dir");
    Files.writeString(file, "x");
    AppProperties badRoot =
        new AppProperties(true, file.toString(), "a", "b", "c", "baseline/policy.json", "6.5.1");
    ComponentsController controller = new ComponentsController(chrome, badRoot);
    MockHttpServletRequest request = new MockHttpServletRequest();
    SessionData session = new SessionData();
    request.setAttribute(SessionFilter.ATTR_SESSION, session);
    ConcurrentModel model = new ConcurrentModel();
    assertThatThrownBy(() -> controller.catalogue(request, model))
        .isInstanceOf(ResponseStatusException.class);

    Path root = temp.resolve("components");
    Path button = root.resolve("button");
    Files.createDirectories(button);
    Files.writeString(
        button.resolve("fixtures.json"),
        """
        {"component":"button","fixtures":[\
        {"name":"hidden-only","hidden":true,"options":{"text":"Continue"},"html":"<button>wrong</button>"},\
        {"name":"with-desc","description":"A fixture with description","options":{"text":"Continue"},"html":"<button>wrong</button>"},\
        {"name":"blank-desc","description":"","options":{"text":"Continue"},"html":"<button>wrong</button>"}\
        ]}\
        """);
    AppProperties custom =
        new AppProperties(true, root.toString(), "a", "b", "c", "baseline/policy.json", "6.5.1");
    ComponentsController customController = new ComponentsController(chrome, custom);
    // parity fails → Important banner; blank description → null descriptionHtml
    customController.component(request, model, "button", "blank-desc");
    assertThat(model.getAttribute("parityHtml").toString()).contains("Important");
    assertThat(model.getAttribute("descriptionHtml")).isNull();
    customController.component(request, model, "button", "with-desc");
    assertThat(model.getAttribute("descriptionHtml")).isNotNull();

    assertThat(customController.fixtureFragment("button", "blank-desc").getStatusCode().value())
        .isEqualTo(200);
    assertThat(customController.fixtureFragment("button", "nope").getStatusCode().value())
        .isEqualTo(404);

    // all-hidden selection + empty fixtures via reflection on selectFixture
    Method select =
        ComponentsController.class.getDeclaredMethod("selectFixture", List.class, String.class);
    select.setAccessible(true);
    Fixtures.Fixture hidden =
        new Fixtures.Fixture("h", new Params(), true, null, "<x/>");
    assertThat(select.invoke(null, List.of(hidden), null)).isEqualTo(hidden);
    assertThat(select.invoke(null, List.of(), null)).isNull();
    assertThat(select.invoke(null, List.of(hidden), "")).isEqualTo(hidden);
    assertThat(select.invoke(null, List.of(hidden), "missing")).isNull();

    // component IOException path when fixtures root is a file
    assertThatThrownBy(() -> controller.component(request, model, "button", "default"))
        .isInstanceOf(ResponseStatusException.class);
    assertThatThrownBy(() -> controller.fixtureFragment("button", "default"))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void validationDateEdgesAndLicenceValues() {
    LocalDate now = LocalDate.of(2026, 9, 28);
    assertThat(Validation.validateDateOfBirth("1", "ab", "2000", now)).isNotEmpty();
    assertThat(Validation.validateDateOfBirth("1", "10", "2013", now)).isNotEmpty();
    assertThat(Validation.validateLicenceLength("8-days")).isEmpty();
    assertThat(Validation.validateCountry("France")).isNotEmpty();
  }

  @Test
  void answersNullBlankAndRawDateParts() throws Exception {
    Method row =
        Answers.class.getDeclaredMethod(
            "row", String.class, String.class, String.class, String.class);
    row.setAccessible(true);
    Params withNull = (Params) row.invoke(null, "Key", null, "/x", "hidden");
    assertThat(withNull.get("value")).isNotNull();

    LicenceApplication app = new LicenceApplication();
    app.setDay("1");
    app.setMonth("0");
    app.setYear("2000");
    Params dobValue = (Params) Answers.summaryRows(app).get(2).get("value");
    assertThat(dobValue.get("text")).isEqualTo("1 0 2000");
    app.setDay("");
    app.setMonth("1");
    app.setYear("2000");
    Params blankDay = (Params) Answers.summaryRows(app).get(2).get("value");
    assertThat(blankDay.get("text")).isEqualTo("Not provided");
    app.setDay("1");
    app.setMonth("");
    app.setYear("2000");
    Params blankMonth = (Params) Answers.summaryRows(app).get(2).get("value");
    assertThat(blankMonth.get("text")).isEqualTo("Not provided");
    app.setDay("1");
    app.setMonth("1");
    app.setYear("");
    Params blankYear = (Params) Answers.summaryRows(app).get(2).get("value");
    assertThat(blankYear.get("text")).isEqualTo("Not provided");
    Params blankValue = (Params) row.invoke(null, "Key", "   ", "/x", "hidden");
    assertThat(((Params) blankValue.get("value")).get("text")).isEqualTo("Not provided");
  }

  private static String csrf(MvcResult result) throws Exception {
    Matcher matcher = CSRF.matcher(result.getResponse().getContentAsString());
    assertThat(matcher.find()).isTrue();
    return matcher.group(1);
  }
}
