package uk.gov.example.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .param("full-name", "Jane Doe"))
        .andExpect(redirectedUrl("/session-expired"));

    mockMvc.perform(get("/session-expired").cookie(session)).andExpect(status().isOk());

    String csrf = csrf(mockMvc.perform(get("/name").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/name")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("full-name", ""))
        .andExpect(redirectedUrl("/name"));
    mockMvc
        .perform(get("/name").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("problem")));

    completeRequired(session);

    csrf =
        csrf(
            mockMvc
                .perform(get("/name").param("return", "check-answers").cookie(session))
                .andReturn());
    mockMvc
        .perform(
            post("/name")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("full-name", "A")
                .param("returnTo", "check-answers"))
        .andExpect(redirectedUrl("/name?return=check-answers"));

    csrf = csrf(mockMvc.perform(get("/name").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/name")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("full-name", "Jane Doe")
                .param("returnTo", "check-answers"))
        .andExpect(redirectedUrl("/check-answers"));
  }

  @Test
  void checkAnswersGuardsAndConfirmation() throws Exception {
    Cookie session = sessionCookie(mockMvc.perform(get("/")).andReturn());

    mockMvc
        .perform(get("/check-answers").cookie(session))
        .andExpect(redirectedUrl("/licence-length"));
    mockMvc.perform(get("/confirmation").cookie(session)).andExpect(redirectedUrl("/"));

    completeRequired(session);

    String csrf = csrf(mockMvc.perform(get("/check-answers").cookie(session)).andReturn());
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
  }

  @Test
  void incompleteCheckAnswersPostRedirects() throws Exception {
    Cookie session = sessionCookie(mockMvc.perform(get("/")).andReturn());
    String csrf = csrf(mockMvc.perform(get("/licence-length").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/licence-length")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("licence-length", "1-day"))
        .andExpect(status().is3xxRedirection());
    csrf = csrf(mockMvc.perform(get("/name").cookie(session)).andReturn());
    mockMvc
        .perform(
            post("/check-answers")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf))
        .andExpect(redirectedUrl("/name"));
  }

  private void completeRequired(Cookie session) throws Exception {
    postStep(session, "/licence-length", "licence-length", "1-day");
    postStep(session, "/name", "full-name", "Jane Doe");
    postStep(
        session,
        "/date-of-birth",
        "date-of-birth-day",
        "1",
        "date-of-birth-month",
        "1",
        "date-of-birth-year",
        "2000");
    postStep(session, "/where-you-will-fish", "country", "England");
    postStep(session, "/email", "email", "jane@example.com");
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
}
