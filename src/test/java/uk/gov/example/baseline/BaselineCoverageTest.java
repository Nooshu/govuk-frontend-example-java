package uk.gov.example.baseline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BaselineCoverageTest {

  @Test
  void strongEtagRejectsNullAndMissingDigest() {
    assertThatThrownBy(() -> StrongEtag.of((byte[]) null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> StrongEtag.of((String) null)).isInstanceOf(NullPointerException.class);
    try (MockedStatic<MessageDigest> md = mockStatic(MessageDigest.class)) {
      md.when(() -> MessageDigest.getInstance("SHA-256"))
          .thenThrow(new NoSuchAlgorithmException("gone"));
      assertThatThrownBy(() -> StrongEtag.of("x".getBytes(StandardCharsets.UTF_8)))
          .isInstanceOf(IllegalStateException.class);
    }
  }

  @Test
  void preloadAndHeaderOptionsNullGuards() {
    assertThatThrownBy(() -> new PreloadLink(null, "font")).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new PreloadLink("/a", null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new HeaderOptions(null, true)).isInstanceOf(NullPointerException.class);
    HeaderOptions withNullPreload = new HeaderOptions(ResponseKind.DOCUMENT, true, false, null, null);
    assertThat(withNullPreload.preload()).isEmpty();
  }

  @Test
  void responseHeadersUnknownKindAndEmptyTypeAndNullPreload() throws Exception {
    Policy policy = Policy.load(PolicyTest.policyPath());
    Policy mocked = mock(Policy.class);
    when(mocked.cacheControl()).thenReturn(Map.of());
    when(mocked.remove()).thenReturn(List.of());
    Map<String, String> headers = new LinkedHashMap<>();
    assertThatThrownBy(
            () ->
                ResponseHeaders.applyHeaders(
                    mocked, headers, new HeaderOptions(ResponseKind.DOCUMENT, false)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("unknown response kind");

    assertThatThrownBy(() -> ResponseHeaders.preloadLinkHeader(null))
        .isInstanceOf(IllegalArgumentException.class);

    Map<String, String> withEmptyType = new LinkedHashMap<>();
    ResponseHeaders.applyHeaders(
        policy,
        withEmptyType,
        new HeaderOptions(ResponseKind.DOWNLOAD, false, false, "", List.of()));
    // empty override falls back to policy default for download
    assertThat(withEmptyType.get("Content-Type")).isNotBlank();

    Map<String, String> sensitive = new LinkedHashMap<>();
    ResponseHeaders.applyHeaders(
        policy, sensitive, new HeaderOptions(ResponseKind.SENSITIVE_DOWNLOAD, false));
    assertThat(sensitive.get("Cache-Control")).isEqualTo("no-store");

    assertThatThrownBy(
            () ->
                ResponseHeaders.preloadLinkHeader(
                    List.of(new PreloadLink("/a.css\tbad", "style"))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                ResponseHeaders.preloadLinkHeader(
                    List.of(new PreloadLink("/a\"b.css", "style"))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                ResponseHeaders.preloadLinkHeader(
                    List.of(new PreloadLink("/a<b.css", "style"))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                ResponseHeaders.preloadLinkHeader(
                    List.of(new PreloadLink("/a>b.css", "style"))))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void policyParseEdgeCasesAndAccessors() throws Exception {
    Policy policy = Policy.load(PolicyTest.policyPath());
    assertThat(policy.hsts().maxAge()).isPositive();
    assertThat(policy.headersAll()).isNotEmpty();
    assertThat(policy.headersDocument()).isNotEmpty();
    assertThat(policy.remove()).isNotEmpty();
    assertThat(policy.cacheControl()).containsKey("document");
    assertThat(policy.contentTypes()).isNotEmpty();

    assertThatThrownBy(() -> Policy.parse("null".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("JSON object");
    assertThatThrownBy(() -> Policy.parse("[]".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class);

    Path bad = Files.createTempFile("policy-bad", ".json");
    Files.writeString(bad, "{}");
    assertThatThrownBy(() -> Policy.load(bad))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("reading");

    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
                    "cacheControl":{"document":"no-cache"},\
                    "csp":{"directives":{"default-src":[1]}}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("strings");

    Policy noSub =
        Policy.parse(
            """
            {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
            "cacheControl":{"document":"no-cache","sensitive-document":"no-store",\
            "fingerprinted-asset":"public","static-asset":"no-cache",\
            "download":"private","sensitive-download":"no-store"},\
            "contentTypes":{"document":"text/html"},\
            "csp":{"directives":{"default-src":["'self'"],"script-src":["'self'"]}},\
            "hsts":{"maxAge":100,"includeSubDomains":false},\
            "headers":{"all":{"X-Content-Type-Options":"nosniff"},"document":{}},\
            "remove":["Server"],"permissionsPolicy":["camera"],\
            "cookie":{"sameSite":1}}\
            """
                .getBytes(StandardCharsets.UTF_8));
    Map<String, String> headers = new LinkedHashMap<>();
    ResponseHeaders.applyHeaders(
        noSub, headers, new HeaderOptions(ResponseKind.DOCUMENT, true));
    assertThat(headers.get("Strict-Transport-Security")).isEqualTo("max-age=100");

    String cookie =
        policy.setCookie("ok", "v", null);
    assertThat(cookie).contains("ok=v");

    Policy emptyCookiePath =
        Policy.parse(
            """
            {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
            "cacheControl":{"document":"no-cache"},\
            "csp":{"directives":{"default-src":["'self'"]}},\
            "cookie":{"path":""}}\
            """
                .getBytes(StandardCharsets.UTF_8));
    assertThatThrownBy(
            () ->
                emptyCookiePath.setCookie(
                    "ok", "v", new Policy.CookieOptions(null, null, null, null, null, false)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Path");

    // non-object cacheControl / remove shapes fall back to empty then fail required checks
    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
                    "cacheControl":[],"csp":{"directives":{"default-src":["'self'"]}}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class);

    Policy withNonTextualSnippet =
        Policy.parse(
            """
            {"jsEnabledSnippet":"keep","jsEnabledScriptHash":"h",\
            "cacheControl":{"document":"no-cache"},\
            "csp":{"directives":{"default-src":["'self'"]}},\
            "remove":"not-array","permissionsPolicy":{"x":1},\
            "headers":{"all":"x","document":null}}\
            """
                .getBytes(StandardCharsets.UTF_8));
    assertThat(withNonTextualSnippet.remove()).isEmpty();
  }

  @Test
  void resolveContentTypeCharsetAndXmlBranches() throws Exception {
    Policy policy = Policy.load(PolicyTest.policyPath());
    Map<String, String> headers = new LinkedHashMap<>();
    ResponseHeaders.applyHeaders(
        policy,
        headers,
        new HeaderOptions(
            ResponseKind.DOWNLOAD, false, false, "application/xml", List.of()));
    assertThat(headers.get("Content-Type")).isEqualTo("application/xml; charset=utf-8");

    headers.clear();
    ResponseHeaders.applyHeaders(
        policy,
        headers,
        new HeaderOptions(
            ResponseKind.DOWNLOAD, false, false, "application/javascript", List.of()));
    assertThat(headers.get("Content-Type")).contains("charset=utf-8");
  }
}
