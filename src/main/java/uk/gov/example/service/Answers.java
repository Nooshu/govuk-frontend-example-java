package uk.gov.example.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import uk.gov.example.govuk.Params;

/** Presentation helpers for check-answers and the task list. */
public final class Answers {

  public record TaskSection(String heading, String idPrefix, List<Params> items) {}

  private static final DateTimeFormatter DOB =
      DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK);

  private Answers() {}

  public static List<Params> summaryRows(LicenceApplication application, LocalDate now) {
    return List.of(
        row("Name", joinName(application), "/name", "name"),
        row("Date of birth", formatDateOfBirth(application), "/date-of-birth", "date of birth"),
        row("Email address", application.email(), "/email", "email address"),
        row(
            "Contact preference",
            Options.labelFor(Options.contactOptions(), application.contactBy()),
            "/contact-preference",
            "contact preference"),
        row("Telephone number", application.telephone(), "/contact-preference", "telephone number"),
        row(
            "Where you will fish",
            formatRegions(application.regions()),
            "/where-you-will-fish",
            "where you will fish"),
        row(
            "Licence length",
            Options.labelFor(Options.licenceLengthOptions(), application.licenceLength()),
            "/licence-length",
            "licence length"),
        row(
            "Start month",
            Options.labelFor(Options.startMonths(now), application.startMonth()),
            "/start-month",
            "start month"),
        row("Address", formatAddress(application), "/address", "address"),
        row("Evidence", application.evidenceFilename(), "/evidence", "evidence"),
        row(
            "Additional details",
            application.additionalDetails(),
            "/additional-details",
            "additional details"),
        row("Password", passwordState(application), "/create-a-password", "password"));
  }

  public static List<TaskSection> taskSections(LicenceApplication application) {
    boolean ready = application.requiredComplete();
    return List.of(
        new TaskSection(
            "Personal details",
            "personal-details",
            List.of(
                task(application, "name", "Your name"),
                task(application, "date-of-birth", "Date of birth"),
                task(application, "email", "Email address"),
                task(application, "contact-preference", "Contact preference"))),
        new TaskSection(
            "Your licence",
            "your-licence",
            List.of(
                task(application, "where-you-will-fish", "Where you will fish"),
                task(application, "licence-length", "Licence length"),
                task(application, "start-month", "Start month"))),
        new TaskSection(
            "More about you",
            "more-about-you",
            List.of(
                task(application, "address", "Your address"),
                task(application, "evidence", "Concession evidence"),
                task(application, "additional-details", "Additional details"),
                task(application, "create-a-password", "Password"))),
        new TaskSection("Apply", "apply", List.of(submitTask(application, ready))));
  }

  private static Params submitTask(LicenceApplication application, boolean ready) {
    Params item = Params.of("title", Params.of("text", "Check your answers and submit"));
    if (!ready) {
      item.set("status", notStartedTag("Cannot start yet"));
    } else if (application.submitted()) {
      item.set("href", "/check-answers");
      item.set("status", Params.of("text", "Completed"));
    } else {
      item.set("href", "/check-answers");
      item.set("status", notStartedTag("Not started"));
    }
    return item;
  }

  private static Params task(LicenceApplication application, String id, String text) {
    Params status =
        application.isCompleted(id)
            ? Params.of("text", "Completed")
            : notStartedTag("Not started");
    return Params.of(
        "title", Params.of("text", text), "href", "/" + id, "status", status);
  }

  private static Params notStartedTag(String text) {
    return Params.of("tag", Params.of("text", text, "classes", "govuk-tag--grey"));
  }

  private static Params row(String key, String value, String href, String hidden) {
    String shown = value == null || value.isBlank() ? "Not provided" : value;
    return Params.of(
        "key",
        Params.of("text", key),
        "value",
        Params.of("text", shown),
        "actions",
        Params.of(
            "items",
            List.of(
                Params.of(
                    "href",
                    href + "?return=check-answers",
                    "text",
                    "Change",
                    "visuallyHiddenText",
                    hidden))));
  }

  private static String passwordState(LicenceApplication application) {
    return application.passwordCreated() ? "Set" : "";
  }

  private static String joinName(LicenceApplication application) {
    return (application.firstName() + " " + application.lastName()).trim();
  }

  private static String formatDateOfBirth(LicenceApplication application) {
    try {
      int day = Integer.parseInt(application.day());
      int month = Integer.parseInt(application.month());
      int year = Integer.parseInt(application.year());
      if (day == 0 || month == 0 || year == 0) {
        return "";
      }
      return LocalDate.of(year, month, day).format(DOB);
    } catch (Exception e) {
      return "";
    }
  }

  private static String formatRegions(List<String> selected) {
    if (selected.contains(Options.NOT_SURE)) {
      return "Not decided yet";
    }
    List<String> labels = new ArrayList<>();
    for (String region : selected) {
      labels.add(Options.labelFor(Options.regions(), region));
    }
    return String.join(", ", labels);
  }

  private static String formatAddress(LicenceApplication application) {
    List<String> parts = new ArrayList<>();
    for (String part :
        new String[] {
          application.addressLine1(),
          application.addressLine2(),
          application.town(),
          application.postcode()
        }) {
      if (part != null && !part.isBlank()) {
        parts.add(part.trim());
      }
    }
    return String.join(", ", parts);
  }
}
