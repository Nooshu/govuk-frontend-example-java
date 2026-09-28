package uk.gov.example.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.demos-enabled=true")
class JourneyCoverageTest {

  private static final Pattern CSRF = Pattern.compile("name=\"csrf\"\\s+value=\"([^\"]+)\"");

  @Autowired private MockMvc mockMvc;

  @Test
  void validationErrorsCsrfReturnToAndSessionExpired() throws Exception {
    Cookie session = sessionCookie(mockMvc.perform(get("/")).andReturn());

    mockMvc
        .perform(
            post("/name")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", "bad")
                .param("first-name", "Jane")
                .param("last-name", "Doe"))
        .andExpect(redirectedUrl("/session-expired"));

    mockMvc.perform(get("/session-expired").cookie(session)).andExpect(status().isOk());

    String csrf = csrf(mockMvc.perform(get("/name").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/name")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("first-name", "")
                .param("last-name", ""))
        .andExpect(redirectedUrl("/name"));
    mockMvc
        .perform(get("/name").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("problem")));

    completeRequired(session);

    csrf = csrf(mockMvc.perform(get("/name").param("return", "check-answers").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/name")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("first-name", "")
                .param("last-name", "Doe")
                .param("returnTo", "check-answers"))
        .andExpect(redirectedUrl("/name?return=check-answers"));

    csrf = csrf(mockMvc.perform(get("/name").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/name")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("first-name", "Jane")
                .param("last-name", "Doe")
                .param("returnTo", "check-answers"))
        .andExpect(redirectedUrl("/check-answers"));
  }

  @Test
  void evidenceUploadCheckAnswersGuardsAndConfirmation() throws Exception {
    Cookie session = sessionCookie(mockMvc.perform(get("/")).andReturn());

    mockMvc.perform(get("/check-answers").cookie(session)).andExpect(redirectedUrl("/name"));
    mockMvc.perform(get("/confirmation").cookie(session)).andExpect(redirectedUrl("/task-list"));

    completeRequired(session);

    String csrf = csrf(mockMvc.perform(get("/evidence").cookie(session)).andReturn());
    MockMultipartFile file =
        new MockMultipartFile("evidence", "proof.pdf", "application/pdf", "pdf".getBytes());
    mockMvc
        .perform(
            multipart("/evidence")
                .file(file)
                .cookie(session)
                .param("csrf", csrf))
        .andExpect(status().is3xxRedirection());
    mockMvc
        .perform(get("/evidence").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Current file")));

    csrf = csrf(mockMvc.perform(get("/check-answers").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/check-answers")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", "bad"))
        .andExpect(redirectedUrl("/session-expired"));

    mockMvc
        .perform(
            post("/check-answers")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf))
        .andExpect(redirectedUrl("/confirmation"));

    mockMvc.perform(get("/check-answers").cookie(session)).andExpect(redirectedUrl("/confirmation"));
    mockMvc
        .perform(
            post("/check-answers")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf))
        .andExpect(redirectedUrl("/confirmation"));
    mockMvc.perform(get("/confirmation").cookie(session)).andExpect(status().isOk());
    mockMvc.perform(get("/task-list").cookie(session)).andExpect(status().isOk());
  }

  @Test
  void incompleteCheckAnswersPostRedirects() throws Exception {
    Cookie session = sessionCookie(mockMvc.perform(get("/")).andReturn());
    String csrf =
        csrf(mockMvc.perform(get("/name").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/name")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("first-name", "Jane")
                .param("last-name", "Doe"))
        .andExpect(status().is3xxRedirection());
    csrf = "unused";
    // Force a CSRF from a page that exists; check-answers GET will redirect before form.
    // Submit check-answers with a valid session CSRF after rotating via another GET.
    csrf = csrf(mockMvc.perform(get("/email").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/check-answers")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf))
        .andExpect(redirectedUrl("/date-of-birth"));
  }

  private void completeRequired(Cookie session) throws Exception {
    postStep(session, "/name", "first-name", "Jane", "last-name", "Doe");
    postStep(
        session,
        "/date-of-birth",
        "date-of-birth-day",
        "1",
        "date-of-birth-month",
        "1",
        "date-of-birth-year",
        "2000");
    postStep(session, "/email", "email", "jane@example.com");
    postStep(session, "/contact-preference", "contact-by", "email");
    postStep(session, "/where-you-will-fish", "regions", "north-west");
    postStep(session, "/licence-length", "licence-length", "1-day");
    postStep(session, "/start-month", "start-month", currentStartMonth());
    postStep(
        session,
        "/address",
        "address-line-1",
        "1 High Street",
        "town",
        "London",
        "postcode",
        "SW1A 1AA");
    postStep(session, "/evidence");
    postStep(session, "/additional-details", "additional-details", "");
    postStep(
        session,
        "/create-a-password",
        "password",
        "password1",
        "password-confirm",
        "password1");
  }

  private void postStep(Cookie session, String path, String... fields) throws Exception {
    String csrfToken = csrf(mockMvc.perform(get(path).cookie(session)).andReturn());
    MockHttpServletRequestBuilder request =
        post(path)
            .cookie(session)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("csrf", csrfToken);
    for (int i = 0; i + 1 < fields.length; i += 2) {
      request.param(fields[i], fields[i + 1]);
    }
    mockMvc.perform(request).andExpect(status().is3xxRedirection());
  }

  private static String csrf(MvcResult result) throws Exception {
    Matcher matcher = CSRF.matcher(result.getResponse().getContentAsString());
    assertThat(matcher.find()).isTrue();
    return matcher.group(1);
  }

  private static Cookie sessionCookie(MvcResult result) {
    Cookie cookie = result.getResponse().getCookie(SessionFilter.COOKIE_NAME);
    assertThat(cookie).isNotNull();
    return cookie;
  }

  private static String currentStartMonth() {
    java.time.LocalDate now = java.time.LocalDate.now(java.time.ZoneOffset.UTC);
    return String.format("%04d-%02d", now.getYear(), now.getMonthValue());
  }
}
