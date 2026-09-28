package uk.gov.example.baseline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Parsed {@code baseline/policy.json}: CSP, cache kinds, cookie defaults, and header sets.
 *
 * <p>CSP directive order matches the JSON object key order so the header string stays comparable
 * with the Node oracle.
 */
public final class Policy {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final String jsEnabledSnippet;
  private final String jsEnabledScriptHash;
  private final Hsts hsts;
  private final Map<String, String> headersAll;
  private final Map<String, String> headersDocument;
  private final List<String> remove;
  private final Map<String, String> cacheControl;
  private final Map<String, String> contentTypes;
  private final List<Directive> cspDirectives;
  private final List<String> permissionsPolicy;
  private final CookieSettings cookie;

  private Policy(
      String jsEnabledSnippet,
      String jsEnabledScriptHash,
      Hsts hsts,
      Map<String, String> headersAll,
      Map<String, String> headersDocument,
      List<String> remove,
      Map<String, String> cacheControl,
      Map<String, String> contentTypes,
      List<Directive> cspDirectives,
      List<String> permissionsPolicy,
      CookieSettings cookie) {
    this.jsEnabledSnippet = jsEnabledSnippet;
    this.jsEnabledScriptHash = jsEnabledScriptHash;
    this.hsts = hsts;
    this.headersAll = headersAll;
    this.headersDocument = headersDocument;
    this.remove = remove;
    this.cacheControl = cacheControl;
    this.contentTypes = contentTypes;
    this.cspDirectives = cspDirectives;
    this.permissionsPolicy = permissionsPolicy;
    this.cookie = cookie;
  }

  /** Load and parse a policy document from a filesystem path. */
  public static Policy load(Path path) throws IOException {
    Objects.requireNonNull(path, "path");
    byte[] raw = Files.readAllBytes(path);
    try {
      return parse(raw);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("baseline: reading " + path + ": " + e.getMessage(), e);
    }
  }

  /**
   * Parse a policy document from JSON bytes.
   *
   * @throws IllegalArgumentException when a field the server depends on is missing
   */
  public static Policy parse(byte[] raw) {
    Objects.requireNonNull(raw, "raw");
    final JsonNode root;
    try {
      root = MAPPER.readTree(raw);
    } catch (IOException e) {
      throw new IllegalArgumentException("baseline: parsing policy: " + e.getMessage(), e);
    }
    if (!root.isObject()) {
      throw new IllegalArgumentException("baseline: policy must be a JSON object");
    }

    String snippet = textOrEmpty(root, "jsEnabledSnippet");
    String hash = textOrEmpty(root, "jsEnabledScriptHash");
    Map<String, String> cacheControl = stringMap(root.get("cacheControl"));
    List<Directive> directives = parseDirectives(root.path("csp").path("directives"));

    if (snippet.isEmpty()) {
      throw new IllegalArgumentException("baseline: policy has no jsEnabledSnippet");
    }
    if (hash.isEmpty()) {
      throw new IllegalArgumentException("baseline: policy has no jsEnabledScriptHash");
    }
    if (cacheControl.isEmpty()) {
      throw new IllegalArgumentException("baseline: policy has no cacheControl");
    }
    if (directives.isEmpty()) {
      throw new IllegalArgumentException("baseline: policy has no csp.directives");
    }

    JsonNode hstsNode = root.path("hsts");
    Hsts hsts =
        new Hsts(
            hstsNode.path("maxAge").asInt(0), hstsNode.path("includeSubDomains").asBoolean(false));

    JsonNode headers = root.path("headers");
    Map<String, String> headersAll = stringMap(headers.get("all"));
    Map<String, String> headersDocument = stringMap(headers.get("document"));

    List<String> remove = stringList(root.get("remove"));
    Map<String, String> contentTypes = stringMap(root.get("contentTypes"));
    List<String> permissions = stringList(root.get("permissionsPolicy"));

    JsonNode cookieNode = root.path("cookie");
    CookieSettings cookie =
        new CookieSettings(
            textOrDefault(cookieNode, "sameSite", "Lax"),
            cookieNode.path("secure").asBoolean(true),
            cookieNode.path("httpOnly").asBoolean(true),
            textOrDefault(cookieNode, "path", "/"));

    return new Policy(
        snippet,
        hash,
        hsts,
        headersAll,
        headersDocument,
        remove,
        cacheControl,
        contentTypes,
        directives,
        permissions,
        cookie);
  }

  public String jsEnabledSnippet() {
    return jsEnabledSnippet;
  }

  public String jsEnabledScriptHash() {
    return jsEnabledScriptHash;
  }

  public Hsts hsts() {
    return hsts;
  }

  public Map<String, String> headersAll() {
    return headersAll;
  }

  public Map<String, String> headersDocument() {
    return headersDocument;
  }

  public List<String> remove() {
    return remove;
  }

  public Map<String, String> cacheControl() {
    return cacheControl;
  }

  public Map<String, String> contentTypes() {
    return contentTypes;
  }

  /** CSP directives in policy order (defensive copy). */
  public List<Directive> directives() {
    return List.copyOf(cspDirectives);
  }

  public List<String> permissionsPolicy() {
    return permissionsPolicy;
  }

  public CookieSettings cookie() {
    return cookie;
  }

  /**
   * Builds the CSP header value.
   *
   * <p>The hash of the js-enabled snippet is appended to {@code script-src} so GOV.UK Frontend's
   * inline snippet runs without opening the policy to {@code 'unsafe-inline'}.
   */
  public String contentSecurityPolicy() {
    List<String> parts = new ArrayList<>(cspDirectives.size());
    for (Directive directive : cspDirectives) {
      if (directive.sources().isEmpty()) {
        parts.add(directive.name());
        continue;
      }
      List<String> sources = new ArrayList<>(directive.sources());
      if ("script-src".equals(directive.name())) {
        sources.add("'" + jsEnabledScriptHash + "'");
      }
      parts.add(directive.name() + " " + String.join(" ", sources));
    }
    return String.join("; ", parts);
  }

  /** Denies every feature listed in the policy. */
  public String permissionsPolicyHeader() {
    List<String> features = new ArrayList<>(permissionsPolicy.size());
    for (String name : permissionsPolicy) {
      features.add(name + "=()");
    }
    return String.join(", ", features);
  }

  String strictTransportSecurity() {
    String value = "max-age=" + hsts.maxAge();
    if (hsts.includeSubDomains()) {
      value += "; includeSubDomains";
    }
    return value;
  }

  String resolveContentType(HeaderOptions options) {
    String chosen = options.contentType();
    if (chosen == null || chosen.isEmpty()) {
      chosen = contentTypes.get(options.kind().value());
    }
    if (chosen == null) {
      return null;
    }
    if (chosen.isEmpty()) {
      return null;
    }
    if (chosen.indexOf('\r') >= 0 || chosen.indexOf('\n') >= 0) {
      throw new IllegalArgumentException("baseline: invalid Content-Type: " + chosen);
    }
    if (chosen.toLowerCase().contains("charset=")) {
      return chosen;
    }
    String lower = chosen.toLowerCase();
    if (lower.startsWith("text/")
        || lower.contains("json")
        || lower.contains("xml")
        || lower.contains("javascript")) {
      return chosen + "; charset=utf-8";
    }
    return chosen;
  }

  /**
   * Builds a {@code Set-Cookie} header value from the policy defaults.
   *
   * @throws IllegalArgumentException when the cookie would be silently dropped by browsers
   */
  public String setCookie(String name, String value, CookieOptions options) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(value, "value");
    if (options == null) {
      options = CookieOptions.defaults();
    }

    if (!COOKIE_NAME.matcher(name).matches()) {
      throw new IllegalArgumentException("baseline: invalid cookie name");
    }
    if (!COOKIE_VALUE.matcher(value).matches()) {
      throw new IllegalArgumentException("baseline: invalid cookie value");
    }

    String sameSite = options.sameSite() != null ? options.sameSite() : cookie.sameSite();
    if (!SAME_SITE.contains(sameSite)) {
      throw new IllegalArgumentException("baseline: invalid SameSite: " + sameSite);
    }

    boolean secure = options.secure() != null ? options.secure() : cookie.secure();
    boolean httpOnly = options.httpOnly() != null ? options.httpOnly() : cookie.httpOnly();
    if ("None".equals(sameSite) && !secure) {
      throw new IllegalArgumentException("baseline: SameSite=None requires Secure");
    }

    String path = options.path() != null ? options.path() : cookie.path();
    if (path == null) {
      path = "/";
    }
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("baseline: cookie Path must start with /, got " + path);
    }

    boolean hostPrefixed = name.startsWith("__Host-");
    if (options.hostPrefix() && !hostPrefixed) {
      throw new IllegalArgumentException("baseline: HostPrefix requires a __Host- cookie name");
    }
    if (hostPrefixed && !secure) {
      throw new IllegalArgumentException("baseline: __Host- cookies require Secure");
    }
    if (hostPrefixed && !"/".equals(path)) {
      throw new IllegalArgumentException("baseline: __Host- cookies require Path=/");
    }
    if (options.maxAge() != null && options.maxAge() < 0) {
      throw new IllegalArgumentException("baseline: Max-Age must not be negative");
    }

    StringBuilder parts = new StringBuilder();
    parts.append(name).append('=').append(value);
    if (options.maxAge() != null) {
      parts.append("; Max-Age=").append(options.maxAge());
    }
    parts.append("; Path=").append(path);
    if (secure) {
      parts.append("; Secure");
    }
    if (httpOnly) {
      parts.append("; HttpOnly");
    }
    parts.append("; SameSite=").append(sameSite);
    return parts.toString();
  }

  private static final java.util.regex.Pattern COOKIE_NAME =
      java.util.regex.Pattern.compile("^[!#$%&'*+\\-.^_`|~0-9A-Za-z]+$");
  private static final java.util.regex.Pattern COOKIE_VALUE =
      java.util.regex.Pattern.compile("^[\\x21\\x23-\\x2B\\x2D-\\x3A\\x3C-\\x5B\\x5D-\\x7E]*$");
  private static final java.util.Set<String> SAME_SITE =
      java.util.Set.of("Lax", "Strict", "None");

  private static List<Directive> parseDirectives(JsonNode node) {
    if (node.isMissingNode()) {
      return List.of();
    }
    if (node.isNull()) {
      return List.of();
    }
    if (!node.isObject()) {
      throw new IllegalArgumentException("baseline: csp.directives must be a JSON object");
    }
    List<Directive> ordered = new ArrayList<>();
    for (Map.Entry<String, JsonNode> entry : node.properties()) {
      JsonNode sourcesNode = entry.getValue();
      if (!sourcesNode.isArray()) {
        throw new IllegalArgumentException(
            "baseline: csp directive sources must be arrays: " + entry.getKey());
      }
      List<String> sources = new ArrayList<>(sourcesNode.size());
      for (JsonNode source : sourcesNode) {
        if (!source.isTextual()) {
          throw new IllegalArgumentException(
              "baseline: csp directive sources must be strings: " + entry.getKey());
        }
        sources.add(source.asText());
      }
      ordered.add(new Directive(entry.getKey(), List.copyOf(sources)));
    }
    return List.copyOf(ordered);
  }

  private static Map<String, String> stringMap(JsonNode node) {
    if (node == null || !node.isObject()) {
      return Map.of();
    }
    Map<String, String> map = new LinkedHashMap<>();
    for (Map.Entry<String, JsonNode> entry : node.properties()) {
      map.put(entry.getKey(), entry.getValue().asText());
    }
    return Collections.unmodifiableMap(map);
  }

  private static List<String> stringList(JsonNode node) {
    if (node == null || !node.isArray()) {
      return List.of();
    }
    List<String> list = new ArrayList<>(node.size());
    for (JsonNode item : node) {
      list.add(item.asText());
    }
    return List.copyOf(list);
  }

  private static String textOrEmpty(JsonNode root, String field) {
    JsonNode node = root.get(field);
    if (node == null) {
      return "";
    }
    if (!node.isTextual()) {
      return "";
    }
    return node.asText();
  }

  private static String textOrDefault(JsonNode root, String field, String fallback) {
    JsonNode node = root.get(field);
    if (node == null || !node.isTextual()) {
      return fallback;
    }
    return node.asText();
  }

  /** One Content-Security-Policy directive and its sources. */
  public record Directive(String name, List<String> sources) {
    public Directive {
      Objects.requireNonNull(name, "name");
      sources = sources == null ? List.of() : List.copyOf(sources);
    }
  }

  /** HSTS settings from the policy. */
  public record Hsts(int maxAge, boolean includeSubDomains) {}

  /** Cookie defaults from the policy. */
  public record CookieSettings(String sameSite, boolean secure, boolean httpOnly, String path) {}

  /**
   * Overrides the cookie defaults in policy.json for one Set-Cookie header.
   *
   * <p>{@code Boolean} fields are nullable so "not set" can be told apart from {@code false}.
   */
  public record CookieOptions(
      String sameSite, Boolean secure, Boolean httpOnly, String path, Integer maxAge, boolean hostPrefix) {

    public static CookieOptions defaults() {
      return new CookieOptions(null, null, null, null, null, false);
    }
  }
}
