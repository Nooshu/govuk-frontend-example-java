package uk.gov.example.baseline;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Applies OWASP and cache headers for a response kind, matching {@code baseline/policy.json}.
 */
public final class ResponseHeaders {

  private static final Set<String> PRELOAD_AS =
      Set.of("style", "script", "font", "image", "fetch");
  private static final Pattern PRELOAD_TYPE = Pattern.compile("^[\\w.+-]+/[\\w.+-]+$");

  private ResponseHeaders() {}

  /**
   * Writes the baseline headers for one response onto {@code headers}.
   *
   * <p>Removes the headers policy.json bans before setting anything. Documents additionally get
   * the frame, cross-origin, permissions, and content security policies.
   */
  public static void applyHeaders(Policy policy, Map<String, String> headers, HeaderOptions options) {
    Objects.requireNonNull(policy, "policy");
    Objects.requireNonNull(headers, "headers");
    Objects.requireNonNull(options, "options");

    String cacheControl = policy.cacheControl().get(options.kind().value());
    if (cacheControl == null) {
      throw new IllegalArgumentException(
          "baseline: unknown response kind: " + options.kind().value());
    }
    boolean document = options.kind().isDocument();
    if (options.setsCookie() && !document) {
      throw new IllegalArgumentException(
          "baseline: Set-Cookie belongs on HTML documents, not on " + options.kind().value());
    }

    for (String name : policy.remove()) {
      headers.remove(name);
    }

    String contentType = policy.resolveContentType(options);
    if (contentType != null) {
      headers.put("Content-Type", contentType);
    }

    if (options.setsCookie() && options.kind() == ResponseKind.DOCUMENT) {
      cacheControl = "private, no-cache";
    }
    headers.put("Cache-Control", cacheControl);

    headers.putAll(policy.headersAll());
    if (document) {
      headers.putAll(policy.headersDocument());
      headers.put("Permissions-Policy", policy.permissionsPolicyHeader());
      headers.put("Content-Security-Policy", policy.contentSecurityPolicy());
    }
    if (options.secureTransport()) {
      headers.put("Strict-Transport-Security", policy.strictTransportSecurity());
    }
    headers.put("Vary", "Accept-Encoding");
    if (!options.preload().isEmpty()) {
      headers.put("Link", preloadLinkHeader(options.preload()));
    }
  }

  /**
   * Writes the baseline headers for one response onto an {@link HttpServletResponse}.
   *
   * <p>Banned headers are cleared (via {@code setHeader(name, null)}) before the baseline set is
   * applied.
   */
  public static void applyHeaders(
      Policy policy, HttpServletResponse response, HeaderOptions options) {
    Objects.requireNonNull(policy, "policy");
    Objects.requireNonNull(response, "response");
    Objects.requireNonNull(options, "options");
    Map<String, String> headers = new LinkedHashMap<>();
    applyHeaders(policy, headers, options);
    for (String name : policy.remove()) {
      response.setHeader(name, null);
    }
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      response.setHeader(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Formats preload hints for the {@code Link} header.
   *
   * <p>Hrefs must be same-origin paths: a preload is a promise about this service's own assets.
   */
  public static String preloadLinkHeader(List<PreloadLink> links) {
    if (links == null || links.isEmpty()) {
      throw new IllegalArgumentException("baseline: preload links must not be empty");
    }
    List<String> formatted = new ArrayList<>(links.size());
    for (PreloadLink link : links) {
      if (!isSameOriginPath(link.href())) {
        throw new IllegalArgumentException(
            "baseline: preload href must be a same-origin path: " + link.href());
      }
      if (!PRELOAD_AS.contains(link.as())) {
        throw new IllegalArgumentException("baseline: unsupported preload as: " + link.as());
      }
      StringBuilder parts = new StringBuilder();
      parts.append('<').append(link.href()).append('>');
      parts.append("; rel=preload");
      parts.append("; as=").append(link.as());
      if (link.type() != null && !link.type().isEmpty()) {
        if (!PRELOAD_TYPE.matcher(link.type()).matches()) {
          throw new IllegalArgumentException("baseline: invalid preload type: " + link.type());
        }
        parts.append("; type=\"").append(link.type()).append('"');
      }
      if ("font".equals(link.as())) {
        parts.append("; crossorigin");
      }
      formatted.add(parts.toString());
    }
    return String.join(", ", formatted);
  }

  private static boolean isSameOriginPath(String href) {
    return href.startsWith("/")
        && !href.startsWith("//")
        && href.indexOf(' ') < 0
        && href.indexOf('\t') < 0
        && href.indexOf('"') < 0
        && href.indexOf('<') < 0
        && href.indexOf('>') < 0;
  }
}
