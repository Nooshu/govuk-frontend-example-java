package uk.gov.example.govuk;

/**
 * Ports for list-like components (table, task-list, summary-list, accordion, …).
 */
final class ComponentsLists {

  private ComponentsLists() {}

  static String renderAccordion(Params p) {
    StringBuilder out = new StringBuilder();
    out.append("<div class=\"govuk-accordion")
        .append(Attributes.classesIf(p.get("classes")))
        .append("\" data-module=\"govuk-accordion\" id=\"")
        .append(Nunjucks.out(p.get("id")))
        .append('"')
        .append(
            Attributes.i18nAttributes(
                "hide-all-sections", p.get("hideAllSectionsText"), Undefined.INSTANCE))
        .append(
            Attributes.i18nAttributes(
                "hide-section", p.get("hideSectionText"), Undefined.INSTANCE))
        .append(
            Attributes.i18nAttributes(
                "hide-section-aria-label",
                p.get("hideSectionAriaLabelText"),
                Undefined.INSTANCE))
        .append(
            Attributes.i18nAttributes(
                "show-all-sections", p.get("showAllSectionsText"), Undefined.INSTANCE))
        .append(
            Attributes.i18nAttributes(
                "show-section", p.get("showSectionText"), Undefined.INSTANCE))
        .append(
            Attributes.i18nAttributes(
                "show-section-aria-label",
                p.get("showSectionAriaLabelText"),
                Undefined.INSTANCE));
    Object remember = p.get("rememberExpanded");
    if (!Nunjucks.isUndefined(remember)) {
      out.append(" data-remember-expanded=\"")
          .append(Nunjucks.escape(Nunjucks.str(remember)))
          .append('"');
    }
    out.append(Attributes.attributes(p.get("attributes"))).append(">\n");

    int index = 0;
    for (Object item : Nunjucks.items(p.get("items"))) {
      index++;
      if (!Nunjucks.truthy(item)) {
        continue;
      }
      out.append(accordionItem(p, item, index));
    }

    out.append("</div>");
    return out.toString();
  }

  static String accordionItem(Params p, Object item, int index) {
    String level = Render.heading(p.get("headingLevel"), "2");
    String id = Nunjucks.out(p.get("id"));
    String position = Integer.toString(index);
    Object itemHeading = Nunjucks.get(item, "heading");
    Object summary = Nunjucks.get(item, "summary");
    Object itemContent = Nunjucks.get(item, "content");

    StringBuilder out = new StringBuilder();
    out.append("  <div class=\"govuk-accordion__section")
        .append(
            Attributes.flagIf(
                " govuk-accordion__section--expanded", Nunjucks.get(item, "expanded")))
        .append("\">\n");
    out.append("    <div class=\"govuk-accordion__section-header\">\n");
    out.append("      <h")
        .append(level)
        .append(" class=\"govuk-accordion__section-heading\">\n");
    out.append("        <span class=\"govuk-accordion__section-button\" id=\"")
        .append(id)
        .append("-heading-")
        .append(position)
        .append("\">\n");
    out.append("          ")
        .append(Nunjucks.contentIndent(itemHeading, "html", "text", 8))
        .append('\n');
    out.append("        </span>\n      </h").append(level).append(">\n");
    if (Nunjucks.truthy(Nunjucks.get(summary, "html"))
        || Nunjucks.truthy(Nunjucks.get(summary, "text"))) {
      out.append("      <div class=\"govuk-accordion__section-summary govuk-body\" id=\"")
          .append(id)
          .append("-summary-")
          .append(position)
          .append("\">\n");
      out.append("        ")
          .append(Nunjucks.contentIndent(summary, "html", "text", 8))
          .append('\n');
      out.append("      </div>\n");
    }
    out.append("    </div>\n");
    out.append("    <div id=\"")
        .append(id)
        .append("-content-")
        .append(position)
        .append("\" class=\"govuk-accordion__section-content\">\n");
    Object html = Nunjucks.get(itemContent, "html");
    Object text = Nunjucks.get(itemContent, "text");
    if (Nunjucks.truthy(html)) {
      out.append("      ")
          .append(Nunjucks.indent(Nunjucks.trim(Nunjucks.str(html)), 6, false))
          .append('\n');
    } else if (Nunjucks.truthy(text)) {
      out.append("      <p class=\"govuk-body\">\n");
      out.append("        ")
          .append(Nunjucks.escape(Nunjucks.indent(Nunjucks.trim(Nunjucks.str(text)), 8, false)))
          .append('\n');
      out.append("      </p>\n");
    }
    out.append("    </div>\n  </div>\n");
    return out.toString();
  }

  static String renderErrorSummary(Params p) {
    StringBuilder out = new StringBuilder();
    out.append("<div class=\"govuk-error-summary")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"');
    Object autoFocus = p.get("disableAutoFocus");
    if (!Nunjucks.isUndefined(autoFocus)) {
      out.append(" data-disable-auto-focus=\"").append(Nunjucks.out(autoFocus)).append('"');
    }
    out.append(Attributes.attributes(p.get("attributes")))
        .append(" data-module=\"govuk-error-summary\">");

    out.append("\n  <div role=\"alert\">\n");
    out.append("    <h2 class=\"govuk-error-summary__title\">\n");
    out.append("      ")
        .append(Nunjucks.contentIndent(p, "titleHtml", "titleText", 6))
        .append('\n');
    out.append("    </h2>\n");
    out.append("    <div class=\"govuk-error-summary__body\">\n");

    if (Nunjucks.truthy(p.get("descriptionHtml")) || Nunjucks.truthy(p.get("descriptionText"))) {
      out.append("      <p>\n        ")
          .append(Nunjucks.contentIndent(p, "descriptionHtml", "descriptionText", 8))
          .append("\n      </p>\n");
    }

    var errorList = Nunjucks.items(p.get("errorList"));
    if (!errorList.isEmpty()) {
      out.append("        <ul class=\"govuk-list govuk-error-summary__list\">\n");
      for (Object item : errorList) {
        out.append("          <li>\n");
        Object href = Nunjucks.get(item, "href");
        if (Nunjucks.truthy(href)) {
          out.append("            <a href=\"")
              .append(Nunjucks.out(href))
              .append('"')
              .append(Attributes.attributes(Nunjucks.get(item, "attributes")))
              .append('>')
              .append(Nunjucks.contentIndent(item, "html", "text", 12))
              .append("</a>\n");
        } else {
          out.append("            ")
              .append(Nunjucks.contentIndent(item, "html", "text", 10))
              .append('\n');
        }
        out.append("          </li>\n");
      }
      out.append("        </ul>\n");
    }

    out.append("    </div>\n  </div>\n</div>");
    return out.toString();
  }

  static String renderNotificationBanner(Params p) {
    boolean success = "success".equals(Nunjucks.str(p.get("type")));
    String typeClass = "";
    if (success) {
      typeClass = " govuk-notification-banner--" + Nunjucks.escape(Nunjucks.str(p.get("type")));
    }

    String role = "region";
    if (Nunjucks.truthy(p.get("role"))) {
      role = Nunjucks.str(p.get("role"));
    } else if (success) {
      role = "alert";
    }

    String title;
    if (Nunjucks.truthy(p.get("titleHtml"))) {
      title = Nunjucks.str(p.get("titleHtml"));
    } else if (Nunjucks.truthy(p.get("titleText"))) {
      title = Nunjucks.out(p.get("titleText"));
    } else if (success) {
      title = "Success";
    } else {
      title = "Important";
    }

    String titleId =
        Nunjucks.out(Nunjucks.defTruthy(p.get("titleId"), "govuk-notification-banner-title"));
    String level = Nunjucks.out(Nunjucks.defTruthy(p.get("titleHeadingLevel"), "2"));

    StringBuilder out = new StringBuilder();
    out.append("<div class=\"govuk-notification-banner")
        .append(typeClass)
        .append(Attributes.classesIf(p.get("classes")))
        .append("\" role=\"")
        .append(Nunjucks.escape(role))
        .append("\" aria-labelledby=\"")
        .append(titleId)
        .append("\" data-module=\"govuk-notification-banner\"");
    Object autoFocus = p.get("disableAutoFocus");
    if (!Nunjucks.isUndefined(autoFocus)) {
      out.append(" data-disable-auto-focus=\"").append(Nunjucks.out(autoFocus)).append('"');
    }
    out.append(Attributes.attributes(p.get("attributes"))).append(">\n");
    out.append("  <div class=\"govuk-notification-banner__header\">\n");
    out.append("    <h")
        .append(level)
        .append(" class=\"govuk-notification-banner__title\" id=\"")
        .append(titleId)
        .append("\">\n");
    out.append("      ").append(title).append("\n    </h").append(level).append(">\n  </div>\n");
    out.append("  <div class=\"govuk-notification-banner__content\">\n");
    Object html = p.get("html");
    Object text = p.get("text");
    if (Nunjucks.truthy(html)) {
      out.append("    ")
          .append(Nunjucks.indent(Nunjucks.trim(Nunjucks.str(html)), 4, false))
          .append('\n');
    } else if (Nunjucks.truthy(text)) {
      out.append("    <p class=\"govuk-notification-banner__heading\">\n");
      out.append("      ")
          .append(Nunjucks.escape(Nunjucks.indent(Nunjucks.trim(Nunjucks.str(text)), 6, false)))
          .append('\n');
      out.append("    </p>\n");
    }
    out.append("  </div>\n</div>");
    return out.toString();
  }

  static String renderSummaryList(Params p) {
    Object card = p.get("card");
    Object cardTitle = Nunjucks.get(card, "title");

    boolean anyRowHasActions = false;
    for (Object row : Nunjucks.items(p.get("rows"))) {
      if (Nunjucks.length(Nunjucks.get(row, "actions", "items")) > 0) {
        anyRowHasActions = true;
      }
    }

    StringBuilder list = new StringBuilder();
    list.append("<dl class=\"govuk-summary-list")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");
    for (Object row : Nunjucks.items(p.get("rows"))) {
      if (!Nunjucks.truthy(row)) {
        continue;
      }
      Object key = Nunjucks.get(row, "key");
      Object value = Nunjucks.get(row, "value");
      Object actions = Nunjucks.get(row, "actions");
      list.append("  <div class=\"govuk-summary-list__row")
          .append(
              Attributes.flagIf(
                  " govuk-summary-list__row--no-actions",
                  anyRowHasActions && !Nunjucks.truthy(Nunjucks.get(actions, "items"))))
          .append(Attributes.classesIf(Nunjucks.get(row, "classes")))
          .append("\">\n");
      list.append("    <dt class=\"govuk-summary-list__key")
          .append(Attributes.classesIf(Nunjucks.get(key, "classes")))
          .append("\">\n      ")
          .append(Nunjucks.contentIndent(key, "html", "text", 6))
          .append("\n    </dt>\n");
      list.append("    <dd class=\"govuk-summary-list__value")
          .append(Attributes.classesIf(Nunjucks.get(value, "classes")))
          .append("\">\n      ")
          .append(Nunjucks.contentIndent(value, "html", "text", 6))
          .append("\n    </dd>\n");

      var entries = Nunjucks.items(Nunjucks.get(actions, "items"));
      if (!entries.isEmpty()) {
        list.append("    <dd class=\"govuk-summary-list__actions")
            .append(Attributes.classesIf(Nunjucks.get(actions, "classes")))
            .append("\">\n");
        if (entries.size() == 1) {
          list.append(Nunjucks.indent(Nunjucks.trim(summaryActionLink(entries.get(0), cardTitle)), 6, true))
              .append('\n');
        } else {
          list.append("      <ul class=\"govuk-summary-list__actions-list\">\n");
          for (Object action : entries) {
            list.append("        <li class=\"govuk-summary-list__actions-list-item\">\n");
            list.append("          ")
                .append(
                    Nunjucks.indent(
                        Nunjucks.trim(summaryActionLink(action, cardTitle)), 8, false))
                .append('\n');
            list.append("        </li>\n");
          }
          list.append("      </ul>\n");
        }
        list.append("    </dd>\n");
      }
      list.append("  </div>\n");
    }
    list.append("</dl>");

    if (Nunjucks.truthy(card)) {
      return summaryCard(card, Nunjucks.indent(Nunjucks.trim(list.toString()), 4, false));
    }
    return Nunjucks.trim(list.toString());
  }

  static String summaryActionLink(Object action, Object cardTitle) {
    StringBuilder out = new StringBuilder();
    out.append("  <a class=\"govuk-link")
        .append(Attributes.classesIf(Nunjucks.get(action, "classes")))
        .append("\" href=\"")
        .append(Nunjucks.out(Nunjucks.get(action, "href")))
        .append('"')
        .append(Attributes.attributes(Nunjucks.get(action, "attributes")))
        .append('>');
    Object html = Nunjucks.get(action, "html");
    if (Nunjucks.truthy(html)) {
      out.append(Nunjucks.indent(Nunjucks.str(html), 4, false));
    } else {
      out.append(Nunjucks.out(Nunjucks.get(action, "text")));
    }
    Object visuallyHidden = Nunjucks.get(action, "visuallyHiddenText");
    if (Nunjucks.truthy(visuallyHidden) || Nunjucks.truthy(cardTitle)) {
      out.append("<span class=\"govuk-visually-hidden\">");
      if (Nunjucks.truthy(visuallyHidden)) {
        out.append(' ').append(Nunjucks.out(visuallyHidden));
      }
      if (Nunjucks.truthy(cardTitle)) {
        String title = Nunjucks.out(Nunjucks.get(cardTitle, "text"));
        Object titleHtml = Nunjucks.get(cardTitle, "html");
        if (Nunjucks.truthy(titleHtml)) {
          title = Nunjucks.indent(Nunjucks.str(titleHtml), 6, false);
        }
        out.append(" (").append(title).append(')');
      }
      out.append("</span>");
    }
    out.append("</a>\n");
    return out.toString();
  }

  static String summaryCard(Object card, String body) {
    Object title = Nunjucks.get(card, "title");
    String level = Render.heading(Nunjucks.get(title, "headingLevel"), "2");
    Object actions = Nunjucks.get(card, "actions");

    StringBuilder out = new StringBuilder();
    out.append("<div class=\"govuk-summary-card")
        .append(Attributes.classesIf(Nunjucks.get(card, "classes")))
        .append('"')
        .append(Attributes.attributes(Nunjucks.get(card, "attributes")))
        .append(">\n");
    out.append("  <div class=\"govuk-summary-card__title-wrapper\">\n");
    if (Nunjucks.truthy(title)) {
      out.append("    <h")
          .append(level)
          .append(" class=\"govuk-summary-card__title")
          .append(Attributes.classesIf(Nunjucks.get(title, "classes")))
          .append("\">\n      ")
          .append(Nunjucks.contentIndent(title, "html", "text", 6))
          .append("\n    </h")
          .append(level)
          .append(">\n");
    }
    var entries = Nunjucks.items(Nunjucks.get(actions, "items"));
    if (!entries.isEmpty()) {
      if (entries.size() == 1) {
        out.append("    <div class=\"govuk-summary-card__actions")
            .append(Attributes.classesIf(Nunjucks.get(actions, "classes")))
            .append("\">\n");
        out.append("      ")
            .append(
                Nunjucks.indent(Nunjucks.trim(summaryActionLink(entries.get(0), title)), 4, false))
            .append('\n');
        out.append("    </div>\n");
      } else {
        out.append("    <ul class=\"govuk-summary-card__actions")
            .append(Attributes.classesIf(Nunjucks.get(actions, "classes")))
            .append("\">\n");
        for (Object action : entries) {
          out.append("      <li class=\"govuk-summary-card__action\">\n");
          out.append("        ")
              .append(
                  Nunjucks.indent(Nunjucks.trim(summaryActionLink(action, title)), 8, false))
              .append('\n');
          out.append("      </li>\n");
        }
        out.append("    </ul>\n");
      }
    }
    out.append("  </div>\n\n");
    out.append("  <div class=\"govuk-summary-card__content\">\n    ")
        .append(body)
        .append("\n  </div>\n</div>\n");
    return out.toString();
  }

  static String renderTable(Params p) {
    StringBuilder out = new StringBuilder();
    out.append("<table class=\"govuk-table")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");

    Object caption = p.get("caption");
    if (Nunjucks.truthy(caption)) {
      out.append("  <caption class=\"govuk-table__caption")
          .append(Attributes.classesIf(p.get("captionClasses")))
          .append("\">")
          .append(Nunjucks.out(caption))
          .append("</caption>\n");
    }

    var head = Nunjucks.items(p.get("head"));
    if (Nunjucks.truthy(p.get("head"))) {
      out.append("  <thead class=\"govuk-table__head\">\n");
      out.append("    <tr class=\"govuk-table__row\">\n");
      for (Object item : head) {
        out.append("      <th scope=\"col\" class=\"govuk-table__header")
            .append(formatClass("govuk-table__header--", Nunjucks.get(item, "format")))
            .append(Attributes.classesIf(Nunjucks.get(item, "classes")))
            .append('"')
            .append(Attributes.attributeIf("colspan", Nunjucks.get(item, "colspan")))
            .append(Attributes.attributeIf("rowspan", Nunjucks.get(item, "rowspan")))
            .append(Attributes.attributes(Nunjucks.get(item, "attributes")))
            .append('>')
            .append(Nunjucks.content(item, "html", "text"))
            .append("</th>\n");
      }
      out.append("    </tr>\n  </thead>\n");
    }

    out.append("  <tbody class=\"govuk-table__body\">\n");
    for (Object row : Nunjucks.items(p.get("rows"))) {
      if (!Nunjucks.truthy(row)) {
        continue;
      }
      out.append("    <tr class=\"govuk-table__row\">\n");
      int index = 0;
      for (Object cell : Nunjucks.items(row)) {
        String common =
            Attributes.attributeIf("colspan", Nunjucks.get(cell, "colspan"))
                + Attributes.attributeIf("rowspan", Nunjucks.get(cell, "rowspan"))
                + Attributes.attributes(Nunjucks.get(cell, "attributes"));
        if (index == 0 && Nunjucks.truthy(p.get("firstCellIsHeader"))) {
          out.append("      <th scope=\"row\" class=\"govuk-table__header")
              .append(Attributes.classesIf(Nunjucks.get(cell, "classes")))
              .append('"')
              .append(common)
              .append('>')
              .append(Nunjucks.content(cell, "html", "text"))
              .append("</th>\n");
        } else {
          out.append("      <td class=\"govuk-table__cell")
              .append(formatClass("govuk-table__cell--", Nunjucks.get(cell, "format")))
              .append(Attributes.classesIf(Nunjucks.get(cell, "classes")))
              .append('"')
              .append(common)
              .append('>')
              .append(Nunjucks.content(cell, "html", "text"))
              .append("</td>\n");
        }
        index++;
      }
      out.append("    </tr>\n");
    }
    out.append("  </tbody>\n</table>");
    return out.toString();
  }

  static String formatClass(String prefix, Object format) {
    if (!Nunjucks.truthy(format)) {
      return "";
    }
    return " " + prefix + Nunjucks.out(format);
  }

  static String renderTabs(Params p) {
    String idPrefix = "";
    Object prefix = p.get("idPrefix");
    if (Nunjucks.truthy(prefix)) {
      idPrefix = Nunjucks.str(prefix);
    }

    StringBuilder out = new StringBuilder();
    out.append("<div")
        .append(Attributes.attributeIf("id", p.get("id")))
        .append(" class=\"govuk-tabs")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(" data-module=\"govuk-tabs\">\n");
    out.append("  <h2 class=\"govuk-tabs__title\">\n    ")
        .append(Nunjucks.out(Nunjucks.def(p.get("title"), "Contents")))
        .append("\n  </h2>\n");

    var entries = Nunjucks.items(p.get("items"));
    if (!entries.isEmpty()) {
      out.append("  <ul class=\"govuk-tabs__list\">\n");
      int index = 0;
      for (Object item : entries) {
        index++;
        if (!Nunjucks.truthy(item)) {
          continue;
        }
        out.append(Nunjucks.indent(Nunjucks.trim(tabListItem(item, index, idPrefix)), 4, true))
            .append('\n');
      }
      out.append("  </ul>\n");
      index = 0;
      for (Object item : entries) {
        index++;
        if (!Nunjucks.truthy(item)) {
          continue;
        }
        out.append(Nunjucks.indent(Nunjucks.trim(tabPanel(item, index, idPrefix)), 2, true))
            .append('\n');
      }
    }

    out.append("</div>");
    return out.toString();
  }

  static String tabPanelId(Object item, int index, String idPrefix) {
    Object id = Nunjucks.get(item, "id");
    if (Nunjucks.truthy(id)) {
      return Nunjucks.str(id);
    }
    return idPrefix + "-" + index;
  }

  static String tabListItem(Object item, int index, String idPrefix) {
    return "<li class=\"govuk-tabs__list-item"
        + Attributes.flagIf(" govuk-tabs__list-item--selected", index == 1)
        + "\">\n"
        + "  <a class=\"govuk-tabs__tab\" href=\"#"
        + Nunjucks.escape(tabPanelId(item, index, idPrefix))
        + '"'
        + Attributes.attributes(Nunjucks.get(item, "attributes"))
        + ">\n    "
        + Nunjucks.out(Nunjucks.get(item, "label"))
        + "\n  </a>\n</li>\n";
  }

  static String tabPanel(Object item, int index, String idPrefix) {
    Object panel = Nunjucks.get(item, "panel");
    StringBuilder out = new StringBuilder();
    out.append("<div class=\"govuk-tabs__panel")
        .append(Attributes.flagIf(" govuk-tabs__panel--hidden", index > 1))
        .append("\" id=\"")
        .append(Nunjucks.escape(tabPanelId(item, index, idPrefix)))
        .append('"')
        .append(Attributes.attributes(Nunjucks.get(panel, "attributes")))
        .append(">\n");
    Object html = Nunjucks.get(panel, "html");
    Object text = Nunjucks.get(panel, "text");
    if (Nunjucks.truthy(html)) {
      out.append("  ")
          .append(Nunjucks.indent(Nunjucks.trim(Nunjucks.str(html)), 2, false))
          .append('\n');
    } else if (Nunjucks.truthy(text)) {
      out.append("  <p class=\"govuk-body\">").append(Nunjucks.out(text)).append("</p>\n");
    }
    out.append("</div>\n");
    return out.toString();
  }

  static String renderTaskList(Params p) {
    String idPrefix = "task-list";
    Object prefix = p.get("idPrefix");
    if (Nunjucks.truthy(prefix)) {
      idPrefix = Nunjucks.str(prefix);
    }

    StringBuilder out = new StringBuilder();
    out.append("<ul class=\"govuk-task-list")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");
    int index = 0;
    for (Object item : Nunjucks.items(p.get("items"))) {
      index++;
      if (Nunjucks.truthy(item)) {
        out.append(taskListItem(item, index, idPrefix)).append('\n');
      } else {
        out.append('\n');
      }
    }
    out.append("</ul>");
    return out.toString();
  }

  static String taskListItem(Object item, int index, String idPrefix) {
    String position = Integer.toString(index);
    String hintId = idPrefix + "-" + position + "-hint";
    String statusId = idPrefix + "-" + position + "-status";
    Object title = Nunjucks.get(item, "title");
    Object hint = Nunjucks.get(item, "hint");
    Object status = Nunjucks.get(item, "status");

    StringBuilder out = new StringBuilder();
    out.append("  <li class=\"govuk-task-list__item")
        .append(Attributes.flagIf(" govuk-task-list__item--with-link", Nunjucks.get(item, "href")))
        .append(Attributes.classesIf(Nunjucks.get(item, "classes")))
        .append("\">\n");
    out.append("    <div class=\"govuk-task-list__name-and-hint\">\n");

    Object href = Nunjucks.get(item, "href");
    if (Nunjucks.truthy(href)) {
      String describedBy = statusId;
      if (Nunjucks.truthy(hint)) {
        describedBy = hintId + " " + statusId;
      }
      out.append("      <a class=\"govuk-link govuk-task-list__link")
          .append(Attributes.classesIf(Nunjucks.get(title, "classes")))
          .append("\" href=\"")
          .append(Nunjucks.out(href))
          .append("\" aria-describedby=\"")
          .append(Nunjucks.escape(describedBy))
          .append("\">\n");
      out.append("        ")
          .append(Nunjucks.contentIndent(title, "html", "text", 8))
          .append('\n');
      out.append("      </a>\n");
    } else {
      out.append("      <div")
          .append(Attributes.attributeIf("class", Nunjucks.get(title, "classes")))
          .append(">\n");
      out.append("        ")
          .append(Nunjucks.contentIndent(title, "html", "text", 8))
          .append('\n');
      out.append("      </div>\n");
    }

    if (Nunjucks.truthy(hint)) {
      out.append("      <div id=\"")
          .append(Nunjucks.escape(hintId))
          .append("\" class=\"govuk-task-list__hint\">\n");
      out.append("        ")
          .append(Nunjucks.contentIndent(hint, "html", "text", 8))
          .append('\n');
      out.append("      </div>\n");
    }
    out.append("    </div>\n");

    out.append("    <div class=\"govuk-task-list__status")
        .append(Attributes.classesIf(Nunjucks.get(status, "classes")))
        .append("\" id=\"")
        .append(Nunjucks.escape(statusId))
        .append("\">\n");
    Object tag = Nunjucks.get(status, "tag");
    if (Nunjucks.truthy(tag)) {
      Params tagParams = tag instanceof Params tp ? tp : new Params();
      out.append("      ")
          .append(Nunjucks.indent(Nunjucks.trim(ComponentsText.renderTag(tagParams)), 6, false))
          .append('\n');
    } else {
      out.append("      ")
          .append(Nunjucks.contentIndent(status, "html", "text", 6))
          .append('\n');
    }
    out.append("    </div>\n  </li>");
    return out.toString();
  }
}
