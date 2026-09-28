package uk.gov.example.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Real Tomcat integration: session cookies must be set before the HTML body is committed, or
 * browsers never receive {@code rod_session} and CSRF checks fail on cookie-banner POSTs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SessionCookieHttpIT {

  private static final Pattern CSRF =
      Pattern.compile("name=\"csrf\"\\s+value=\"([^\"]+)\"");

  @LocalServerPort private int port;

  @Test
  void cookieBannerAcceptKeepsSession() throws Exception {
    HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
    String base = "http://127.0.0.1:" + port;

    HttpResponse<String> start =
        client.send(
            HttpRequest.newBuilder(URI.create(base + "/")).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(start.statusCode()).isEqualTo(200);

    Optional<String> setCookie =
        start.headers().allValues("Set-Cookie").stream()
            .filter(v -> v.startsWith(SessionFilter.COOKIE_NAME + "="))
            .findFirst();
    assertThat(setCookie)
        .as("Set-Cookie must be present on the first HTML response")
        .isPresent();

    String cookieHeader = setCookie.get().split(";", 2)[0];
    Matcher csrfMatcher = CSRF.matcher(start.body());
    assertThat(csrfMatcher.find()).isTrue();
    String csrf = csrfMatcher.group(1);

    String body =
        "csrf="
            + URLEncoder.encode(csrf, StandardCharsets.UTF_8)
            + "&cookies=accept&returnPath="
            + URLEncoder.encode("/", StandardCharsets.UTF_8);

    HttpResponse<String> post =
        client.send(
            HttpRequest.newBuilder(URI.create(base + "/cookie-choices"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", cookieHeader)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString());

    assertThat(post.statusCode()).isEqualTo(302);
    List<String> locations = post.headers().allValues("Location");
    assertThat(locations).isNotEmpty();
    assertThat(locations.getFirst()).doesNotContain("session-expired");
    assertThat(locations.getFirst()).endsWith("/");

    HttpResponse<String> after =
        client.send(
            HttpRequest.newBuilder(URI.create(base + "/"))
                .header("Cookie", cookieHeader)
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(after.body()).contains("You have accepted analytics cookies");
  }
}
