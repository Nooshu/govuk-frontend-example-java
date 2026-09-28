package uk.gov.example.baseline;

import java.util.List;

/**
 * Describes one response so the baseline can choose its headers.
 *
 * @param kind one of the {@link ResponseKind} values; decides caching and document hardening
 * @param secureTransport whether the request arrived over HTTPS (HSTS is only sent when true)
 * @param setsCookie downgrades a public document to private caching
 * @param contentType overrides the kind's default type; may be {@code null}
 * @param preload resources to announce in the Link header; may be {@code null} or empty
 */
public record HeaderOptions(
    ResponseKind kind,
    boolean secureTransport,
    boolean setsCookie,
    String contentType,
    List<PreloadLink> preload) {

  public HeaderOptions {
    if (kind == null) {
      throw new NullPointerException("kind must not be null");
    }
    preload = preload == null ? List.of() : List.copyOf(preload);
  }

  public HeaderOptions(ResponseKind kind, boolean secureTransport) {
    this(kind, secureTransport, false, null, List.of());
  }

  public HeaderOptions(ResponseKind kind, boolean secureTransport, boolean setsCookie) {
    this(kind, secureTransport, setsCookie, null, List.of());
  }
}
