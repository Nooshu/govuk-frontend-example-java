package uk.gov.example.service;

import java.util.ArrayList;
import java.util.List;
import uk.gov.example.govuk.Nunjucks;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.TrustedHtml;

/** Build GOV.UK component option maps for each question page and support content. */
public final class Forms {

  private Forms() {}

  public static Params errorSummary(List<Validation.FieldError> errors) {
    if (errors == null || errors.isEmpty()) {
      return null;
    }
    List<Object> list = new ArrayList<>(errors.size());
    for (Validation.FieldError err : errors) {
      list.add(Params.of("text", err.text(), "href", err.href()));
    }
    return Params.of("titleText", "There is a problem", "errorList", list);
  }

  public static Params nameField(
      LicenceApplication application, List<Validation.FieldError> errors) {
    return textInput(
        "full-name",
        "What is your full name?",
        application.fullName(),
        errors,
        Params.of(
            "autocomplete",
            "name",
            "label",
            Params.of(
                "text",
                "What is your full name?",
                "isPageHeading",
                true,
                "classes",
                "govuk-label--l")));
  }

  public static Params emailField(
      LicenceApplication application, List<Validation.FieldError> errors) {
    return textInput(
        "email",
        "What is your email address?",
        application.email(),
        errors,
        Params.of(
            "type",
            "email",
            "autocomplete",
            "email",
            "spellcheck",
            false,
            "hint",
            Params.of("text", "This example stores the address in your browser session only."),
            "label",
            Params.of(
                "text",
                "What is your email address?",
                "isPageHeading",
                true,
                "classes",
                "govuk-label--l")));
  }

  public static Params dateField(
      LicenceApplication application, List<Validation.FieldError> errors) {
    Params date =
        Params.of(
            "id",
            "date-of-birth",
            "namePrefix",
            "date-of-birth",
            "fieldset",
            Params.of(
                "legend",
                Params.of(
                    "text",
                    "What is your date of birth?",
                    "isPageHeading",
                    true,
                    "classes",
                    "govuk-fieldset__legend--l")),
            "hint",
            Params.of("text", "For example, 31 3 1980"),
            "items",
            List.of(
                Params.of("name", "day", "value", application.day()),
                Params.of("name", "month", "value", application.month()),
                Params.of("name", "year", "value", application.year())));
    addError(date, errors, "date-of-birth");
    return date;
  }

  public static Params countryFields(
      LicenceApplication application, List<Validation.FieldError> errors) {
    List<Object> items = new ArrayList<>();
    int index = 0;
    for (Options.Option option : Options.countries()) {
      Params item =
          Params.of(
              "value",
              option.value(),
              "text",
              option.text(),
              "checked",
              option.value().equals(application.country()));
      if (index == 0) {
        item.set("id", "country");
      }
      items.add(item);
      index++;
    }
    Params radios =
        Params.of(
            "idPrefix",
            "country",
            "name",
            "country",
            "fieldset",
            Params.of(
                "legend",
                Params.of(
                    "text",
                    "Where will you fish?",
                    "isPageHeading",
                    true,
                    "classes",
                    "govuk-fieldset__legend--l")),
            "hint",
            Params.of("text", "This example is fictional. It does not check a real fishing area."),
            "items",
            items);
    addError(radios, errors, "country");
    return radios;
  }

  public static Params licenceFields(
      LicenceApplication application, List<Validation.FieldError> errors) {
    List<Object> items = new ArrayList<>();
    int index = 0;
    for (Options.Option option : Options.licenceLengths()) {
      Params item =
          Params.of(
              "value",
              option.value(),
              "text",
              option.text(),
              "checked",
              option.value().equals(application.licenceLength()));
      if (index == 0) {
        item.set("id", "licence-length");
      }
      items.add(item);
      index++;
    }
    Params radios =
        Params.of(
            "idPrefix",
            "licence-length",
            "name",
            "licence-length",
            "fieldset",
            Params.of(
                "legend",
                Params.of(
                    "text",
                    "How long do you need the licence for?",
                    "isPageHeading",
                    true,
                    "classes",
                    "govuk-fieldset__legend--l")),
            "items",
            items);
    addError(radios, errors, "licence-length");
    return radios;
  }

  public static Params cookieFields(String analyticsChoice, List<Validation.FieldError> errors) {
    String selected = "";
    if ("yes".equals(analyticsChoice)) {
      selected = "yes";
    } else if ("no".equals(analyticsChoice)) {
      selected = "no";
    }
    Params radios =
        Params.of(
            "idPrefix",
            "analytics",
            "name",
            "analytics",
            "fieldset",
            Params.of(
                "legend",
                Params.of(
                    "text",
                    "Do you want to accept analytics cookies?",
                    "isPageHeading",
                    true,
                    "classes",
                    "govuk-fieldset__legend--l")),
            "hint",
            Params.of(
                "text", "This example stores your choice. It does not set analytics cookies."),
            "items",
            List.of(
                Params.of(
                    "value", "yes", "text", "Yes", "id", "analytics", "checked", "yes".equals(selected)),
                Params.of("value", "no", "text", "No", "checked", "no".equals(selected))));
    addError(radios, errors, "analytics");
    return radios;
  }

  public static Params feesTable() {
    List<Object> rows = new ArrayList<>();
    for (Options.FeeRow option : Options.licenceFees()) {
      rows.add(
          List.of(
              Params.of("text", option.text()),
              Params.of("text", option.fee(), "format", "numeric")));
    }
    return Params.of(
        "caption",
        "Rod licence fees",
        "captionClasses",
        "govuk-table__caption--m",
        "firstCellIsHeader",
        true,
        "head",
        List.of(Params.of("text", "Licence"), Params.of("text", "Fee", "format", "numeric")),
        "rows",
        rows);
  }

  public static Params helpAccordion() {
    return Params.of(
        "id",
        "help",
        "items",
        List.of(
            Params.of(
                "heading",
                Params.of("text", "Who can apply"),
                "content",
                Params.of(
                    "text",
                    "You can apply if you are 13 or over and you will fish with a rod in England, Wales or Scotland.")),
            Params.of(
                "heading",
                Params.of("text", "What a licence covers"),
                "content",
                Params.of(
                    "html",
                    new TrustedHtml(
                        "<ul class=\"govuk-list govuk-list--bullet\"><li>Rod and line fishing</li><li>Up to 2 rods where the licence allows it</li><li>The dates printed on your licence</li></ul>"))),
            Params.of(
                "heading",
                Params.of("text", "If you need help to apply"),
                "content",
                Params.of(
                    "text",
                    "You can ask someone to apply for you. This example service does not offer a phone application line."))));
  }

  public static Params guidanceTabs() {
    return Params.of(
        "id",
        "guidance",
        "items",
        List.of(
            Params.of(
                "label",
                "Before you apply",
                "id",
                "before-you-apply",
                "panel",
                Params.of(
                    "html",
                    new TrustedHtml(
                        "<h2 class=\"govuk-heading-l\">Before you apply</h2><p class=\"govuk-body\">You need how long you need the licence, your name, date of birth, the country where you will fish, and your email address.</p>"))),
            Params.of(
                "label",
                "Fees",
                "id",
                "fees",
                "panel",
                Params.of(
                    "html",
                    new TrustedHtml(
                        "<h2 class=\"govuk-heading-l\">Fees</h2><p class=\"govuk-body\">Fees depend on the length of the licence. <a class=\"govuk-link\" href=\"/fees\">See licence fees</a>.</p>"))),
            Params.of(
                "label",
                "After you apply",
                "id",
                "after-you-apply",
                "panel",
                Params.of(
                    "html",
                    new TrustedHtml(
                        "<h2 class=\"govuk-heading-l\">After you apply</h2><p class=\"govuk-body\">This example shows a confirmation page with a reference number. It does not send email and it does not take payment.</p>")))));
  }

  public static Params confirmationPanel(String reference) {
    return Params.of(
        "titleText",
        "Application complete",
        "html",
        new TrustedHtml(
            "Your example reference number<br><strong>"
                + Nunjucks.escape(reference)
                + "</strong>"));
  }

  private static Params textInput(
      String id,
      String label,
      String value,
      List<Validation.FieldError> errors,
      Params extra) {
    Params field =
        Params.of("id", id, "name", id, "label", Params.of("text", label), "value", value);
    for (String key : extra.keys()) {
      field.set(key, extra.get(key));
    }
    addError(field, errors, id);
    return field;
  }

  private static void addError(Params params, List<Validation.FieldError> errors, String field) {
    String message = messageFor(errors, field);
    if (message != null) {
      params.set("errorMessage", Params.of("text", message));
    }
  }

  private static String messageFor(List<Validation.FieldError> errors, String field) {
    if (errors == null) {
      return null;
    }
    for (Validation.FieldError err : errors) {
      if (err.field().equals(field)) {
        return err.text();
      }
    }
    return null;
  }
}
