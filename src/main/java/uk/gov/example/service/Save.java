package uk.gov.example.service;

/**
 * Persist validated answers onto a {@link LicenceApplication}.
 *
 * <p>Invalid answers are still stored so the question can be shown back with values retained;
 * completion is only marked when {@code valid} is true.
 */
public final class Save {

  private Save() {}

  public static void saveName(LicenceApplication application, String fullName, boolean valid) {
    application.setFullName(Validation.clean(fullName));
    finish(application, "name", valid);
  }

  public static void saveDate(
      LicenceApplication application, String day, String month, String year, boolean valid) {
    application.setDay(Validation.clean(day));
    application.setMonth(Validation.clean(month));
    application.setYear(Validation.clean(year));
    finish(application, "date-of-birth", valid);
  }

  public static void saveEmail(LicenceApplication application, String email, boolean valid) {
    application.setEmail(Validation.clean(email));
    finish(application, "email", valid);
  }

  public static void saveCountry(LicenceApplication application, String country, boolean valid) {
    application.setCountry(Validation.clean(country));
    finish(application, "where-you-will-fish", valid);
  }

  public static void saveLicence(LicenceApplication application, String value, boolean valid) {
    application.setLicenceLength(Validation.asLicenceLength(value));
    finish(application, "licence-length", valid);
  }

  private static void finish(LicenceApplication application, String id, boolean valid) {
    if (valid) {
      application.markCompleted(id);
    } else {
      application.unmarkCompleted(id);
    }
  }
}
