package uk.gov.example.govuk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Nunjucks-parity filters: escape, indent, length, and related helpers.
 */
public final class Nunjucks {

  private Nunjucks() {}

  /** Nunjucks escape map, backslash included. */
  public static String escape(String text) {
    if (text == null || text.isEmpty()) {
      return text == null ? "" : text;
    }
    StringBuilder out = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      switch (c) {
        case '&' -> out.append("&amp;");
        case '"' -> out.append("&quot;");
        case '\'' -> out.append("&#39;");
        case '<' -> out.append("&lt;");
        case '>' -> out.append("&gt;");
        case '\\' -> out.append("&#92;");
        default -> out.append(c);
      }
    }
    return out.toString();
  }

  /**
   * Renders a value for a {@code {{ }}} expression in an autoescaping template: {@link
   * TrustedHtml} passes through, everything else is escaped.
   */
  public static String out(Object value) {
    if (value instanceof TrustedHtml trusted) {
      return trusted.value();
    }
    return escape(str(value));
  }

  /** Converts a value to the string JavaScript would produce, before any escaping. */
  public static String str(Object value) {
    if (value == null || value instanceof Undefined) {
      return "";
    }
    if (value instanceof String s) {
      return s;
    }
    if (value instanceof TrustedHtml trusted) {
      return trusted.value();
    }
    if (value instanceof Boolean b) {
      return b ? "true" : "false";
    }
    if (value instanceof JsonNumber number) {
      return number.toJsString();
    }
    if (value instanceof List<?> list) {
      List<String> parts = new ArrayList<>(list.size());
      for (Object item : list) {
        parts.add(str(item));
      }
      return String.join(",", parts);
    }
    return "[object Object]";
  }

  /**
   * JavaScript truthiness. Notably an empty array and an empty object are both truthy.
   */
  public static boolean truthy(Object value) {
    if (value == null || value instanceof Undefined) {
      return false;
    }
    if (value instanceof Boolean b) {
      return b;
    }
    if (value instanceof String s) {
      return !s.isEmpty();
    }
    if (value instanceof JsonNumber number) {
      double n = number.doubleValue();
      return !Double.isNaN(n) && n != 0.0;
    }
    return true;
  }

  /** Whether an option was never supplied ({@code !== undefined}). */
  public static boolean isUndefined(Object value) {
    return value instanceof Undefined;
  }

  public static String trim(String text) {
    return text == null ? "" : text.trim();
  }

  /**
   * Nunjucks indent filter: prefix every line with {@code width} spaces, skipping the first line
   * unless {@code first} is true. An empty string is returned unchanged.
   */
  public static String indent(String text, int width, boolean first) {
    if (text == null || text.isEmpty()) {
      return text == null ? "" : text;
    }
    String padding = " ".repeat(width);
    String[] lines = text.split("\n", -1);
    for (int i = 0; i < lines.length; i++) {
      if (i == 0 && !first) {
        continue;
      }
      lines[i] = padding + lines[i];
    }
    return String.join("\n", lines);
  }

  /**
   * Nunjucks {@code default(fallback)}: fallback only when the option is undefined.
   */
  public static Object def(Object value, Object fallback) {
    if (isUndefined(value)) {
      return fallback;
    }
    return value;
  }

  /** Nunjucks {@code default(fallback, true)}: also replaces falsy values. */
  public static Object defTruthy(Object value, Object fallback) {
    if (truthy(value)) {
      return value;
    }
    return fallback;
  }

  /**
   * Nunjucks length filter: element count for arrays, key count for objects, character count for
   * strings.
   */
  public static int length(Object value) {
    if (value == null || value instanceof Undefined) {
      return 0;
    }
    if (value instanceof Boolean) {
      return 0;
    }
    if (value instanceof List<?> list) {
      return list.size();
    }
    if (value instanceof Params params) {
      return params.size();
    }
    if (value instanceof String s) {
      return s.length();
    }
    if (value instanceof TrustedHtml trusted) {
      return trusted.value().length();
    }
    return 0;
  }

  /**
   * Walks a chain of option names, returning {@link Undefined} as soon as the chain leaves an
   * object.
   */
  public static Object get(Object value, String... names) {
    Object current = value;
    for (String name : names) {
      if (!(current instanceof Params object)) {
        return Undefined.INSTANCE;
      }
      current = object.get(name);
    }
    return current;
  }

  /** Returns value as a list, or empty when it is not an array. */
  @SuppressWarnings("unchecked")
  public static List<Object> items(Object value) {
    if (value instanceof List<?> list) {
      return (List<Object>) list;
    }
    return List.of();
  }

  /** Returns the index'th element of an array option, or {@link Undefined} when out of range. */
  public static Object at(Object value, int index) {
    List<Object> list = items(value);
    if (index < 0 || index >= list.size()) {
      return Undefined.INSTANCE;
    }
    return list.get(index);
  }

  /** JavaScript {@code ==} for the value kinds the macros compare. */
  public static boolean looseEq(Object left, Object right) {
    boolean leftNil = left == null || isUndefined(left);
    boolean rightNil = right == null || isUndefined(right);
    if (leftNil || rightNil) {
      return leftNil && rightNil;
    }
    boolean leftIsBool = left instanceof Boolean;
    boolean rightIsBool = right instanceof Boolean;
    if (leftIsBool && rightIsBool) {
      return Objects.equals(left, right);
    }
    if (leftIsBool) {
      return looseEq(boolToNumber((Boolean) left), right);
    }
    if (rightIsBool) {
      return looseEq(left, boolToNumber((Boolean) right));
    }
    boolean leftIsNumber = left instanceof JsonNumber;
    boolean rightIsNumber = right instanceof JsonNumber;
    if (leftIsNumber && rightIsNumber) {
      return numeric(((JsonNumber) left).raw()) == numeric(((JsonNumber) right).raw());
    }
    if (leftIsNumber) {
      return sameNumber(((JsonNumber) left).raw(), str(right));
    }
    if (rightIsNumber) {
      return sameNumber(str(left), ((JsonNumber) right).raw());
    }
    return str(left).equals(str(right));
  }

  /** JavaScript {@code ===}. */
  public static boolean strictEq(Object left, Object right) {
    if (isUndefined(left) || isUndefined(right)) {
      return isUndefined(left) && isUndefined(right);
    }
    if (left == null || right == null) {
      return left == null && right == null;
    }
    boolean leftIsNumber = left instanceof JsonNumber;
    boolean rightIsNumber = right instanceof JsonNumber;
    if (leftIsNumber != rightIsNumber) {
      return false;
    }
    if (leftIsNumber) {
      return numeric(((JsonNumber) left).raw()) == numeric(((JsonNumber) right).raw());
    }
    if (left instanceof Boolean) {
      return right instanceof Boolean && left.equals(right);
    }
    if (left instanceof String) {
      return right instanceof String && left.equals(right);
    }
    return left == right;
  }

  /**
   * Nunjucks {@code in} operator: substring search for strings, strict element search for arrays,
   * and key lookup for objects.
   */
  public static boolean contains(Object needle, Object haystack) {
    if (haystack instanceof String s) {
      return s.contains(str(needle));
    }
    if (haystack instanceof TrustedHtml trusted) {
      return trusted.value().contains(str(needle));
    }
    if (haystack instanceof List<?> list) {
      for (Object item : list) {
        if (strictEq(needle, item)) {
          return true;
        }
      }
      return false;
    }
    if (haystack instanceof Params params) {
      return params.has(str(needle));
    }
    return false;
  }

  /**
   * Common {@code x.html | safe if x.html else x.text} choice: HTML is trusted, text is escaped.
   */
  public static String content(Object params, String htmlKey, String textKey) {
    Object html = get(params, htmlKey);
    if (truthy(html)) {
      return str(html);
    }
    return out(get(params, textKey));
  }

  /**
   * {@code x.html | safe | trim | indent(width) if x.html else x.text}.
   */
  public static String contentIndent(Object params, String htmlKey, String textKey, int width) {
    Object html = get(params, htmlKey);
    if (truthy(html)) {
      return indent(trim(str(html)), width, false);
    }
    return out(get(params, textKey));
  }

  private static JsonNumber boolToNumber(boolean value) {
    return new JsonNumber(value ? "1" : "0");
  }

  private static double numeric(String text) {
    try {
      return Double.parseDouble(text);
    } catch (NumberFormatException e) {
      return 0.0;
    }
  }

  private static boolean sameNumber(String left, String right) {
    String trimmed = right.trim();
    if (trimmed.isEmpty()) {
      trimmed = "0";
    }
    try {
      double value = Double.parseDouble(trimmed);
      return numeric(left) == value;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
