package uk.gov.example.baseline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class ResponseHeadersTest {

  private Policy policy;

  @BeforeEach
  void loadPolicy() throws Exception {
    policy = Policy.load(PolicyTest.policyPath());
  }

  @Test
  void documentHeadersIncludeCspCacheAndDocumentHardening() {
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Server", "secret");
    ResponseHeaders.applyHeaders(
        policy,
        headers,
        new HeaderOptions(
            ResponseKind.DOCUMENT,
            true,
            true,
            null,
            List.of(new PreloadLink("/assets/fonts/light.woff2", "font", "font/woff2"))));

    assertThat(headers).doesNotContainKey("Server");
    assertThat(headers.get("Cache-Control")).isEqualTo("private, no-cache");
    assertThat(headers.get("Content-Security-Policy"))
        .contains(policy.jsEnabledScriptHash());
    assertThat(headers.get("Strict-Transport-Security")).contains("max-age=");
    assertThat(headers.get("X-Frame-Options")).isEqualTo("DENY");
    assertThat(headers.get("Cross-Origin-Opener-Policy")).isEqualTo("same-origin");
    assertThat(headers.get("Permissions-Policy")).isNotBlank();
    assertThat(headers.get("Link")).contains("crossorigin");
    assertThat(headers.get("Vary")).isEqualTo("Accept-Encoding");
    assertThat(headers.get("Content-Type")).isEqualTo("text/html; charset=utf-8");
  }

  @Test
  void hstsOnlyWhenSecureTransport() {
    Map<String, String> insecure = new LinkedHashMap<>();
    ResponseHeaders.applyHeaders(
        policy, insecure, new HeaderOptions(ResponseKind.SENSITIVE_DOCUMENT, false));
    assertThat(insecure).doesNotContainKey("Strict-Transport-Security");
    assertThat(insecure.get("Cache-Control")).isEqualTo("no-store");
    assertThat(insecure.get("Content-Security-Policy")).contains(policy.jsEnabledScriptHash());

    Map<String, String> secure = new LinkedHashMap<>();
    ResponseHeaders.applyHeaders(
        policy, secure, new HeaderOptions(ResponseKind.DOCUMENT, true));
    assertThat(secure.get("Strict-Transport-Security")).isNotBlank();
  }

  @Test
  void cacheControlKinds() {
    assertCache(ResponseKind.DOCUMENT, false, "no-cache");
    assertCache(ResponseKind.SENSITIVE_DOCUMENT, false, "no-store");
    assertCache(ResponseKind.FINGERPRINTED_ASSET, false, "public, max-age=31536000, immutable");
    assertCache(ResponseKind.STATIC_ASSET, false, "no-cache");
    assertCache(ResponseKind.DOWNLOAD, false, "private, no-cache");
    assertCache(ResponseKind.SENSITIVE_DOWNLOAD, false, "no-store");
  }

  @Test
  void assetsDoNotReceiveDocumentHeaders() {
    Map<String, String> headers = new LinkedHashMap<>();
    ResponseHeaders.applyHeaders(
        policy, headers, new HeaderOptions(ResponseKind.FINGERPRINTED_ASSET, false));
    assertThat(headers).doesNotContainKey("Content-Security-Policy");
    assertThat(headers).doesNotContainKey("X-Frame-Options");
    assertThat(headers.get("X-Content-Type-Options")).isEqualTo("nosniff");
  }

  @Test
  void applyHeadersOnServletResponse() {
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setHeader("Server", "secret");
    ResponseHeaders.applyHeaders(
        policy, response, new HeaderOptions(ResponseKind.DOCUMENT, true, true));
    assertThat(response.getHeader("Server")).isNull();
    assertThat(response.getHeader("Cache-Control")).isEqualTo("private, no-cache");
    assertThat(response.getHeader("Content-Security-Policy"))
        .contains(policy.jsEnabledScriptHash());
  }

  @Test
  void rejections() {
    Map<String, String> headers = new LinkedHashMap<>();
    assertThatThrownBy(
            () ->
                ResponseHeaders.applyHeaders(
                    policy,
                    headers,
                    new HeaderOptions(ResponseKind.STATIC_ASSET, false, true)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Set-Cookie");

    assertThatThrownBy(
            () ->
                ResponseHeaders.applyHeaders(
                    policy,
                    headers,
                    new HeaderOptions(
                        ResponseKind.DOCUMENT,
                        false,
                        false,
                        null,
                        List.of(new PreloadLink("https://example.com/a.woff2", "font")))))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void contentTypes() {
    assertContentType("text/plain; charset=utf-8", "text/plain; charset=utf-8", true);
    assertContentType("text/plain", "text/plain; charset=utf-8", true);
    assertContentType("application/json", "application/json; charset=utf-8", true);
    assertContentType("image/png", "image/png", true);
    assertContentType("text/plain\r\nX: 1", null, false);
  }

  @Test
  void emptyContentTypeIsOmittedForAssetsWithoutDefault() throws Exception {
    // static-asset has no default content type in policy.json
    Map<String, String> headers = new LinkedHashMap<>();
    ResponseHeaders.applyHeaders(
        policy, headers, new HeaderOptions(ResponseKind.STATIC_ASSET, false));
    assertThat(headers).doesNotContainKey("Content-Type");
  }

  @Test
  void preloadLinkHeader() {
    String ok =
        ResponseHeaders.preloadLinkHeader(
            List.of(
                new PreloadLink("/assets/app.css", "style", "text/css"),
                new PreloadLink("/assets/app.js", "script")));
    assertThat(ok).contains("type=\"text/css\"");
    assertThat(ok).doesNotContain("crossorigin");

    assertThatThrownBy(() -> ResponseHeaders.preloadLinkHeader(List.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                ResponseHeaders.preloadLinkHeader(
                    List.of(new PreloadLink("https://example.com/a.css", "style"))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                ResponseHeaders.preloadLinkHeader(
                    List.of(new PreloadLink("//cdn.example/a.css", "style"))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                ResponseHeaders.preloadLinkHeader(
                    List.of(new PreloadLink("/a.css", "video"))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                ResponseHeaders.preloadLinkHeader(
                    List.of(new PreloadLink("/a.css", "style", "not a type"))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                ResponseHeaders.preloadLinkHeader(
                    List.of(new PreloadLink("/a b.css", "style"))))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void strongEtagIsStableQuotedSha256() {
    String first = StrongEtag.of("a".getBytes());
    String second = StrongEtag.of("a");
    assertThat(first).isEqualTo(second);
    assertThat(first).startsWith("\"").endsWith("\"");
  }

  @Test
  void responseKindHelpers() {
    assertThat(ResponseKind.DOCUMENT.isDocument()).isTrue();
    assertThat(ResponseKind.SENSITIVE_DOCUMENT.isDocument()).isTrue();
    assertThat(ResponseKind.STATIC_ASSET.isDocument()).isFalse();
    assertThat(ResponseKind.fromValue("document")).isEqualTo(ResponseKind.DOCUMENT);
    assertThatThrownBy(() -> ResponseKind.fromValue("nope"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ResponseKind.fromValue(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private void assertCache(ResponseKind kind, boolean setsCookie, String expected) {
    Map<String, String> headers = new LinkedHashMap<>();
    ResponseHeaders.applyHeaders(
        policy, headers, new HeaderOptions(kind, false, setsCookie));
    assertThat(headers.get("Cache-Control")).isEqualTo(expected);
  }

  private void assertContentType(String in, String want, boolean ok) {
    Map<String, String> headers = new LinkedHashMap<>();
    if (ok) {
      ResponseHeaders.applyHeaders(
          policy,
          headers,
          new HeaderOptions(ResponseKind.DOWNLOAD, false, false, in, List.of()));
      assertThat(headers.get("Content-Type")).isEqualTo(want);
    } else {
      assertThatThrownBy(
              () ->
                  ResponseHeaders.applyHeaders(
                      policy,
                      headers,
                      new HeaderOptions(ResponseKind.DOWNLOAD, false, false, in, List.of())))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }
}
