package uk.gov.example.govuk;

import java.util.ArrayList;
import java.util.List;

/**
 * Ports for form components (input, radios, date-input, character-count, …).
 */
final class ComponentsForms {

  private ComponentsForms() {}

  static String formGroupOpen(Params p) {
    Object formGroup = p.get("formGroup");
    return "<div class=\"govuk-form-group"
        + Attributes.flagIf(" govuk-form-group--error", p.get("errorMessage"))
        + Attributes.classesIf(Nunjucks.get(formGroup, "classes"))
        + '"'
        + Attributes.attributes(Nunjucks.get(formGroup, "attributes"))
        + ">\n";
  }

  static String describedByAppend(String describedBy, String id) {
    if (!describedBy.isEmpty()) {
      return describedBy + " " + id;
    }
    return id;
  }

  static String[] hintBlock(Params p, String id, String describedBy, int width) {
    Object hint = p.get("hint");
    if (!Nunjucks.truthy(hint)) {
      return new String[] {"", describedBy};
    }
    String hintId = id + "-hint";
    describedBy = describedByAppend(describedBy, hintId);
    String html =
        ComponentsText.renderHint(
            Params.of(
                "id", hintId,
                "classes", Nunjucks.get(hint, "classes"),
                "attributes", Nunjucks.get(hint, "attributes"),
                "html", Nunjucks.get(hint, "html"),
                "text", Nunjucks.get(hint, "text")));
    return new String[] {
      " ".repeat(width) + Nunjucks.indent(Nunjucks.trim(html), width, false) + "\n", describedBy
    };
  }

  static String[] errorBlock(Params p, String id, String describedBy, int width) {
    Object message = p.get("errorMessage");
    if (!Nunjucks.truthy(message)) {
      return new String[] {"", describedBy};
    }
    String errorId = id + "-error";
    describedBy = describedByAppend(describedBy, errorId);
    String html =
        ComponentsText.renderErrorMessage(
            Params.of(
                "id", errorId,
                "classes", Nunjucks.get(message, "classes"),
                "attributes", Nunjucks.get(message, "attributes"),
                "html", Nunjucks.get(message, "html"),
                "text", Nunjucks.get(message, "text"),
                "visuallyHiddenText", Nunjucks.get(message, "visuallyHiddenText")));
    return new String[] {
      " ".repeat(width) + Nunjucks.indent(Nunjucks.trim(html), width, false) + "\n", describedBy
    };
  }

  static String labelBlock(Params p, Object id, int width) {
    Object label = p.get("label");
    String html =
        ComponentsText.renderLabel(
            Params.of(
                "html", Nunjucks.get(label, "html"),
                "text", Nunjucks.get(label, "text"),
                "classes", Nunjucks.get(label, "classes"),
                "isPageHeading", Nunjucks.get(label, "isPageHeading"),
                "attributes", Nunjucks.get(label, "attributes"),
                "for", id));
    return " ".repeat(width) + Nunjucks.indent(Nunjucks.trim(html), width, false) + "\n";
  }

  static String slotContent(Object slot, int width, boolean indentFirst) {
    Object html = Nunjucks.get(slot, "html");
    if (Nunjucks.truthy(html)) {
      return Nunjucks.indent(Nunjucks.trim(Nunjucks.str(html)), width, indentFirst);
    }
    return Nunjucks.out(Nunjucks.get(slot, "text"));
  }

  static Object componentId(Params p) {
    Object id = p.get("id");
    if (Nunjucks.truthy(id)) {
      return id;
    }
    return p.get("name");
  }

  static String renderInput(Params p) {
    String classNames = "govuk-input";
    Object classes = p.get("classes");
    if (Nunjucks.truthy(classes)) {
      classNames += " " + Nunjucks.str(classes);
    }
    if (Nunjucks.truthy(p.get("errorMessage"))) {
      classNames += " govuk-input--error";
    }

    Object id = componentId(p);
    String describedBy = "";
    Object supplied = p.get("describedBy");
    if (Nunjucks.truthy(supplied)) {
      describedBy = Nunjucks.str(supplied);
    }

    Object formGroup = p.get("formGroup");
    Object prefix = p.get("prefix");
    Object suffix = p.get("suffix");
    Object beforeInput = Nunjucks.get(formGroup, "beforeInput");
    Object afterInput = Nunjucks.get(formGroup, "afterInput");
    boolean hasPrefix =
        Nunjucks.truthy(prefix)
            && (Nunjucks.truthy(Nunjucks.get(prefix, "text"))
                || Nunjucks.truthy(Nunjucks.get(prefix, "html")));
    boolean hasSuffix =
        Nunjucks.truthy(suffix)
            && (Nunjucks.truthy(Nunjucks.get(suffix, "text"))
                || Nunjucks.truthy(Nunjucks.get(suffix, "html")));
    boolean hasBefore =
        Nunjucks.truthy(beforeInput)
            && (Nunjucks.truthy(Nunjucks.get(beforeInput, "text"))
                || Nunjucks.truthy(Nunjucks.get(beforeInput, "html")));
    boolean hasAfter =
        Nunjucks.truthy(afterInput)
            && (Nunjucks.truthy(Nunjucks.get(afterInput, "text"))
                || Nunjucks.truthy(Nunjucks.get(afterInput, "html")));

    StringBuilder out = new StringBuilder();
    out.append(formGroupOpen(p));
    out.append(labelBlock(p, id, 2));

    String[] hintResult = hintBlock(p, Nunjucks.str(id), describedBy, 2);
    out.append(hintResult[0]);
    describedBy = hintResult[1];
    String[] errorResult = errorBlock(p, Nunjucks.str(id), describedBy, 2);
    out.append(errorResult[0]);
    describedBy = errorResult[1];

    String element = inputElement(p, classNames, id, describedBy);
    if (hasPrefix || hasSuffix || hasBefore || hasAfter) {
      Object wrapper = p.get("inputWrapper");
      out.append("  <div class=\"govuk-input__wrapper")
          .append(Attributes.classesIf(Nunjucks.get(wrapper, "classes")))
          .append('"')
          .append(Attributes.attributes(Nunjucks.get(wrapper, "attributes")))
          .append(">\n");
      if (hasBefore) {
        out.append(slotContent(beforeInput, 4, true)).append('\n');
      }
      if (hasPrefix) {
        out.append(Nunjucks.indent(affixItem(prefix, "prefix"), 2, true)).append('\n');
      }
      out.append("    ").append(element).append('\n');
      if (hasSuffix) {
        out.append(Nunjucks.indent(affixItem(suffix, "suffix"), 2, true)).append('\n');
      }
      if (hasAfter) {
        out.append(slotContent(afterInput, 4, true)).append('\n');
      }
      out.append("  </div>\n");
    } else {
      out.append("  ").append(element).append('\n');
    }

    out.append("</div>");
    return out.toString();
  }

  static String inputElement(Params p, String classNames, Object id, String describedBy) {
    Object spellcheck = Boolean.FALSE;
    Object spellFlag = p.get("spellcheck");
    if (spellFlag instanceof Boolean b) {
      spellcheck = Boolean.toString(b);
    }
    Object ariaDescribedBy = Undefined.INSTANCE;
    if (!describedBy.isEmpty()) {
      ariaDescribedBy = describedBy;
    }

    Params attributes =
        Params.of(
            "class", classNames,
            "id", id,
            "name", p.get("name"),
            "type", Nunjucks.defTruthy(p.get("type"), "text"),
            "spellcheck", Params.of("value", spellcheck, "optional", true),
            "value", Params.of("value", p.get("value"), "optional", true),
            "disabled", Params.of("value", p.get("disabled"), "optional", true),
            "aria-describedby", Params.of("value", ariaDescribedBy, "optional", true),
            "autocomplete", Params.of("value", p.get("autocomplete"), "optional", true),
            "autocapitalize", Params.of("value", p.get("autocapitalize"), "optional", true),
            "pattern", Params.of("value", p.get("pattern"), "optional", true),
            "inputmode", Params.of("value", p.get("inputmode"), "optional", true));
    return "<input"
        + Attributes.attributes(attributes)
        + Attributes.attributes(p.get("attributes"))
        + ">";
  }

  static String affixItem(Object affix, String kind) {
    return "  <div class=\"govuk-input__"
        + kind
        + Attributes.classesIf(Nunjucks.get(affix, "classes"))
        + "\" aria-hidden=\"true\""
        + Attributes.attributes(Nunjucks.get(affix, "attributes"))
        + ">"
        + Nunjucks.contentIndent(affix, "html", "text", 4)
        + "</div>";
  }

  static String renderTextarea(Params p) {
    Object id = componentId(p);
    String describedBy = "";
    Object supplied = p.get("describedBy");
    if (Nunjucks.truthy(supplied)) {
      describedBy = Nunjucks.str(supplied);
    }
    Object formGroup = p.get("formGroup");

    StringBuilder out = new StringBuilder();
    out.append(formGroupOpen(p));
    out.append(labelBlock(p, id, 2));

    String[] hintResult = hintBlock(p, Nunjucks.str(id), describedBy, 2);
    out.append(hintResult[0]);
    describedBy = hintResult[1];
    String[] errorResult = errorBlock(p, Nunjucks.str(id), describedBy, 2);
    out.append(errorResult[0]);
    describedBy = errorResult[1];

    Object before = Nunjucks.get(formGroup, "beforeInput");
    if (Nunjucks.truthy(before)) {
      out.append("  ").append(slotContent(before, 2, false)).append('\n');
    }

    String spellcheck = "";
    Object spellFlag = p.get("spellcheck");
    if (spellFlag instanceof Boolean b) {
      spellcheck = " spellcheck=\"" + Boolean.toString(b) + '"';
    }
    out.append("  <textarea class=\"govuk-textarea")
        .append(Attributes.flagIf(" govuk-textarea--error", p.get("errorMessage")))
        .append(Attributes.classesIf(p.get("classes")))
        .append("\" id=\"")
        .append(Nunjucks.out(id))
        .append("\" name=\"")
        .append(Nunjucks.out(p.get("name")))
        .append("\" rows=\"")
        .append(Nunjucks.out(Nunjucks.defTruthy(p.get("rows"), "5")))
        .append('"')
        .append(spellcheck)
        .append(Attributes.flagIf(" disabled", p.get("disabled")))
        .append(Attributes.attributeIf("aria-describedby", describedBy))
        .append(Attributes.attributeIf("autocomplete", p.get("autocomplete")))
        .append(Attributes.attributes(p.get("attributes")))
        .append('>')
        .append(Nunjucks.out(p.get("value")))
        .append("</textarea>\n");

    Object after = Nunjucks.get(formGroup, "afterInput");
    if (Nunjucks.truthy(after)) {
      out.append("  ").append(slotContent(after, 2, false)).append('\n');
    }

    out.append("</div>");
    return out.toString();
  }

  static String renderSelect(Params p) {
    Object id = componentId(p);
    String describedBy = "";
    Object supplied = p.get("describedBy");
    if (Nunjucks.truthy(supplied)) {
      describedBy = Nunjucks.str(supplied);
    }
    Object formGroup = p.get("formGroup");

    StringBuilder out = new StringBuilder();
    out.append(formGroupOpen(p));
    out.append(labelBlock(p, id, 2));

    String[] hintResult = hintBlock(p, Nunjucks.str(id), describedBy, 2);
    out.append(hintResult[0]);
    describedBy = hintResult[1];
    String[] errorResult = errorBlock(p, Nunjucks.str(id), describedBy, 2);
    out.append(errorResult[0]);
    describedBy = errorResult[1];

    Object before = Nunjucks.get(formGroup, "beforeInput");
    if (Nunjucks.truthy(before)) {
      out.append("  ").append(slotContent(before, 2, false)).append('\n');
    }

    out.append("  <select class=\"govuk-select")
        .append(Attributes.classesIf(p.get("classes")))
        .append(Attributes.flagIf(" govuk-select--error", p.get("errorMessage")))
        .append("\" id=\"")
        .append(Nunjucks.out(id))
        .append("\" name=\"")
        .append(Nunjucks.out(p.get("name")))
        .append('"')
        .append(Attributes.flagIf(" disabled", p.get("disabled")))
        .append(Attributes.attributeIf("aria-describedby", describedBy))
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");

    Object selected = p.get("value");
    for (Object item : Nunjucks.items(p.get("items"))) {
      if (!Nunjucks.truthy(item)) {
        continue;
      }
      Object value = Nunjucks.get(item, "value");
      Object effective = Nunjucks.def(value, Nunjucks.get(item, "text"));
      boolean isSelected = Nunjucks.truthy(Nunjucks.get(item, "selected"));
      if (!isSelected && Nunjucks.truthy(selected)) {
        isSelected =
            Nunjucks.looseEq(effective, selected)
                && !Nunjucks.looseEq(Nunjucks.get(item, "selected"), false);
      }

      out.append("    <option");
      if (!Nunjucks.isUndefined(value)) {
        out.append(" value=\"").append(Nunjucks.out(value)).append('"');
      }
      out.append(Attributes.flagIf(" selected", isSelected))
          .append(Attributes.flagIf(" disabled", Nunjucks.get(item, "disabled")))
          .append(Attributes.attributes(Nunjucks.get(item, "attributes")))
          .append('>')
          .append(Nunjucks.out(Nunjucks.get(item, "text")))
          .append("</option>\n");
    }
    out.append("  </select>\n");

    Object after = Nunjucks.get(formGroup, "afterInput");
    if (Nunjucks.truthy(after)) {
      out.append("  ").append(slotContent(after, 2, false)).append('\n');
    }

    out.append("</div>");
    return out.toString();
  }

  static String renderFileUpload(Params p) {
    Object id = componentId(p);
    String describedBy = "";
    Object supplied = p.get("describedBy");
    if (Nunjucks.truthy(supplied)) {
      describedBy = Nunjucks.str(supplied);
    }
    Object formGroup = p.get("formGroup");

    StringBuilder out = new StringBuilder();
    out.append(formGroupOpen(p));
    out.append(labelBlock(p, id, 2));

    String[] hintResult = hintBlock(p, Nunjucks.str(id), describedBy, 2);
    out.append(hintResult[0]);
    describedBy = hintResult[1];
    String[] errorResult = errorBlock(p, Nunjucks.str(id), describedBy, 2);
    out.append(errorResult[0]);
    describedBy = errorResult[1];

    Object before = Nunjucks.get(formGroup, "beforeInput");
    if (Nunjucks.truthy(before)) {
      out.append("  ").append(slotContent(before, 2, false)).append('\n');
    }

    boolean javascript = Nunjucks.truthy(p.get("javascript"));
    if (javascript) {
      out.append("  <div\n    class=\"govuk-file-upload-wrapper")
          .append(Attributes.classesIf(p.get("wrapperClasses")))
          .append("\"\n    data-module=\"govuk-file-upload\"")
          .append(
              Attributes.i18nAttributes(
                  "choose-files-button", p.get("chooseFilesButtonText"), Undefined.INSTANCE))
          .append(
              Attributes.i18nAttributes(
                  "no-file-chosen", p.get("noFileChosenText"), Undefined.INSTANCE))
          .append(
              Attributes.i18nAttributes(
                  "multiple-files-chosen", Undefined.INSTANCE, p.get("multipleFilesChosenText")))
          .append(
              Attributes.i18nAttributes(
                  "drop-instruction", p.get("dropInstructionText"), Undefined.INSTANCE))
          .append(
              Attributes.i18nAttributes(
                  "entered-drop-zone", p.get("enteredDropZoneText"), Undefined.INSTANCE))
          .append(
              Attributes.i18nAttributes(
                  "left-drop-zone", p.get("leftDropZoneText"), Undefined.INSTANCE))
          .append(Attributes.attributes(p.get("wrapperAttributes")))
          .append("\n  >\n");
    }

    out.append("  <input class=\"govuk-file-upload")
        .append(Attributes.classesIf(p.get("classes")))
        .append(Attributes.flagIf(" govuk-file-upload--error", p.get("errorMessage")))
        .append("\" id=\"")
        .append(Nunjucks.out(id))
        .append("\" name=\"")
        .append(Nunjucks.out(p.get("name")))
        .append("\" type=\"file\"")
        .append(Attributes.flagIf(" disabled", p.get("disabled")))
        .append(Attributes.flagIf(" multiple", p.get("multiple")))
        .append(Attributes.attributeIf("aria-describedby", describedBy))
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");

    if (javascript) {
      out.append("  </div>\n");
    }
    Object after = Nunjucks.get(formGroup, "afterInput");
    if (Nunjucks.truthy(after)) {
      out.append("  ").append(slotContent(after, 2, false)).append('\n');
    }

    out.append("</div>");
    return out.toString();
  }

  static String renderCharacterCount(Params p) {
    Object maxwords = p.get("maxwords");
    Object maxlength = p.get("maxlength");
    boolean hasNoLimit = !Nunjucks.truthy(maxwords) && !Nunjucks.truthy(maxlength);
    Object id = componentId(p);

    Object descriptionNoLimit = Undefined.INSTANCE;
    if (!hasNoLimit) {
      Object limit = maxlength;
      if (Nunjucks.truthy(maxwords)) {
        limit = maxwords;
      }
      String unit = "characters";
      if (Nunjucks.truthy(maxwords)) {
        unit = "words";
      }
      String description = "You can enter up to %{count} " + unit;
      Object supplied = p.get("textareaDescriptionText");
      if (Nunjucks.truthy(supplied)) {
        description = Nunjucks.str(supplied);
      }
      descriptionNoLimit = description.replace("%{count}", Nunjucks.str(limit));
    }

    Object countMessage = p.get("countMessage");
    String countMessageHtml =
        Nunjucks.trim(
                ComponentsText.renderHint(
                    Params.of(
                        "text",
                        descriptionNoLimit,
                        "id",
                        Nunjucks.str(id) + "-info",
                        "classes",
                        "govuk-character-count__message"
                            + Render.concatIf(" ", Nunjucks.get(countMessage, "classes")))))
            + "\n";

    Object formGroup = p.get("formGroup");
    Object after = Nunjucks.get(formGroup, "afterInput");
    if (Nunjucks.truthy(after)) {
      Object html = Nunjucks.get(after, "html");
      if (Nunjucks.truthy(html)) {
        countMessageHtml += Nunjucks.trim(Nunjucks.str(html)) + "\n";
      } else {
        countMessageHtml += Nunjucks.out(Nunjucks.get(after, "text")) + "\n";
      }
    }

    String attributesHtml =
        Attributes.attributes(
            Params.of(
                "data-module",
                "govuk-character-count",
                "data-maxlength",
                Params.of("value", maxlength, "optional", true),
                "data-threshold",
                Params.of("value", p.get("threshold"), "optional", true),
                "data-maxwords",
                Params.of("value", maxwords, "optional", true)));
    Object description = p.get("textareaDescriptionText");
    if (hasNoLimit && Nunjucks.truthy(description)) {
      attributesHtml +=
          Attributes.i18nAttributes(
              "textarea-description", Undefined.INSTANCE, Params.of("other", description));
    }
    attributesHtml +=
        Attributes.i18nAttributes(
                "characters-under-limit", Undefined.INSTANCE, p.get("charactersUnderLimitText"))
            + Attributes.i18nAttributes(
                "characters-at-limit", p.get("charactersAtLimitText"), Undefined.INSTANCE)
            + Attributes.i18nAttributes(
                "characters-over-limit", Undefined.INSTANCE, p.get("charactersOverLimitText"))
            + Attributes.i18nAttributes(
                "words-under-limit", Undefined.INSTANCE, p.get("wordsUnderLimitText"))
            + Attributes.i18nAttributes(
                "words-at-limit", p.get("wordsAtLimitText"), Undefined.INSTANCE)
            + Attributes.i18nAttributes(
                "words-over-limit", Undefined.INSTANCE, p.get("wordsOverLimitText"));
    attributesHtml += appendedAttributes(Nunjucks.get(formGroup, "attributes"));

    Object label = p.get("label");
    return Nunjucks.trim(
        renderTextarea(
            Params.of(
                "id",
                id,
                "name",
                p.get("name"),
                "describedBy",
                Nunjucks.str(id) + "-info",
                "rows",
                p.get("rows"),
                "spellcheck",
                p.get("spellcheck"),
                "value",
                p.get("value"),
                "formGroup",
                Params.of(
                    "classes",
                    "govuk-character-count"
                        + Render.concatIf(" ", Nunjucks.get(formGroup, "classes")),
                    "attributes",
                    attributesHtml,
                    "beforeInput",
                    Nunjucks.get(formGroup, "beforeInput"),
                    "afterInput",
                    Params.of("html", new TrustedHtml(countMessageHtml))),
                "classes",
                "govuk-js-character-count" + Render.concatIf(" ", p.get("classes")),
                "label",
                Params.of(
                    "html",
                    Nunjucks.get(label, "html"),
                    "text",
                    Nunjucks.get(label, "text"),
                    "classes",
                    Nunjucks.get(label, "classes"),
                    "isPageHeading",
                    Nunjucks.get(label, "isPageHeading"),
                    "attributes",
                    Nunjucks.get(label, "attributes"),
                    "for",
                    id),
                "hint",
                p.get("hint"),
                "errorMessage",
                p.get("errorMessage"),
                "attributes",
                p.get("attributes"))));
  }

  static String appendedAttributes(Object attributes) {
    if (!(attributes instanceof Params object)) {
      return "";
    }
    StringBuilder out = new StringBuilder();
    for (String name : object.keys()) {
      out.append(' ')
          .append(Nunjucks.escape(name))
          .append("=\"")
          .append(Nunjucks.escape(Nunjucks.str(object.get(name))))
          .append('"');
    }
    return out.toString();
  }

  static String renderPasswordInput(Params p) {
    Object id = componentId(p);
    Object formGroup = p.get("formGroup");

    String attributesHtml =
        " data-module=\"govuk-password-input\""
            + Attributes.i18nAttributes(
                "show-password", p.get("showPasswordText"), Undefined.INSTANCE)
            + Attributes.i18nAttributes(
                "hide-password", p.get("hidePasswordText"), Undefined.INSTANCE)
            + Attributes.i18nAttributes(
                "show-password-aria-label",
                p.get("showPasswordAriaLabelText"),
                Undefined.INSTANCE)
            + Attributes.i18nAttributes(
                "hide-password-aria-label",
                p.get("hidePasswordAriaLabelText"),
                Undefined.INSTANCE)
            + Attributes.i18nAttributes(
                "password-shown-announcement",
                p.get("passwordShownAnnouncementText"),
                Undefined.INSTANCE)
            + Attributes.i18nAttributes(
                "password-hidden-announcement",
                p.get("passwordHiddenAnnouncementText"),
                Undefined.INSTANCE)
            + appendedAttributes(Nunjucks.get(formGroup, "attributes"));

    Object button = p.get("button");
    String buttonHtml =
        Nunjucks.trim(
                ComponentsButton.renderButton(
                    Params.of(
                        "type",
                        "button",
                        "classes",
                        "govuk-button--secondary govuk-password-input__toggle govuk-js-password-input-toggle"
                            + Render.concatIf(" ", Nunjucks.get(button, "classes")),
                        "text",
                        Nunjucks.def(p.get("showPasswordText"), "Show"),
                        "attributes",
                        Params.of(
                            "aria-controls",
                            id,
                            "aria-label",
                            Nunjucks.def(p.get("showPasswordAriaLabelText"), "Show password"),
                            "hidden",
                            Params.of("value", true, "optional", true)))))
            + "\n";
    Object after = Nunjucks.get(formGroup, "afterInput");
    if (Nunjucks.truthy(after)) {
      Object html = Nunjucks.get(after, "html");
      if (Nunjucks.truthy(html)) {
        buttonHtml += Nunjucks.trim(Nunjucks.str(html)) + "\n";
      } else {
        buttonHtml += Nunjucks.out(Nunjucks.get(after, "text")) + "\n";
      }
    }

    return Nunjucks.trim(
        renderInput(
            Params.of(
                "formGroup",
                Params.of(
                    "classes",
                    "govuk-password-input"
                        + Render.concatIf(" ", Nunjucks.get(formGroup, "classes")),
                    "attributes",
                    attributesHtml,
                    "beforeInput",
                    Nunjucks.get(formGroup, "beforeInput"),
                    "afterInput",
                    Params.of("html", new TrustedHtml(buttonHtml))),
                "inputWrapper",
                Params.of("classes", "govuk-password-input__wrapper"),
                "label",
                p.get("label"),
                "hint",
                p.get("hint"),
                "classes",
                "govuk-password-input__input govuk-js-password-input-input"
                    + Render.concatIf(" ", p.get("classes")),
                "errorMessage",
                p.get("errorMessage"),
                "id",
                id,
                "name",
                p.get("name"),
                "type",
                "password",
                "spellcheck",
                false,
                "autocapitalize",
                "none",
                "autocomplete",
                Nunjucks.defTruthy(p.get("autocomplete"), "current-password"),
                "value",
                p.get("value"),
                "disabled",
                p.get("disabled"),
                "describedBy",
                p.get("describedBy"),
                "attributes",
                p.get("attributes"))));
  }

  static String renderCheckboxes(Params p) {
    Object idPrefix = p.get("idPrefix");
    if (!Nunjucks.truthy(idPrefix)) {
      idPrefix = p.get("name");
    }
    Object fieldset = p.get("fieldset");
    String describedBy = "";
    Object supplied = p.get("describedBy");
    if (Nunjucks.truthy(supplied)) {
      describedBy = Nunjucks.str(supplied);
    }
    Object fieldsetDescribedBy = Nunjucks.get(fieldset, "describedBy");
    if (Nunjucks.truthy(fieldsetDescribedBy)) {
      describedBy = Nunjucks.str(fieldsetDescribedBy);
    }
    boolean hasFieldset = Nunjucks.truthy(fieldset);

    StringBuilder inner = new StringBuilder();
    String[] hintResult = hintBlock(p, Nunjucks.str(idPrefix), describedBy, 2);
    inner.append(hintResult[0]);
    describedBy = hintResult[1];
    String[] errorResult = errorBlock(p, Nunjucks.str(idPrefix), describedBy, 2);
    inner.append(errorResult[0]);
    describedBy = errorResult[1];

    Object formGroup = p.get("formGroup");
    inner
        .append("  <div class=\"govuk-checkboxes")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(" data-module=\"govuk-checkboxes\">\n");
    Object before = Nunjucks.get(formGroup, "beforeInputs");
    if (Nunjucks.truthy(before)) {
      inner.append("    ").append(slotContent(before, 4, false)).append('\n');
    }
    int index = 0;
    for (Object item : Nunjucks.items(p.get("items"))) {
      index++;
      if (!Nunjucks.truthy(item)) {
        continue;
      }
      inner.append(checkboxItem(p, item, index, Nunjucks.str(idPrefix), describedBy, hasFieldset));
    }
    Object after = Nunjucks.get(formGroup, "afterInputs");
    if (Nunjucks.truthy(after)) {
      inner.append("    ").append(slotContent(after, 4, false)).append('\n');
    }
    inner.append("  </div>\n");

    return fieldsetWrapper(p, inner.toString(), describedBy, Undefined.INSTANCE, false);
  }

  static String fieldsetWrapper(
      Params p, String inner, String describedBy, Object role, boolean indentFieldset) {
    Object fieldset = p.get("fieldset");
    String body = Nunjucks.trim(inner);
    if (Nunjucks.truthy(fieldset)) {
      String html =
          ComponentsText.renderFieldset(
              Params.of(
                  "describedBy",
                  describedBy,
                  "classes",
                  Nunjucks.get(fieldset, "classes"),
                  "role",
                  role,
                  "attributes",
                  Nunjucks.get(fieldset, "attributes"),
                  "legend",
                  Nunjucks.get(fieldset, "legend"),
                  "html",
                  new TrustedHtml(body)));
      body = Nunjucks.trim(html);
      if (indentFieldset) {
        body = Nunjucks.indent(body, 2, false);
      }
    }
    return formGroupOpen(p) + "  " + body + "\n</div>";
  }

  static String checkboxItem(
      Params p,
      Object item,
      int index,
      String idPrefix,
      String describedBy,
      boolean hasFieldset) {
    String itemId = idPrefix;
    if (index > 1) {
      itemId += "-" + index;
    }
    Object supplied = Nunjucks.get(item, "id");
    if (Nunjucks.truthy(supplied)) {
      itemId = Nunjucks.str(supplied);
    }
    Object itemName = p.get("name");
    Object nameSupplied = Nunjucks.get(item, "name");
    if (Nunjucks.truthy(nameSupplied)) {
      itemName = nameSupplied;
    }
    String conditionalId = "conditional-" + itemId;

    Object divider = Nunjucks.get(item, "divider");
    if (Nunjucks.truthy(divider)) {
      return "    <div class=\"govuk-checkboxes__divider\">"
          + Nunjucks.out(divider)
          + "</div>\n";
    }

    boolean checked = Nunjucks.truthy(Nunjucks.get(item, "checked"));
    if (!checked && Nunjucks.truthy(p.get("values"))) {
      checked =
          Nunjucks.contains(Nunjucks.get(item, "value"), p.get("values"))
              && !Nunjucks.looseEq(Nunjucks.get(item, "checked"), false);
    }
    Object hint = Nunjucks.get(item, "hint");
    boolean hasHint =
        Nunjucks.truthy(Nunjucks.get(hint, "text")) || Nunjucks.truthy(Nunjucks.get(hint, "html"));
    String itemHintId = "";
    if (hasHint) {
      itemHintId = itemId + "-item-hint";
    }
    String itemDescribedBy = "";
    if (!hasFieldset) {
      itemDescribedBy = describedBy;
    }
    itemDescribedBy = Nunjucks.trim(itemDescribedBy + " " + itemHintId);

    Object conditional = Nunjucks.get(item, "conditional");
    Object label = Nunjucks.get(item, "label");

    StringBuilder out = new StringBuilder();
    out.append("    <div class=\"govuk-checkboxes__item\">\n");
    out.append("      <input class=\"govuk-checkboxes__input\" id=\"")
        .append(Nunjucks.escape(itemId))
        .append("\" name=\"")
        .append(Nunjucks.out(itemName))
        .append("\" type=\"checkbox\" value=\"")
        .append(Nunjucks.out(Nunjucks.get(item, "value")))
        .append('"')
        .append(Attributes.flagIf(" checked", checked))
        .append(Attributes.flagIf(" disabled", Nunjucks.get(item, "disabled")))
        .append(
            Attributes.attributeIf(
                "data-aria-controls",
                ifTruthy(Nunjucks.get(conditional, "html"), conditionalId)))
        .append(Attributes.attributeIf("data-behaviour", Nunjucks.get(item, "behaviour")))
        .append(Attributes.attributeIf("aria-describedby", itemDescribedBy))
        .append(Attributes.attributes(Nunjucks.get(item, "attributes")))
        .append(">\n");
    out.append("      ")
        .append(
            Nunjucks.indent(
                Nunjucks.trim(
                    ComponentsText.renderLabel(
                        Params.of(
                            "html",
                            Nunjucks.get(item, "html"),
                            "text",
                            Nunjucks.get(item, "text"),
                            "classes",
                            "govuk-checkboxes__label"
                                + Render.concatIf(" ", Nunjucks.get(label, "classes")),
                            "attributes",
                            Nunjucks.get(label, "attributes"),
                            "for",
                            itemId))),
                6,
                false))
        .append('\n');
    if (hasHint) {
      out.append("      ")
          .append(
              Nunjucks.indent(
                  Nunjucks.trim(
                      ComponentsText.renderHint(
                          Params.of(
                              "id",
                              itemHintId,
                              "classes",
                              "govuk-checkboxes__hint"
                                  + Render.concatIf(" ", Nunjucks.get(hint, "classes")),
                              "attributes",
                              Nunjucks.get(hint, "attributes"),
                              "html",
                              Nunjucks.get(hint, "html"),
                              "text",
                              Nunjucks.get(hint, "text")))),
                  6,
                  false))
          .append('\n');
    }
    out.append("    </div>\n");
    Object html = Nunjucks.get(conditional, "html");
    if (Nunjucks.truthy(html)) {
      out.append("    <div class=\"govuk-checkboxes__conditional")
          .append(Attributes.flagIf(" govuk-checkboxes__conditional--hidden", !checked))
          .append("\" id=\"")
          .append(Nunjucks.escape(conditionalId))
          .append("\">\n      ")
          .append(Nunjucks.trim(Nunjucks.str(html)))
          .append("\n    </div>\n");
    }
    return out.toString();
  }

  static Object ifTruthy(Object condition, String value) {
    if (condition instanceof Boolean b) {
      if (b) {
        return value;
      }
      return Undefined.INSTANCE;
    }
    if (Nunjucks.truthy(condition)) {
      return value;
    }
    return Undefined.INSTANCE;
  }

  static String renderRadios(Params p) {
    Object idPrefix = p.get("idPrefix");
    if (!Nunjucks.truthy(idPrefix)) {
      idPrefix = p.get("name");
    }
    Object fieldset = p.get("fieldset");
    String describedBy = "";
    Object supplied = Nunjucks.get(fieldset, "describedBy");
    if (Nunjucks.truthy(supplied)) {
      describedBy = Nunjucks.str(supplied);
    }

    StringBuilder inner = new StringBuilder();
    String[] hintResult = hintBlock(p, Nunjucks.str(idPrefix), describedBy, 2);
    inner.append(hintResult[0]);
    describedBy = hintResult[1];
    String[] errorResult = errorBlock(p, Nunjucks.str(idPrefix), describedBy, 2);
    inner.append(errorResult[0]);
    describedBy = errorResult[1];

    Object formGroup = p.get("formGroup");
    inner
        .append("  <div class=\"govuk-radios")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(" data-module=\"govuk-radios\">\n");
    Object before = Nunjucks.get(formGroup, "beforeInputs");
    if (Nunjucks.truthy(before)) {
      inner.append("    ").append(slotContent(before, 4, false)).append('\n');
    }
    int index = 0;
    for (Object item : Nunjucks.items(p.get("items"))) {
      index++;
      if (!Nunjucks.truthy(item)) {
        continue;
      }
      inner.append(radioItem(p, item, index, Nunjucks.str(idPrefix)));
    }
    Object after = Nunjucks.get(formGroup, "afterInputs");
    if (Nunjucks.truthy(after)) {
      inner.append("    ").append(slotContent(after, 4, false)).append('\n');
    }
    inner.append("  </div>\n");

    return fieldsetWrapper(p, inner.toString(), describedBy, Undefined.INSTANCE, false);
  }

  static String radioItem(Params p, Object item, int index, String idPrefix) {
    String itemId = idPrefix;
    if (index > 1) {
      itemId += "-" + index;
    }
    Object supplied = Nunjucks.get(item, "id");
    if (Nunjucks.truthy(supplied)) {
      itemId = Nunjucks.str(supplied);
    }
    String conditionalId = "conditional-" + itemId;

    Object divider = Nunjucks.get(item, "divider");
    if (Nunjucks.truthy(divider)) {
      return "    <div class=\"govuk-radios__divider\">" + Nunjucks.out(divider) + "</div>\n";
    }

    boolean checked = Nunjucks.truthy(Nunjucks.get(item, "checked"));
    if (!checked && Nunjucks.truthy(p.get("value"))) {
      checked =
          Nunjucks.looseEq(Nunjucks.get(item, "value"), p.get("value"))
              && !Nunjucks.looseEq(Nunjucks.get(item, "checked"), false);
    }
    Object hint = Nunjucks.get(item, "hint");
    boolean hasHint =
        Nunjucks.truthy(Nunjucks.get(hint, "text")) || Nunjucks.truthy(Nunjucks.get(hint, "html"));
    String itemHintId = itemId + "-item-hint";
    Object conditional = Nunjucks.get(item, "conditional");
    Object label = Nunjucks.get(item, "label");

    StringBuilder out = new StringBuilder();
    out.append("    <div class=\"govuk-radios__item\">\n");
    out.append("      <input class=\"govuk-radios__input\" id=\"")
        .append(Nunjucks.escape(itemId))
        .append("\" name=\"")
        .append(Nunjucks.out(p.get("name")))
        .append("\" type=\"radio\" value=\"")
        .append(Nunjucks.out(Nunjucks.get(item, "value")))
        .append('"')
        .append(Attributes.flagIf(" checked", checked))
        .append(Attributes.flagIf(" disabled", Nunjucks.get(item, "disabled")))
        .append(
            Attributes.attributeIf(
                "data-aria-controls",
                ifTruthy(Nunjucks.get(conditional, "html"), conditionalId)))
        .append(Attributes.attributeIf("aria-describedby", ifTruthy(hasHint, itemHintId)))
        .append(Attributes.attributes(Nunjucks.get(item, "attributes")))
        .append(">\n");
    out.append("      ")
        .append(
            Nunjucks.indent(
                Nunjucks.trim(
                    ComponentsText.renderLabel(
                        Params.of(
                            "html",
                            Nunjucks.get(item, "html"),
                            "text",
                            Nunjucks.get(item, "text"),
                            "classes",
                            "govuk-radios__label"
                                + Render.concatIf(" ", Nunjucks.get(label, "classes")),
                            "attributes",
                            Nunjucks.get(label, "attributes"),
                            "for",
                            itemId))),
                6,
                false))
        .append('\n');
    if (hasHint) {
      out.append("      ")
          .append(
              Nunjucks.indent(
                  Nunjucks.trim(
                      ComponentsText.renderHint(
                          Params.of(
                              "id",
                              itemHintId,
                              "classes",
                              "govuk-radios__hint"
                                  + Render.concatIf(" ", Nunjucks.get(hint, "classes")),
                              "attributes",
                              Nunjucks.get(hint, "attributes"),
                              "html",
                              Nunjucks.get(hint, "html"),
                              "text",
                              Nunjucks.get(hint, "text")))),
                  6,
                  false))
          .append('\n');
    }
    out.append("    </div>\n");
    Object html = Nunjucks.get(conditional, "html");
    if (Nunjucks.truthy(html)) {
      out.append("    <div class=\"govuk-radios__conditional")
          .append(Attributes.flagIf(" govuk-radios__conditional--hidden", !checked))
          .append("\" id=\"")
          .append(Nunjucks.escape(conditionalId))
          .append("\">\n      ")
          .append(Nunjucks.trim(Nunjucks.str(html)))
          .append("\n    </div>\n");
    }
    return out.toString();
  }

  static String renderDateInput(Params p) {
    Object fieldset = p.get("fieldset");
    String describedBy = "";
    Object supplied = Nunjucks.get(fieldset, "describedBy");
    if (Nunjucks.truthy(supplied)) {
      describedBy = Nunjucks.str(supplied);
    }
    Object values = p.get("values");

    Object day =
        Nunjucks.def(
            p.get("day"),
            Params.of(
                "name", "day", "value", Nunjucks.get(values, "day"), "classes", "govuk-input--width-2"));
    Object month =
        Nunjucks.def(
            p.get("month"),
            Params.of(
                "name",
                "month",
                "value",
                Nunjucks.get(values, "month"),
                "classes",
                "govuk-input--width-2"));
    Object year =
        Nunjucks.def(
            p.get("year"),
            Params.of(
                "name",
                "year",
                "value",
                Nunjucks.get(values, "year"),
                "classes",
                "govuk-input--width-4"));

    List<Object> dateItems = new ArrayList<>(Nunjucks.items(p.get("items")));
    if (dateItems.isEmpty()) {
      dateItems = List.of(day, month, year);
    }

    boolean anyItemHasError = false;
    for (Object item : dateItems) {
      if (dateItemHasError(item)) {
        anyItemHasError = true;
      }
    }

    StringBuilder inner = new StringBuilder();
    String[] hintResult = hintBlock(p, Nunjucks.str(p.get("id")), describedBy, 2);
    inner.append(hintResult[0]);
    describedBy = hintResult[1];
    String[] errorResult = errorBlock(p, Nunjucks.str(p.get("id")), describedBy, 2);
    inner.append(errorResult[0]);
    describedBy = errorResult[1];

    Object formGroup = p.get("formGroup");
    inner
        .append("  <div class=\"govuk-date-input")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(Attributes.attributeIf("id", p.get("id")))
        .append(">\n");
    Object before = Nunjucks.get(formGroup, "beforeInputs");
    if (Nunjucks.truthy(before)) {
      inner.append("    ").append(slotContent(before, 4, false)).append('\n');
    }
    for (Object item : dateItems) {
      if (!Nunjucks.truthy(item)) {
        continue;
      }
      inner
          .append(
              Nunjucks.indent(
                  Nunjucks.trim(dateInputItem(p, item, anyItemHasError, day, month, year)),
                  4,
                  true))
          .append('\n');
    }
    Object after = Nunjucks.get(formGroup, "afterInputs");
    if (Nunjucks.truthy(after)) {
      inner.append("    ").append(slotContent(after, 4, false)).append('\n');
    }
    inner.append("  </div>\n");

    return fieldsetWrapper(p, inner.toString(), describedBy, "group", true);
  }

  static boolean dateItemHasError(Object item) {
    if (Nunjucks.truthy(Nunjucks.get(item, "error"))) {
      return true;
    }
    Object classes = Nunjucks.get(item, "classes");
    return Nunjucks.truthy(classes) && Nunjucks.contains("govuk-input--error", classes);
  }

  static String dateInputItem(
      Params p, Object item, boolean anyItemHasError, Object day, Object month, Object year) {
    Object itemName = Nunjucks.get(item, "name");
    Object itemValue = Nunjucks.get(item, "value");
    String itemWidth = "2";
    String itemClasses = "";
    boolean itemHasError = dateItemHasError(item);

    Object name = Nunjucks.get(item, "name");
    if (item == day
        || (Nunjucks.truthy(name)
            && Nunjucks.contains(name, List.of("day", Nunjucks.get(day, "name"))))) {
      itemName = Nunjucks.def(name, "day");
      itemValue = Nunjucks.def(itemValue, Nunjucks.get(day, "value"));
    } else if (item == month
        || (Nunjucks.truthy(name)
            && Nunjucks.contains(name, List.of("month", Nunjucks.get(month, "name"))))) {
      itemName = Nunjucks.def(name, "month");
      itemValue = Nunjucks.def(itemValue, Nunjucks.get(month, "value"));
    } else if (item == year
        || (Nunjucks.truthy(name)
            && Nunjucks.contains(name, List.of("year", Nunjucks.get(year, "name"))))) {
      itemName = Nunjucks.def(name, "year");
      itemValue = Nunjucks.def(itemValue, Nunjucks.get(year, "value"));
      itemWidth = "4";
    }

    Object classes = Nunjucks.get(item, "classes");
    boolean hasErrorClass =
        Nunjucks.truthy(classes) && Nunjucks.contains("govuk-input--error", classes);
    if (!hasErrorClass
        && (itemHasError
            || (!Nunjucks.looseEq(Nunjucks.get(item, "error"), false)
                && Nunjucks.truthy(p.get("errorMessage"))
                && !anyItemHasError))) {
      itemClasses = Nunjucks.trim(itemClasses + " govuk-input--error");
    }
    if (!Nunjucks.truthy(classes) || !Nunjucks.contains("govuk-input--width-", classes)) {
      itemClasses = Nunjucks.trim(itemClasses + " govuk-input--width-" + itemWidth);
    }
    if (Nunjucks.truthy(classes)) {
      itemClasses = Nunjucks.trim(itemClasses + " " + Nunjucks.str(classes));
    }

    String namePrefix = "";
    Object prefix = p.get("namePrefix");
    if (Nunjucks.truthy(prefix)) {
      namePrefix = Nunjucks.str(prefix) + "-";
    }

    Object label = Nunjucks.get(item, "label");
    if (!Nunjucks.truthy(label)) {
      label = capitalise(Nunjucks.str(itemName));
    }
    Object id = Nunjucks.get(item, "id");
    if (!Nunjucks.truthy(id)) {
      id = Nunjucks.str(p.get("id")) + "-" + Nunjucks.str(itemName);
    }
    Object value = itemValue;
    if (Nunjucks.isUndefined(value)) {
      value = Nunjucks.get(p.get("values"), namePrefix + Nunjucks.str(itemName));
    }

    String input =
        renderInput(
            Params.of(
                "label",
                Params.of("text", label, "classes", "govuk-date-input__label"),
                "id",
                id,
                "classes",
                "govuk-date-input__input" + Render.concatIf(" ", itemClasses),
                "name",
                namePrefix + Nunjucks.str(itemName),
                "value",
                value,
                "type",
                "text",
                "inputmode",
                Nunjucks.defTruthy(Nunjucks.get(item, "inputmode"), "numeric"),
                "autocomplete",
                Nunjucks.get(item, "autocomplete"),
                "pattern",
                Nunjucks.get(item, "pattern"),
                "attributes",
                Nunjucks.get(item, "attributes")));

    return "<div class=\"govuk-date-input__item\">\n  "
        + Nunjucks.indent(Nunjucks.trim(input), 2, false)
        + "\n</div>";
  }

  static String capitalise(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    String lowered = text.toLowerCase();
    return Character.toUpperCase(lowered.charAt(0)) + lowered.substring(1);
  }
}
