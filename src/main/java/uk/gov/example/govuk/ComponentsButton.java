package uk.gov.example.govuk;

/**
 * Ports for button-related Frontend components ({@code button}, {@code exit-this-page}).
 */
final class ComponentsButton {

  private ComponentsButton() {}

  /**
   * Arrow GOV.UK Frontend appends to a start button. The leading newline is load-bearing for
   * fixture parity.
   */
  private static final String START_ICON =
      "\n"
          + "  <svg class=\"govuk-button__start-icon\" xmlns=\"http://www.w3.org/2000/svg\""
          + " width=\"17.5\" height=\"19\" viewBox=\"0 0 33 40\" aria-hidden=\"true\""
          + " focusable=\"false\">\n"
          + "    <path fill=\"currentColor\" d=\"M0 0h13l20 20-20 20H0l20-20z\"/>\n"
          + "  </svg>";

  /**
   * Default exit-this-page button label. Trailing newline matches the captured {@code {% set %}}
   * block upstream.
   */
  private static final String EXIT_THIS_PAGE_DEFAULT_HTML =
      "  <span class=\"govuk-visually-hidden\">Emergency</span> Exit this page\n";

  static String renderButton(Params p) {
    String classNames = "govuk-button";
    Object classes = p.get("classes");
    if (Nunjucks.truthy(classes)) {
      classNames += " " + Nunjucks.str(classes);
    }
    boolean startButton = Nunjucks.truthy(p.get("isStartButton"));
    if (startButton) {
      classNames += " govuk-button--start";
    }

    String commonAttributes =
        " class=\""
            + Nunjucks.escape(classNames)
            + "\" data-module=\"govuk-button\""
            + Attributes.attributes(p.get("attributes"))
            + Attributes.attributeIf("id", p.get("id"));

    Object html = p.get("html");
    String text = Nunjucks.out(p.get("text"));
    if (Nunjucks.truthy(html) && startButton) {
      text = "<span>" + Nunjucks.trim(Nunjucks.str(html)) + "</span>";
    } else if (Nunjucks.truthy(html)) {
      text = Nunjucks.trim(Nunjucks.str(html));
    }

    StringBuilder out = new StringBuilder();
    if (Nunjucks.truthy(p.get("href"))) {
      out.append("<a href=\"")
          .append(Nunjucks.out(p.get("href")))
          .append("\" role=\"button\" draggable=\"false\"")
          .append(commonAttributes)
          .append(">\n  ")
          .append(Nunjucks.indent(text, 2, false));
    } else {
      out.append("<button type=\"")
          .append(Nunjucks.out(Nunjucks.defTruthy(p.get("type"), "submit")))
          .append('"')
          .append(Attributes.attributeIf("value", p.get("value")))
          .append(Attributes.attributeIf("name", p.get("name")))
          .append(Attributes.flagIf(" disabled aria-disabled=\"true\"", p.get("disabled")));
      Object preventDoubleClick = p.get("preventDoubleClick");
      if (!Nunjucks.isUndefined(preventDoubleClick)) {
        out.append(" data-prevent-double-click=\"")
            .append(Nunjucks.out(preventDoubleClick))
            .append('"');
      }
      out.append(commonAttributes)
          .append(">\n  ")
          .append(Nunjucks.indent(text, 2, false));
    }
    if (startButton) {
      out.append(START_ICON);
    }
    if (Nunjucks.truthy(p.get("href"))) {
      out.append("\n</a>");
    } else {
      out.append("\n</button>");
    }
    return out.toString();
  }

  static String renderExitThisPage(Params p) {
    Object html = p.get("html");
    if (!Nunjucks.truthy(html) && !Nunjucks.truthy(p.get("text"))) {
      html = new TrustedHtml(EXIT_THIS_PAGE_DEFAULT_HTML);
    }

    String button =
        renderButton(
            Params.of(
                "html",
                html,
                "text",
                p.get("text"),
                "classes",
                "govuk-button--warning govuk-exit-this-page__button govuk-js-exit-this-page-button",
                "href",
                Nunjucks.defTruthy(p.get("redirectUrl"), "https://www.bbc.co.uk/weather"),
                "attributes",
                Params.of("rel", "nofollow noreferrer")));

    return "<div"
        + Attributes.attributeIf("id", p.get("id"))
        + " class=\"govuk-exit-this-page"
        + Attributes.classesIf(p.get("classes"))
        + "\" data-module=\"govuk-exit-this-page\""
        + Attributes.attributes(p.get("attributes"))
        + Attributes.attributeIf("data-i18n.activated", p.get("activatedText"))
        + Attributes.attributeIf("data-i18n.timed-out", p.get("timedOutText"))
        + Attributes.attributeIf("data-i18n.press-two-more-times", p.get("pressTwoMoreTimesText"))
        + Attributes.attributeIf("data-i18n.press-one-more-time", p.get("pressOneMoreTimeText"))
        + ">\n  "
        + Nunjucks.indent(Nunjucks.trim(button), 2, false)
        + "\n</div>";
  }
}
