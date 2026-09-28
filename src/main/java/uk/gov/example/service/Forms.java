package uk.gov.example.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import uk.gov.example.govuk.Nunjucks;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.Render;
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

  public static Map<String, Params> nameFields(
      LicenceApplication application, List<Validation.FieldError> errors) {
    return Map.of(
        "firstName",
        textInput(
            "first-name",
            "First name",
            application.firstName(),
            errors,
            Params.of(
                "autocomplete", "given-name",
                "classes", "govuk-input--width-20",
                "spellcheck", false)),
        "lastName",
        textInput(
            "last-name",
            "Last name",
            application.lastName(),
            errors,
            Params.of(
                "autocomplete", "family-name",
                "classes", "govuk-input--width-20",
                "spellcheck", false)));
  }

  public static Params emailField(
      LicenceApplication application, List<Validation.FieldError> errors) {
    return textInput(
        "email",
        "Email address",
        application.email(),
        errors,
        Params.of(
            "type",
            "email",
            "autocomplete",
            "email",
            "spellcheck",
            false,
            "classes",
            "govuk-input--width-20",
            "hint",
            Params.of("text", "We will send the decision to this address"),
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
                Params.of("name", "day", "autocomplete", "bday-day", "value", application.day()),
                Params.of(
                    "name", "month", "autocomplete", "bday-month", "value", application.month()),
                Params.of(
                    "name", "year", "autocomplete", "bday-year", "value", application.year())));
    addError(date, errors, "date-of-birth");
    return date;
  }

  public static Params contactFields(
      LicenceApplication application, List<Validation.FieldError> errors) {
    Params telephone =
        textInput(
            "telephone",
            "Telephone number",
            application.telephone(),
            errors,
            Params.of("type", "tel", "autocomplete", "tel", "classes", "govuk-input--width-20"));
    String conditional = Render.mustRender("input", telephone);
    List<Object> items = new ArrayList<>();
    for (Options.Option option : Options.contactOptions()) {
      if (LicenceApplication.CONTACT_BY_TELEPHONE.equals(option.value())) {
        items.add(
            Params.of(
                "value",
                option.value(),
                "text",
                option.text(),
                "checked",
                LicenceApplication.CONTACT_BY_TELEPHONE.equals(application.contactBy()),
                "conditional",
                Params.of("html", new TrustedHtml(conditional))));
      } else {
        items.add(
            Params.of(
                "value",
                option.value(),
                "text",
                option.text(),
                "id",
                "contact-by",
                "checked",
                option.value().equals(application.contactBy())));
      }
    }
    Params radios =
        Params.of(
            "idPrefix",
            "contact-by",
            "name",
            "contact-by",
            "fieldset",
            Params.of(
                "legend",
                Params.of(
                    "text",
                    "How should we contact you?",
                    "isPageHeading",
                    true,
                    "classes",
                    "govuk-fieldset__legend--l")),
            "hint",
            Params.of("text", "We will use this if we need to ask about your application"),
            "items",
            items);
    addError(radios, errors, "contact-by");
    return radios;
  }

  public static Params regionFields(
      LicenceApplication application, List<Validation.FieldError> errors) {
    Map<String, Boolean> chosen = new HashMap<>();
    for (String region : application.regions()) {
      chosen.put(region, true);
    }
    List<Object> items = new ArrayList<>();
    int index = 0;
    for (Options.Option region : Options.regions()) {
      Params item =
          Params.of(
              "value",
              region.value(),
              "text",
              region.text(),
              "checked",
              Boolean.TRUE.equals(chosen.get(region.value())));
      if (index == 0) {
        item.set("id", "regions");
      }
      items.add(item);
      index++;
    }
    items.add(Params.of("divider", "or"));
    items.add(
        Params.of(
            "value",
            Options.NOT_SURE,
            "text",
            "I have not decided yet",
            "behaviour",
            "exclusive",
            "checked",
            Boolean.TRUE.equals(chosen.get(Options.NOT_SURE))));
    Params checkboxes =
        Params.of(
            "idPrefix",
            "where",
            "name",
            "regions",
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
            Params.of("text", "Select all that apply"),
            "items",
            items);
    addError(checkboxes, errors, "regions");
    return checkboxes;
  }

  public static Params licenceFields(
      LicenceApplication application, List<Validation.FieldError> errors) {
    List<Object> items = new ArrayList<>();
    int index = 0;
    for (Options.LicenceOption option : Options.licenceLengths()) {
      Params item =
          Params.of(
              "value",
              option.value(),
              "text",
              option.text() + " (" + option.fee() + ")",
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
                    "How long do you need a licence for?",
                    "isPageHeading",
                    true,
                    "classes",
                    "govuk-fieldset__legend--l")),
            "items",
            items);
    addError(radios, errors, "licence-length");
    return radios;
  }

  public static Params monthField(
      LicenceApplication application, List<Validation.FieldError> errors, LocalDate now) {
    List<Options.Option> months = Options.startMonths(now);
    List<Object> items = new ArrayList<>();
    items.add(
        Params.of(
            "value", "", "text", "Select a month", "selected", application.startMonth().isEmpty()));
    for (Options.Option month : months) {
      items.add(
          Params.of(
              "value",
              month.value(),
              "text",
              month.text(),
              "selected",
              month.value().equals(application.startMonth())));
    }
    Params field =
        Params.of(
            "id",
            "start-month",
            "name",
            "start-month",
            "label",
            Params.of(
                "text",
                "When should the licence start?",
                "isPageHeading",
                true,
                "classes",
                "govuk-label--l"),
            "items",
            items);
    addError(field, errors, "start-month");
    return field;
  }

  public static Map<String, Params> addressFields(
      LicenceApplication application, List<Validation.FieldError> errors) {
    Map<String, Params> fields = new HashMap<>();
    fields.put(
        "fieldset",
        Params.of(
            "legend",
            Params.of(
                "text",
                "What is your address?",
                "isPageHeading",
                true,
                "classes",
                "govuk-fieldset__legend--l")));
    fields.put(
        "line1",
        textInput(
            "address-line-1",
            "Address line 1",
            application.addressLine1(),
            errors,
            Params.of("autocomplete", "address-line1")));
    fields.put(
        "line2",
        textInput(
            "address-line-2",
            "Address line 2 (optional)",
            application.addressLine2(),
            errors,
            Params.of("autocomplete", "address-line2")));
    fields.put(
        "town",
        textInput(
            "town",
            "Town or city",
            application.town(),
            errors,
            Params.of("autocomplete", "address-level2", "classes", "govuk-input--width-20")));
    fields.put(
        "postcode",
        textInput(
            "postcode",
            "Postcode",
            application.postcode(),
            errors,
            Params.of(
                "autocomplete",
                "postal-code",
                "classes",
                "govuk-input--width-10",
                "spellcheck",
                false)));
    fields.put(
        "inset",
        Params.of(
            "text",
            "This example asks you to type your address. It does not look up addresses from a postcode."));
    return fields;
  }

  public static Params evidenceField(
      LicenceApplication application, List<Validation.FieldError> errors) {
    Params upload =
        Params.of(
            "id",
            "evidence",
            "name",
            "evidence",
            "label",
            Params.of(
                "text",
                "Upload evidence of a concession",
                "isPageHeading",
                true,
                "classes",
                "govuk-label--l"),
            "hint",
            Params.of(
                "text",
                "PDF, PNG, or JPG. You can skip this question if you do not have a concession."));
    addError(upload, errors, "evidence");
    return upload;
  }

  public static Params detailsField(
      LicenceApplication application, List<Validation.FieldError> errors) {
    Params details =
        Params.of(
            "name",
            "additional-details",
            "id",
            "additional-details",
            "maxlength",
            200,
            "threshold",
            75,
            "value",
            application.additionalDetails(),
            "label",
            Params.of(
                "text",
                "Is there anything else we should know?",
                "isPageHeading",
                true,
                "classes",
                "govuk-label--l"),
            "hint",
            Params.of(
                "text",
                "You can skip this question. Do not include payment card numbers or passwords."));
    addError(details, errors, "additional-details");
    return details;
  }

  public static Map<String, Params> passwordFields(List<Validation.FieldError> errors) {
    Params password =
        Params.of(
            "id",
            "password",
            "name",
            "password",
            "autocomplete",
            "new-password",
            "label",
            Params.of(
                "text", "Create a password", "isPageHeading", true, "classes", "govuk-label--l"),
            "hint",
            Params.of(
                "text",
                "Must be at least 8 characters. This example does not store your password."));
    addError(password, errors, "password");
    Params confirm =
        Params.of(
            "id",
            "password-confirm",
            "name",
            "password-confirm",
            "autocomplete",
            "new-password",
            "label",
            Params.of("text", "Confirm password"));
    addError(confirm, errors, "password-confirm");
    return Map.of("password", password, "confirm", confirm);
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
    for (Options.LicenceOption option : Options.licenceLengths()) {
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
                    "You can apply if you are 13 or over and you will fish with a rod in England or Wales.")),
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
                        "<h2 class=\"govuk-heading-l\">Before you apply</h2><p class=\"govuk-body\">You need your name, date of birth, email address, and home address.</p>"))),
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
            "Your reference number<br><strong>" + Nunjucks.escape(reference) + "</strong>"));
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
