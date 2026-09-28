package uk.gov.example.govuk;

/**
 * JSON number with original spelling preserved (Jackson token text as parsed).
 */
public record JsonNumber(String raw) {
  public JsonNumber {
    if (raw == null) {
      throw new NullPointerException("JsonNumber raw must not be null");
    }
  }

  /** JavaScript-style stringification: {@code 5} stays {@code 5}, not {@code 5.0}. */
  public String toJsString() {
    try {
      long integer = Long.parseLong(raw);
      return Long.toString(integer);
    } catch (NumberFormatException ignored) {
      // fall through
    }
    try {
      double value = Double.parseDouble(raw);
      if (Double.isFinite(value)) {
        // Match JavaScript number stringification (no trailing .0 for integers)
        return trimFloat(value);
      }
    } catch (NumberFormatException ignored) {
      // fall through
    }
    return raw;
  }

  public double doubleValue() {
    try {
      return Double.parseDouble(raw);
    } catch (NumberFormatException e) {
      return Double.NaN;
    }
  }

  private static String trimFloat(double value) {
    // Java Double.toString can use scientific notation; prefer fixed decimal form for JS parity
    if (value == (long) value && !Double.isInfinite(value) && Math.abs(value) < 1e15) {
      return Long.toString((long) value);
    }
    String s = Double.toString(value);
    // Double.toString may produce "1.0" — strip trailing .0 for integers already handled above
    if (s.indexOf('E') >= 0 || s.indexOf('e') >= 0) {
      // Avoid scientific notation for fixture parity with JavaScript number printing
      java.math.BigDecimal bd = java.math.BigDecimal.valueOf(value).stripTrailingZeros();
      return bd.toPlainString();
    }
    if (s.endsWith(".0")) {
      return s.substring(0, s.length() - 2);
    }
    return s;
  }

  @Override
  public String toString() {
    return raw;
  }
}
