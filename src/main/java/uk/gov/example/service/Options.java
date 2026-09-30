package uk.gov.example.service;

import java.util.List;

/** Static option lists (countries, licence lengths, fees). */
public final class Options {

  public record Option(String value, String text) {}

  public record FeeRow(String text, String fee) {}

  private static final List<Option> COUNTRIES =
      List.of(
          new Option("England", "England"),
          new Option("Wales", "Wales"),
          new Option("Scotland", "Scotland"));

  private static final List<Option> LICENCE_LENGTHS =
      List.of(
          new Option(LicenceApplication.LICENCE_ONE_DAY, "1 day"),
          new Option(LicenceApplication.LICENCE_EIGHT_DAYS, "8 days"),
          new Option(LicenceApplication.LICENCE_TWELVE_MONTHS, "12 months"));

  private static final List<FeeRow> LICENCE_FEES =
      List.of(
          new FeeRow("1 day", "£7.10"),
          new FeeRow("8 days", "£14.20"),
          new FeeRow("12 months", "£36.80"));

  private Options() {}

  public static List<Option> countries() {
    return COUNTRIES;
  }

  public static List<Option> licenceLengths() {
    return LICENCE_LENGTHS;
  }

  public static List<FeeRow> licenceFees() {
    return LICENCE_FEES;
  }

  /** Label for a selected value; unknown values are returned unchanged. */
  public static String labelFor(List<Option> options, String value) {
    for (Option option : options) {
      if (option.value().equals(value)) {
        return option.text();
      }
    }
    return value == null ? "" : value;
  }
}
