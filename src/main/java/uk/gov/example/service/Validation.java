package uk.gov.example.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Server-side validation rules for the rod licence journey. */
public final class Validation {

  /** One validation failure, shaped for both the error summary and the field itself. */
  public record FieldError(String field, String href, String text) {}

  private static final Pattern EMAIL =
      Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  private static final Pattern PHONE = Pattern.compile("^[0-9+() -]{8,20}$");
  private static final Pattern POSTCODE =
      Pattern.compile("^[A-Z]{1,2}[0-9][A-Z0-9]? [0-9][A-Z]{2}$");
  private static final Pattern EVIDENCE = Pattern.compile("(?i)^.+\\.(pdf|png|jpe?g)$");
  private static final Pattern ONE_OR_TWO_DIGITS = Pattern.compile("^[0-9]{1,2}$");
  private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");
  private static final Pattern FILENAME = Pattern.compile("^[\\w. -]+$");

  private Validation() {}

  public static List<FieldError> validateName(String firstName, String lastName) {
    List<FieldError> errors = new ArrayList<>();
    appendNamePart(errors, "first-name", "First name", "Enter your first name", firstName);
    appendNamePart(errors, "last-name", "Last name", "Enter your last name", lastName);
    return errors;
  }

  private static void appendNamePart(
      List<FieldError> errors, String field, String label, String missing, String value) {
    String trimmed = clean(value);
    if (trimmed.isEmpty()) {
      errors.add(new FieldError(field, "#" + field, missing));
    } else if (trimmed.codePointCount(0, trimmed.length()) > 100) {
      errors.add(
          new FieldError(field, "#" + field, label + " must be 100 characters or fewer"));
    }
  }

  public static List<FieldError> validateDateOfBirth(
      String day, String month, String year, LocalDate now) {
    day = clean(day);
    month = clean(month);
    year = clean(year);
    if (day.isEmpty() || month.isEmpty() || year.isEmpty()) {
      return List.of(
          new FieldError(
              "date-of-birth",
              "#date-of-birth-day",
              "Date of birth must include a day, month and year"));
    }
    if (!ONE_OR_TWO_DIGITS.matcher(day).matches()
        || !ONE_OR_TWO_DIGITS.matcher(month).matches()
        || !FOUR_DIGITS.matcher(year).matches()) {
      return List.of(
          new FieldError("date-of-birth", "#date-of-birth-day", "Date of birth must be a real date"));
    }
    int dayNumber = Integer.parseInt(day);
    int monthNumber = Integer.parseInt(month);
    int yearNumber = Integer.parseInt(year);
    LocalDate date;
    try {
      date = LocalDate.of(yearNumber, monthNumber, dayNumber);
    } catch (Exception e) {
      return List.of(
          new FieldError("date-of-birth", "#date-of-birth-day", "Date of birth must be a real date"));
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
              "You must be 13 or over to apply for a rod licence"));
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

  public static List<FieldError> validateContactPreference(String contactBy, String telephone) {
    List<FieldError> errors = new ArrayList<>();
    if (!LicenceApplication.CONTACT_BY_EMAIL.equals(contactBy)
        && !LicenceApplication.CONTACT_BY_TELEPHONE.equals(contactBy)) {
      errors.add(
          new FieldError("contact-by", "#contact-by", "Select how we should contact you"));
    }
    String trimmed = clean(telephone);
    if (LicenceApplication.CONTACT_BY_TELEPHONE.equals(contactBy) && trimmed.isEmpty()) {
      errors.add(new FieldError("telephone", "#telephone", "Enter a telephone number"));
    } else if (!trimmed.isEmpty() && !PHONE.matcher(trimmed).matches()) {
      errors.add(
          new FieldError(
              "telephone", "#telephone", "Enter a telephone number, like 01632 960 001"));
    }
    return errors;
  }

  public static List<FieldError> validateRegions(List<String> selected) {
    if (selected == null || selected.isEmpty()) {
      return List.of(new FieldError("regions", "#regions", "Select where you will fish"));
    }
    Map<String, Boolean> known = new HashMap<>();
    for (Options.Option region : Options.regions()) {
      known.put(region.value(), true);
    }
    List<String> chosen = new ArrayList<>();
    boolean exclusive = false;
    for (String region : selected) {
      if (Options.NOT_SURE.equals(region)) {
        exclusive = true;
        continue;
      }
      chosen.add(region);
    }
    if (exclusive && !chosen.isEmpty()) {
      return List.of(
          new FieldError(
              "regions",
              "#regions",
              "Select where you will fish, or select that you have not decided yet"));
    }
    for (String region : chosen) {
      if (!known.containsKey(region)) {
        return List.of(new FieldError("regions", "#regions", "Select where you will fish"));
      }
    }
    return List.of();
  }

  public static List<FieldError> validateLicenceLength(String value) {
    for (Options.LicenceOption option : Options.licenceLengths()) {
      if (option.value().equals(value)) {
        return List.of();
      }
    }
    return List.of(
        new FieldError(
            "licence-length", "#licence-length", "Select how long you need a licence for"));
  }

  public static List<FieldError> validateStartMonth(String value, LocalDate now) {
    for (Options.Option month : Options.startMonths(now)) {
      if (month.value().equals(value)) {
        return List.of();
      }
    }
    return List.of(
        new FieldError("start-month", "#start-month", "Select when the licence should start"));
  }

  public static List<FieldError> validateAddress(String line1, String town, String postcode) {
    List<FieldError> errors = new ArrayList<>();
    String trimmedLine1 = clean(line1);
    if (trimmedLine1.isEmpty()) {
      errors.add(new FieldError("address-line-1", "#address-line-1", "Enter address line 1"));
    } else if (trimmedLine1.codePointCount(0, trimmedLine1.length()) > 100) {
      errors.add(
          new FieldError(
              "address-line-1",
              "#address-line-1",
              "Address line 1 must be 100 characters or fewer"));
    }
    if (clean(town).isEmpty()) {
      errors.add(new FieldError("town", "#town", "Enter a town or city"));
    }
    String normalised = normalisePostcode(postcode);
    if (normalised.isEmpty()) {
      errors.add(new FieldError("postcode", "#postcode", "Enter a full UK postcode"));
    } else if (!POSTCODE.matcher(normalised).matches()) {
      errors.add(new FieldError("postcode", "#postcode", "Enter a full UK postcode"));
    }
    return errors;
  }

  public static List<FieldError> validateEvidence(String filename) {
    if (filename == null || filename.isEmpty()) {
      return List.of();
    }
    if (!EVIDENCE.matcher(filename).matches()) {
      return List.of(
          new FieldError(
              "evidence", "#evidence", "The selected file must be a PDF, PNG, or JPG"));
    }
    return List.of();
  }

  public static List<FieldError> validateAdditionalDetails(String value) {
    if (value != null && value.codePointCount(0, value.length()) > 200) {
      return List.of(
          new FieldError(
              "additional-details",
              "#additional-details",
              "Additional details must be 200 characters or fewer"));
    }
    return List.of();
  }

  public static List<FieldError> validatePassword(String password, String confirm) {
    if (password == null || password.codePointCount(0, password.length()) < 8) {
      return List.of(
          new FieldError("password", "#password", "Password must be at least 8 characters"));
    }
    if (!password.equals(confirm)) {
      return List.of(
          new FieldError(
              "password-confirm",
              "#password-confirm",
              "Enter the same password in both fields"));
    }
    return List.of();
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

  public static String normalisePostcode(String value) {
    String compact = clean(value).toUpperCase().replace(" ", "");
    if (compact.length() < 5) {
      return "";
    }
    return compact.substring(0, compact.length() - 3) + " " + compact.substring(compact.length() - 3);
  }

  public static String clean(String value) {
    return value == null ? "" : value.trim();
  }

  public static String asContactBy(String value) {
    if (LicenceApplication.CONTACT_BY_EMAIL.equals(value)
        || LicenceApplication.CONTACT_BY_TELEPHONE.equals(value)) {
      return value;
    }
    return "";
  }

  public static String asLicenceLength(String value) {
    if (LicenceApplication.LICENCE_ONE_DAY.equals(value)
        || LicenceApplication.LICENCE_EIGHT_DAY.equals(value)
        || LicenceApplication.LICENCE_TWELVE_MTH.equals(value)) {
      return value;
    }
    return "";
  }

  /** Returns the safe base name of an upload, or empty when unsafe. */
  public static String safeFilename(String filename) {
    if (filename == null) {
      return "";
    }
    String base = filename;
    int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));
    if (slash != -1) {
      base = base.substring(slash + 1);
    }
    if (base.isEmpty() || ".".equals(base) || "..".equals(base)) {
      return "";
    }
    if (base.codePointCount(0, base.length()) > 120 || !FILENAME.matcher(base).matches()) {
      return "";
    }
    return base;
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
