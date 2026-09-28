package uk.gov.example.govuk;

/**
 * Sentinel for a missing option, distinct from {@code null} (JSON null).
 * Matches JavaScript {@code undefined}.
 */
public final class Undefined {
  public static final Undefined INSTANCE = new Undefined();

  private Undefined() {}

  @Override
  public String toString() {
    return "undefined";
  }
}
