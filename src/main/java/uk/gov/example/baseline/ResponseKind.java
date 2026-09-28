package uk.gov.example.baseline;

/**
 * Response kinds from {@code baseline/policy.json}. Each selects a Cache-Control value and, for
 * documents, the full set of document security headers.
 */
public enum ResponseKind {
  /** Public HTML. */
  DOCUMENT("document"),
  /** HTML that shows someone's answers, so it is never stored. */
  SENSITIVE_DOCUMENT("sensitive-document"),
  /** An asset whose URL changes when its bytes change. */
  FINGERPRINTED_ASSET("fingerprinted-asset"),
  /** An asset served from a stable URL, so it must be revalidated. */
  STATIC_ASSET("static-asset"),
  /** A public file attachment. */
  DOWNLOAD("download"),
  /** A file attachment that is never stored. */
  SENSITIVE_DOWNLOAD("sensitive-download");

  private final String value;

  ResponseKind(String value) {
    this.value = value;
  }

  /** The JSON / Cache-Control map key for this kind. */
  public String value() {
    return value;
  }

  /** Whether this kind is an HTML document. */
  public boolean isDocument() {
    return this == DOCUMENT || this == SENSITIVE_DOCUMENT;
  }

  /**
   * Parse a kind string from policy.json / callers.
   *
   * @throws IllegalArgumentException if the value is not a known kind
   */
  public static ResponseKind fromValue(String value) {
    if (value == null) {
      throw new IllegalArgumentException("baseline: response kind must not be null");
    }
    for (ResponseKind kind : values()) {
      if (kind.value.equals(value)) {
        return kind;
      }
    }
    throw new IllegalArgumentException("baseline: unknown response kind: " + value);
  }
}
