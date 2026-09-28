package uk.gov.example.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Static option lists (regions, licence lengths, months). */
public final class Options {

  /** Checkbox value for an applicant who has not decided where they will fish. */
  public static final String NOT_SURE = "not-sure";

  public record Option(String value, String text) {}

  public record LicenceOption(String value, String text, String fee) {}

  private static final List<Option> REGIONS =
      List.of(
          new Option("north-west", "North West"),
          new Option("north-east", "North East"),
          new Option("midlands", "Midlands"),
          new Option("south-west", "South West"),
          new Option("south-east", "South East"),
          new Option("wales", "Wales"));

  private static final List<LicenceOption> LICENCE_LENGTHS =
      List.of(
          new LicenceOption(LicenceApplication.LICENCE_ONE_DAY, "1 day", "£7.10"),
          new LicenceOption(LicenceApplication.LICENCE_EIGHT_DAY, "8 days", "£14.20"),
          new LicenceOption(LicenceApplication.LICENCE_TWELVE_MTH, "12 months", "£36.80"));

  private static final List<Option> CONTACT_OPTIONS =
      List.of(
          new Option(LicenceApplication.CONTACT_BY_EMAIL, "Email"),
          new Option(LicenceApplication.CONTACT_BY_TELEPHONE, "Telephone"));

  private static final DateTimeFormatter MONTH_LABEL =
      DateTimeFormatter.ofPattern("MMMM yyyy", Locale.UK);

  private Options() {}

  public static List<Option> regions() {
    return REGIONS;
  }

  public static List<LicenceOption> licenceLengths() {
    return LICENCE_LENGTHS;
  }

  public static List<Option> contactOptions() {
    return CONTACT_OPTIONS;
  }

  public static List<Option> licenceLengthOptions() {
    List<Option> options = new ArrayList<>(LICENCE_LENGTHS.size());
    for (LicenceOption length : LICENCE_LENGTHS) {
      options.add(new Option(length.value(), length.text()));
    }
    return options;
  }

  /** Next 12 months the licence can start, counting from the month {@code now} falls in (UTC). */
  public static List<Option> startMonths(LocalDate now) {
    YearMonth start = YearMonth.from(now.atStartOfDay(ZoneOffset.UTC).toLocalDate());
    List<Option> months = new ArrayList<>(12);
    for (int index = 0; index < 12; index++) {
      YearMonth month = start.plusMonths(index);
      months.add(
          new Option(
              String.format("%04d-%02d", month.getYear(), month.getMonthValue()),
              month.atDay(1).format(MONTH_LABEL)));
    }
    return months;
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
