package uk.gov.example.baseline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PolicyTest {

  private Policy policy;

  @BeforeEach
  void loadPolicy() throws Exception {
    policy = Policy.load(policyPath());
  }

  static Path policyPath() {
    return Path.of(System.getProperty("user.dir"), "baseline", "policy.json");
  }

  @Test
  void loadParsesJsEnabledSnippetAndHash() {
    assertThat(policy.jsEnabledSnippet()).isNotBlank();
    assertThat(policy.jsEnabledScriptHash())
        .isEqualTo("sha256-GUQ5ad8JK5KmEWmROf3LZd9ge94daqNvd8xy9YS1iDw=");
  }

  @Test
  void contentSecurityPolicyAppendsJsEnabledHashToScriptSrc() {
    String csp = policy.contentSecurityPolicy();
    assertThat(csp).contains("script-src");
    assertThat(csp).contains("'" + policy.jsEnabledScriptHash() + "'");
    assertThat(csp).contains("upgrade-insecure-requests");
  }

  @Test
  void permissionsPolicyHeaderDeniesAllFeatures() {
    String header = policy.permissionsPolicyHeader();
    assertThat(header).isNotBlank();
    assertThat(header).contains("camera=()");
    assertThat(header).contains("geolocation=()");
  }

  @Test
  void cookieSettingsMatchPolicyDefaults() {
    Policy.CookieSettings cookie = policy.cookie();
    assertThat(cookie.sameSite()).isEqualTo("Lax");
    assertThat(cookie.secure()).isTrue();
    assertThat(cookie.httpOnly()).isTrue();
    assertThat(cookie.path()).isEqualTo("/");
  }

  @Test
  void directivesReturnsDefensiveCopy() {
    assertThat(policy.directives()).isNotEmpty();
    String first = policy.directives().getFirst().name();
    assertThatThrownBy(() -> policy.directives().set(0, new Policy.Directive("mutated", null)))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThat(policy.directives().getFirst().name()).isEqualTo(first);
  }

  @Test
  void parseRejectsMissingRequiredFields() {
    assertThatThrownBy(() -> Policy.parse("{}".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledScriptHash":"h","cacheControl":{"document":"no-cache"},\
                    "csp":{"directives":{"default-src":["'self'"]}}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("jsEnabledSnippet");

    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledSnippet":"s","cacheControl":{"document":"no-cache"},\
                    "csp":{"directives":{"default-src":["'self'"]}}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("jsEnabledScriptHash");

    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
                    "csp":{"directives":{"default-src":["'self'"]}}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cacheControl");

    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
                    "cacheControl":{"document":"no-cache"},"csp":{"directives":{}}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("csp.directives");
  }

  @Test
  void parseRejectsInvalidJsonAndDirectiveShapes() {
    assertThatThrownBy(() -> Policy.parse("{".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
                    "cacheControl":{"document":"no-cache"},"csp":{"directives":[]}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(
            () ->
                Policy.parse(
                    """
                    {"jsEnabledSnippet":"s","jsEnabledScriptHash":"h",\
                    "cacheControl":{"document":"no-cache"},\
                    "csp":{"directives":{"default-src":"'self'"}}}\
                    """
                        .getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void loadMissingFileFails() {
    assertThatThrownBy(() -> Policy.load(Path.of("does-not-exist-baseline-policy.json")))
        .isInstanceOf(Exception.class);
  }

  @Test
  void setCookieUsesPolicyDefaultsAndOverrides() {
    String value =
        policy.setCookie(
            "rod_session",
            "abc",
            new Policy.CookieOptions(null, false, false, null, 60, false));
    assertThat(value).contains("rod_session=abc");
    assertThat(value).contains("Max-Age=60");
    assertThat(value).contains("Path=/");
    assertThat(value).contains("SameSite=Lax");
    assertThat(value).doesNotContain("Secure");
    assertThat(value).doesNotContain("HttpOnly");

    String host =
        policy.setCookie(
            "__Host-session",
            "abc",
            new Policy.CookieOptions("Strict", true, null, "/", null, true));
    assertThat(host).contains("Secure");
    assertThat(host).contains("SameSite=Strict");

    String none =
        policy.setCookie(
            "analytics", "1", new Policy.CookieOptions("None", true, null, "/", null, false));
    assertThat(none).contains("SameSite=None");
    assertThat(none).contains("Secure");
  }

  @Test
  void setCookieRejectsInvalidInput() {
    assertThatThrownBy(() -> policy.setCookie("bad name", "a", Policy.CookieOptions.defaults()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> policy.setCookie("ok", "a;b", Policy.CookieOptions.defaults()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                policy.setCookie(
                    "ok", "a", new Policy.CookieOptions("Maybe", null, null, null, null, false)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                policy.setCookie(
                    "ok", "a", new Policy.CookieOptions("None", false, null, null, null, false)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                policy.setCookie(
                    "ok", "a", new Policy.CookieOptions(null, null, null, "relative", null, false)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                policy.setCookie(
                    "session", "a", new Policy.CookieOptions(null, null, null, null, null, true)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                policy.setCookie(
                    "__Host-session",
                    "a",
                    new Policy.CookieOptions(null, false, null, null, null, false)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                policy.setCookie(
                    "__Host-session",
                    "a",
                    new Policy.CookieOptions(null, true, null, "/app", null, false)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                policy.setCookie(
                    "ok", "a", new Policy.CookieOptions(null, null, null, null, -1, false)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
