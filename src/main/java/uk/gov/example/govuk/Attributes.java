package uk.gov.example.govuk;

/**
 * Attribute and i18n helpers matching Nunjucks macros ({@code govukAttributes}, {@code
 * govukI18nAttributes}).
 */
public final class Attributes {

  private Attributes() {}

  /**
   * Renders the {@code attributes} option the way GOV.UK Frontend's private {@code
   * govukAttributes} macro does, returning HTML that already starts with a leading space when not
   * empty.
   */
  public static String attributes(Object value) {
    if (value instanceof String s) {
      return s;
    }
    if (value instanceof TrustedHtml trusted) {
      return trusted.value();
    }
    if (value instanceof Params typed) {
      StringBuilder out = new StringBuilder();
      for (String name : typed.keys()) {
        out.append(attribute(name, typed.get(name)));
      }
      return out.toString();
    }
    return "";
  }

  /** Renders a single entry of the {@code attributes} option. */
  public static String attribute(String name, Object item) {
    Object value = item;
    boolean optional = false;
    if (item instanceof Params options) {
      value = options.get("value");
      Object flag = options.get("optional");
      optional = flag instanceof Boolean b && b;
    }

    boolean empty = value == null || Nunjucks.isUndefined(value);
    String escaped = "";
    if (!empty) {
      if (value instanceof TrustedHtml safe) {
        escaped = safe.value();
      } else {
        escaped = Nunjucks.escape(Nunjucks.str(value));
      }
    }

    if (optional) {
      boolean isBool = value instanceof Boolean;
      boolean flag = isBool && (Boolean) value;
      if (isBool && flag) {
        return " " + Nunjucks.escape(name);
      }
      if (empty || (isBool && !flag)) {
        return "";
      }
    }
    return " " + Nunjucks.escape(name) + "=\"" + escaped + "\"";
  }

  /**
   * Renders translated text into {@code data-i18n.*} attributes. {@code messages} takes
   * precedence over {@code message}.
   */
  public static String i18nAttributes(String key, Object message, Object messages) {
    if (Nunjucks.truthy(messages)) {
      if (!(messages instanceof Params object)) {
        return "";
      }
      StringBuilder out = new StringBuilder();
      for (String rule : object.keys()) {
        out.append(" data-i18n.")
            .append(key)
            .append('.')
            .append(rule)
            .append("=\"")
            .append(Nunjucks.escape(Nunjucks.str(object.get(rule))))
            .append('"');
      }
      return out.toString();
    }
    if (Nunjucks.truthy(message)) {
      return " data-i18n." + key + "=\"" + Nunjucks.escape(Nunjucks.str(message)) + '"';
    }
    return "";
  }

  /** {@code name="value"} when the option is truthy. */
  public static String attributeIf(String name, Object value) {
    if (!Nunjucks.truthy(value)) {
      return "";
    }
    return " " + name + "=\"" + Nunjucks.out(value) + '"';
  }

  /** {@code some-class} when the option is truthy. */
  public static String classesIf(Object value) {
    if (!Nunjucks.truthy(value)) {
      return "";
    }
    return " " + Nunjucks.out(value);
  }

  /** Literal suffix when the option is truthy ({@code disabled}, conditional class names). */
  public static String flagIf(String suffix, Object value) {
    if (!Nunjucks.truthy(value)) {
      return "";
    }
    return suffix;
  }
}
