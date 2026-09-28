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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.demos-enabled=true")
class JourneyHappyPathTest {

  private static final Pattern CSRF =
      Pattern.compile("name=\"csrf\"\\s+value=\"([^\"]+)\"");

  @Autowired private MockMvc mockMvc;

  @Test
  void startThroughConfirmationSkippingOptional() throws Exception {
    MvcResult start = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();
    Cookie sessionCookie = sessionCookie(start.getResponse());
    assertThat(start.getResponse().getContentAsString())
        .contains("This is a live demo. It is not a real government service.");

    mockMvc
        .perform(get("/task-list").cookie(sessionCookie))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Your application")));

    postStep(sessionCookie, "/name", "first-name", "Jane", "last-name", "Doe");
    postStep(
        sessionCookie,
        "/date-of-birth",
        "date-of-birth-day",
        "1",
        "date-of-birth-month",
        "1",
        "date-of-birth-year",
        "2000");
    postStep(sessionCookie, "/email", "email", "jane@example.com");
    postStep(sessionCookie, "/contact-preference", "contact-by", "email");
    postStep(sessionCookie, "/where-you-will-fish", "regions", "north-west");
    postStep(sessionCookie, "/licence-length", "licence-length", "1-day");
    postStep(sessionCookie, "/start-month", "start-month", currentStartMonth());
    postStep(
        sessionCookie,
        "/address",
        "address-line-1",
        "1 High Street",
        "town",
        "London",
        "postcode",
        "SW1A 1AA");
    postStep(sessionCookie, "/evidence");
    postStep(sessionCookie, "/additional-details", "additional-details", "");
    postStep(
        sessionCookie,
        "/create-a-password",
        "password",
        "password1",
        "password-confirm",
        "password1");

    String csrf =
        csrf(
            mockMvc
                .perform(get("/check-answers").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andReturn());
    mockMvc
        .perform(
            post("/check-answers")
                .cookie(sessionCookie)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/confirmation"));

    mockMvc
        .perform(get("/confirmation").cookie(sessionCookie))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Application complete")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("RL")));
  }

  private void postStep(Cookie sessionCookie, String path, String... fields) throws Exception {
    String csrfToken =
        csrf(
            mockMvc
                .perform(get(path).cookie(sessionCookie))
                .andExpect(status().isOk())
                .andReturn());
    MockHttpServletRequestBuilder request =
        post(path)
            .cookie(sessionCookie)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("csrf", csrfToken);
    for (int i = 0; i + 1 < fields.length; i += 2) {
      request.param(fields[i], fields[i + 1]);
    }
    mockMvc
        .perform(request)
        .andExpect(status().is3xxRedirection())
        .andExpect(
            result ->
                assertThat(result.getResponse().getRedirectedUrl())
                    .as("POST %s should not fail CSRF/validation", path)
                    .doesNotContain("session-expired")
                    .isNotEqualTo(path));
  }

  private static String csrf(MvcResult result) throws Exception {
    Matcher matcher = CSRF.matcher(result.getResponse().getContentAsString());
    assertThat(matcher.find()).isTrue();
    return matcher.group(1);
  }

  private static Cookie sessionCookie(MockHttpServletResponse response) {
    Cookie cookie = response.getCookie(SessionFilter.COOKIE_NAME);
    assertThat(cookie).isNotNull();
    return cookie;
  }

  private static String currentStartMonth() {
    java.time.LocalDate now = java.time.LocalDate.now(java.time.ZoneOffset.UTC);
    return String.format("%04d-%02d", now.getYear(), now.getMonthValue());
  }
}
