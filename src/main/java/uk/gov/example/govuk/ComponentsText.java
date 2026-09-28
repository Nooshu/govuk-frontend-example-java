package uk.gov.example.govuk;

/**
 * Ports for text components (inset-text, warning-text, tag, back-link, hint, …).
 */
final class ComponentsText {

  private ComponentsText() {}

  static String renderBackLink(Params p) {
    String text = Nunjucks.out(Nunjucks.defTruthy(p.get("text"), "Back"));
    Object html = p.get("html");
    if (Nunjucks.truthy(html)) {
      text = Nunjucks.str(html);
    }
    return "<a href=\""
        + Nunjucks.out(Nunjucks.defTruthy(p.get("href"), "#"))
        + "\" class=\"govuk-back-link"
        + Attributes.classesIf(p.get("classes"))
        + '"'
        + Attributes.attributes(p.get("attributes"))
        + ">"
        + text
        + "</a>";
  }

  static String renderSkipLink(Params p) {
    return "<a href=\""
        + Nunjucks.out(Nunjucks.defTruthy(p.get("href"), "#content"))
        + "\" class=\"govuk-skip-link"
        + Attributes.classesIf(p.get("classes"))
        + '"'
        + Attributes.attributes(p.get("attributes"))
        + " data-module=\"govuk-skip-link\">"
        + Nunjucks.content(p, "html", "text")
        + "</a>";
  }

  static String renderHint(Params p) {
    return "<div"
        + Attributes.attributeIf("id", p.get("id"))
        + " class=\"govuk-hint"
        + Attributes.classesIf(p.get("classes"))
        + '"'
        + Attributes.attributes(p.get("attributes"))
        + ">\n  "
        + Nunjucks.contentIndent(p, "html", "text", 2)
        + "\n</div>";
  }

  static String renderInsetText(Params p) {
    return "<div"
        + Attributes.attributeIf("id", p.get("id"))
        + " class=\"govuk-inset-text"
        + Attributes.classesIf(p.get("classes"))
        + '"'
        + Attributes.attributes(p.get("attributes"))
        + ">\n  "
        + Nunjucks.contentIndent(p, "html", "text", 2)
        + "\n</div>";
  }

  static String renderTag(Params p) {
    return "<strong class=\"govuk-tag"
        + Attributes.classesIf(p.get("classes"))
        + '"'
        + Attributes.attributes(p.get("attributes"))
        + ">\n  "
        + Nunjucks.contentIndent(p, "html", "text", 2)
        + "\n</strong>";
  }

  static String renderWarningText(Params p) {
    return "<div class=\"govuk-warning-text"
        + Attributes.classesIf(p.get("classes"))
        + '"'
        + Attributes.attributes(p.get("attributes"))
        + ">\n"
        + "  <span class=\"govuk-warning-text__icon\" aria-hidden=\"true\">!</span>\n"
        + "  <strong class=\"govuk-warning-text__text\">\n"
        + "    <span class=\"govuk-visually-hidden\">"
        + Nunjucks.out(Nunjucks.defTruthy(p.get("iconFallbackText"), "Warning"))
        + "</span>\n"
        + "    "
        + Nunjucks.content(p, "html", "text")
        + "\n"
        + "  </strong>\n</div>";
  }

  static String renderErrorMessage(Params p) {
    Object visuallyHidden = Nunjucks.def(p.get("visuallyHiddenText"), "Error");
    String message = Nunjucks.contentIndent(p, "html", "text", 2);

    StringBuilder out = new StringBuilder();
    out.append("<p")
        .append(Attributes.attributeIf("id", p.get("id")))
        .append(" class=\"govuk-error-message")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");
    if (Nunjucks.truthy(visuallyHidden)) {
      out.append("  <span class=\"govuk-visually-hidden\">")
          .append(Nunjucks.out(visuallyHidden))
          .append(":</span> ")
          .append(message)
          .append('\n');
    } else {
      out.append("  ").append(message).append('\n');
    }
    out.append("</p>");
    return out.toString();
  }

  static String renderDetails(Params p) {
    return "<details"
        + Attributes.attributeIf("id", p.get("id"))
        + " class=\"govuk-details"
        + Attributes.classesIf(p.get("classes"))
        + '"'
        + Attributes.attributes(p.get("attributes"))
        + Attributes.flagIf(" open", p.get("open"))
        + ">\n"
        + "  <summary class=\"govuk-details__summary\">\n"
        + "    <span class=\"govuk-details__summary-text\">\n"
        + "      "
        + Nunjucks.contentIndent(p, "summaryHtml", "summaryText", 6)
        + "\n"
        + "    </span>\n  </summary>\n"
        + "  <div class=\"govuk-details__text\">\n"
        + "    "
        + Nunjucks.content(p, "html", "text")
        + "\n"
        + "  </div>\n</details>";
  }

  static String renderLabel(Params p) {
    if (!Nunjucks.truthy(p.get("html")) && !Nunjucks.truthy(p.get("text"))) {
      return "";
    }
    String label =
        "<label class=\"govuk-label"
            + Attributes.classesIf(p.get("classes"))
            + '"'
            + Attributes.attributes(p.get("attributes"))
            + Attributes.attributeIf("for", p.get("for"))
            + ">\n  "
            + Nunjucks.contentIndent(p, "html", "text", 2)
            + "\n</label>\n";

    if (Nunjucks.truthy(p.get("isPageHeading"))) {
      return "<h1 class=\"govuk-label-wrapper\">\n  "
          + Nunjucks.indent(Nunjucks.trim(label), 2, false)
          + "\n</h1>\n";
    }
    return Nunjucks.trim(label) + "\n";
  }

  static String renderPanel(Params p) {
    Object classes = p.get("classes");
    boolean interruption =
        Nunjucks.truthy(classes) && Nunjucks.contains("govuk-panel--interruption", classes);
    String level = Render.heading(p.get("headingLevel"), "1");

    StringBuilder out = new StringBuilder();
    out.append("<div class=\"govuk-panel");
    if (!interruption) {
      out.append(" govuk-panel--confirmation");
    }
    out.append(Attributes.classesIf(classes))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");
    out.append("  <h")
        .append(level)
        .append(" class=\"govuk-panel__title\">\n    ")
        .append(Nunjucks.content(p, "titleHtml", "titleText"))
        .append("\n  </h")
        .append(level)
        .append(">\n");

    if (Nunjucks.truthy(p.get("html")) || Nunjucks.truthy(p.get("text"))) {
      out.append("  <div class=\"govuk-panel__body\">\n    ")
          .append(Nunjucks.contentIndent(p, "html", "text", 4))
          .append("\n  </div>\n");
    }

    Object actions = p.get("actions");
    if (interruption && Nunjucks.truthy(actions)) {
      out.append("  <div class=\"govuk-panel__actions")
          .append(Attributes.classesIf(Nunjucks.get(actions, "classes")))
          .append('"')
          .append(Attributes.attributes(Nunjucks.get(actions, "attributes")))
          .append('>');
      var entries = Nunjucks.items(Nunjucks.get(actions, "items"));
      if (!entries.isEmpty()) {
        out.append("<div class=\"govuk-button-group\">\n");
        for (Object action : entries) {
          out.append("      ")
              .append(Nunjucks.indent(Nunjucks.trim(panelAction(action)), 6, false))
              .append('\n');
        }
        out.append("    </div>");
      }
      out.append("</div>\n");
    }

    out.append("</div>");
    return out.toString();
  }

  private static String panelAction(Object action) {
    Object href = Nunjucks.get(action, "href");
    if (!Nunjucks.truthy(href) || "button".equals(Nunjucks.str(Nunjucks.get(action, "type")))) {
      return ComponentsButton.renderButton(
          Params.of(
              "text",
              Nunjucks.get(action, "text"),
              "type",
              Nunjucks.defTruthy(Nunjucks.get(action, "type"), "button"),
              "classes",
              "govuk-button--inverse" + Render.concatIf(" ", Nunjucks.get(action, "classes")),
              "href",
              href,
              "attributes",
              Nunjucks.get(action, "attributes")));
    }
    return "<a class=\"govuk-link govuk-link--inverse"
        + Attributes.classesIf(Nunjucks.get(action, "classes"))
        + "\" href=\""
        + Nunjucks.out(href)
        + '"'
        + Attributes.attributes(Nunjucks.get(action, "attributes"))
        + ">"
        + Nunjucks.out(Nunjucks.get(action, "text"))
        + "</a>";
  }

  static String renderPhaseBanner(Params p) {
    Object tag = p.get("tag");
    String tagHtml =
        renderTag(
            Params.of(
                "text",
                Nunjucks.get(tag, "text"),
                "html",
                Nunjucks.get(tag, "html"),
                "classes",
                "govuk-phase-banner__content__tag"
                    + Render.concatIf(" ", Nunjucks.get(tag, "classes"))));
    return "<div class=\"govuk-phase-banner govuk-width-container"
        + Attributes.classesIf(p.get("classes"))
        + '"'
        + Attributes.attributes(p.get("attributes"))
        + ">\n"
        + "  <p class=\"govuk-phase-banner__content\">\n"
        + "    "
        + Nunjucks.indent(Nunjucks.trim(tagHtml), 4, false)
        + "\n"
        + "    <span class=\"govuk-phase-banner__text\">\n"
        + "      "
        + Nunjucks.contentIndent(p, "html", "text", 6)
        + "\n"
        + "    </span>\n  </p>\n</div>";
  }

  static String renderFeedback(Params p) {
    String level = Render.heading(p.get("headingLevel"), "2");

    StringBuilder out = new StringBuilder();
    out.append("<div class=\"govuk-feedback govuk-width-container")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");
    out.append("  <div class=\"govuk-grid-row\">\n");
    out.append("    <div class=\"govuk-grid-column-two-thirds\">\n");
    out.append("      <h")
        .append(level)
        .append(" class=\"govuk-feedback__title\">\n        ")
        .append(Nunjucks.content(p, "titleHtml", "titleText"))
        .append("\n      </h")
        .append(level)
        .append(">\n");

    Object html = p.get("html");
    Object text = p.get("text");
    if (Nunjucks.truthy(html) || Nunjucks.truthy(text)) {
      out.append("        <div class=\"govuk-feedback__body\">\n");
      if (Nunjucks.truthy(html)) {
        out.append("            ")
            .append(Nunjucks.indent(Nunjucks.trim(Nunjucks.str(html)), 4, false))
            .append('\n');
      } else if (Nunjucks.truthy(text)) {
        out.append("            <p class=\"govuk-body\">\n");
        out.append("              ")
            .append(Nunjucks.escape(Nunjucks.indent(Nunjucks.trim(Nunjucks.str(text)), 6, false)))
            .append('\n');
        out.append("            </p>\n");
      }
      out.append("        </div>\n");
    }

    out.append("    </div>\n  </div>\n</div>");
    return out.toString();
  }

  static String renderFieldset(Params p) {
    StringBuilder out = new StringBuilder();
    out.append("<fieldset class=\"govuk-fieldset")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributeIf("role", p.get("role")))
        .append(Attributes.attributeIf("aria-describedby", p.get("describedBy")))
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");

    Object legend = p.get("legend");
    if (Nunjucks.truthy(Nunjucks.get(legend, "html"))
        || Nunjucks.truthy(Nunjucks.get(legend, "text"))) {
      out.append("  <legend class=\"govuk-fieldset__legend")
          .append(Attributes.classesIf(Nunjucks.get(legend, "classes")))
          .append("\">\n");
      if (Nunjucks.truthy(Nunjucks.get(legend, "isPageHeading"))) {
        out.append("    <h1 class=\"govuk-fieldset__heading\">\n");
        out.append("      ")
            .append(Nunjucks.contentIndent(legend, "html", "text", 6))
            .append('\n');
        out.append("    </h1>\n");
      } else {
        out.append("    ").append(Nunjucks.contentIndent(legend, "html", "text", 4)).append('\n');
      }
      out.append("  </legend>\n");
    }

    Object html = p.get("html");
    if (Nunjucks.truthy(html)) {
      out.append("  ").append(Nunjucks.str(html)).append('\n');
    }

    out.append("</fieldset>");
    return out.toString();
  }
}
