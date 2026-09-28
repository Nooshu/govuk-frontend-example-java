package uk.gov.example.govuk;

/**
 * A string that is already HTML and must be emitted without escaping.
 * Equivalent to a Nunjucks SafeString ({@code | safe}).
 */
public record TrustedHtml(String value) {
  public TrustedHtml {
    if (value == null) {
      throw new NullPointerException("TrustedHtml value must not be null");
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
