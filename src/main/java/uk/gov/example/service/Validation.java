package uk.gov.example.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.regex.Pattern;

/** Server-side validation rules for the rod licence journey. */
public final class Validation {

  /** One validation failure, shaped for both the error summary and the field itself. */
  public record FieldError(String field, String href, String text) {}

  private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  private static final Pattern ONE_OR_TWO_DIGITS = Pattern.compile("^[0-9]{1,2}$");
  private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

  private Validation() {}

  public static List<FieldError> validateName(String fullName) {
    String name = clean(fullName);
    if (name.codePointCount(0, name.length()) < 2) {
      return List.of(new FieldError("full-name", "#full-name", "Enter your full name"));
    }
    if (name.codePointCount(0, name.length()) > 100) {
      return List.of(
          new FieldError(
              "full-name", "#full-name", "Full name must be 100 characters or fewer"));
    }
    return List.of();
  }

  public static List<FieldError> validateDateOfBirth(
      String day, String month, String year, LocalDate now) {
    day = clean(day);
    month = clean(month);
    year = clean(year);
    if (day.isEmpty() || month.isEmpty() || year.isEmpty()) {
      return List.of(
          new FieldError("date-of-birth", "#date-of-birth-day", "Enter your date of birth"));
    }
    if (!ONE_OR_TWO_DIGITS.matcher(day).matches()
        || !ONE_OR_TWO_DIGITS.matcher(month).matches()
        || !FOUR_DIGITS.matcher(year).matches()) {
      return List.of(
          new FieldError("date-of-birth", "#date-of-birth-day", "Enter a real date of birth"));
    }
    int dayNumber = Integer.parseInt(day);
    int monthNumber = Integer.parseInt(month);
    int yearNumber = Integer.parseInt(year);
    LocalDate date;
    try {
      date = LocalDate.of(yearNumber, monthNumber, dayNumber);
    } catch (Exception e) {
      return List.of(
          new FieldError("date-of-birth", "#date-of-birth-day", "Enter a real date of birth"));
    }
    LocalDate today = now.atStartOfDay(ZoneOffset.UTC).toLocalDate();
    if (date.isAfter(today)) {
      return List.of(
          new FieldError("date-of-birth", "#date-of-birth-day", "Date of birth must be in the past"));
    }
    if (ageOn(date, today) < 13) {
      return List.of(
          new FieldError(
              "date-of-birth",
              "#date-of-birth-day",
              "You must be at least 13 to use this example"));
    }
    return List.of();
  }

  public static List<FieldError> validateEmail(String email) {
    if (!EMAIL.matcher(clean(email)).matches()) {
      return List.of(
          new FieldError(
              "email",
              "#email",
              "Enter an email address in the correct format, like name@example.com"));
    }
    return List.of();
  }

  public static List<FieldError> validateCountry(String country) {
    for (Options.Option option : Options.countries()) {
      if (option.value().equals(country)) {
        return List.of();
      }
    }
    return List.of(new FieldError("country", "#country", "Select where you will fish"));
  }

  public static List<FieldError> validateLicenceLength(String value) {
    for (Options.Option option : Options.licenceLengths()) {
      if (option.value().equals(value)) {
        return List.of();
      }
    }
    return List.of(
        new FieldError(
            "licence-length",
            "#licence-length",
            "Select how long you need the licence for"));
  }

  public static List<FieldError> validateCookieChoice(String value) {
    if (!"yes".equals(value) && !"no".equals(value)) {
      return List.of(
          new FieldError(
              "analytics",
              "#analytics",
              "Select yes if you want to accept analytics cookies"));
    }
    return List.of();
  }

  public static String clean(String value) {
    return value == null ? "" : value.trim();
  }

  public static String asLicenceLength(String value) {
    if (LicenceApplication.LICENCE_ONE_DAY.equals(value)
        || LicenceApplication.LICENCE_EIGHT_DAYS.equals(value)
        || LicenceApplication.LICENCE_TWELVE_MONTHS.equals(value)) {
      return value;
    }
    return "";
  }

  private static int ageOn(LocalDate dob, LocalDate today) {
    int age = today.getYear() - dob.getYear();
    int monthDelta = today.getMonthValue() - dob.getMonthValue();
    if (monthDelta < 0 || (monthDelta == 0 && today.getDayOfMonth() < dob.getDayOfMonth())) {
      age--;
    }
    return age;
  }
}
