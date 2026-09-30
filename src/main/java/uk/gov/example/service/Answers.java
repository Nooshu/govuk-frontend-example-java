package uk.gov.example.service;

import java.util.List;
import uk.gov.example.govuk.Params;

/** Presentation helpers for check-answers. */
public final class Answers {

  private Answers() {}

  public static List<Params> summaryRows(LicenceApplication application) {
    return List.of(
        row(
            "Licence length",
            Options.labelFor(Options.licenceLengths(), application.licenceLength()),
            "/licence-length",
            "licence length"),
        row("Name", application.fullName(), "/name", "name"),
        row("Date of birth", formatDateOfBirth(application), "/date-of-birth", "date of birth"),
        row(
            "Where you will fish",
            application.country(),
            "/where-you-will-fish",
            "where you will fish"),
        row("Email address", application.email(), "/email", "email address"));
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

  private static String formatDateOfBirth(LicenceApplication application) {
    String day = application.day().trim();
    String month = application.month().trim();
    String year = application.year().trim();
    if (day.isEmpty() || month.isEmpty() || year.isEmpty()) {
      return "";
    }
    return day + " " + month + " " + year;
  }
}
