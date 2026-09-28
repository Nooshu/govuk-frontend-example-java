package uk.gov.example.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persist validated answers onto a {@link LicenceApplication}.
 *
 * <p>Invalid answers are still stored so the question can be shown back with values retained;
 * completion is only marked when {@code valid} is true.
 */
public final class Save {

  public record Address(String line1, String line2, String town, String postcode) {}

  private Save() {}

  public static void saveName(
      LicenceApplication application, String firstName, String lastName, boolean valid) {
    application.setFirstName(Validation.clean(firstName));
    application.setLastName(Validation.clean(lastName));
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

  public static void saveContact(
      LicenceApplication application, String contactBy, String telephone, boolean valid) {
    application.setContactBy(Validation.asContactBy(contactBy));
    application.setTelephone(Validation.clean(telephone));
    finish(application, "contact-preference", valid);
  }

  public static void saveRegions(
      LicenceApplication application, List<String> selected, boolean valid) {
    Map<String, Boolean> known = new HashMap<>();
    known.put(Options.NOT_SURE, true);
    for (Options.Option region : Options.regions()) {
      known.put(region.value(), true);
    }
    List<String> kept = new ArrayList<>();
    if (selected != null) {
      for (String region : selected) {
        if (known.containsKey(region)) {
          kept.add(region);
        }
      }
    }
    application.setRegions(kept);
    finish(application, "where-you-will-fish", valid);
  }

  public static void saveLicence(LicenceApplication application, String value, boolean valid) {
    application.setLicenceLength(Validation.asLicenceLength(value));
    finish(application, "licence-length", valid);
  }

  public static void saveMonth(LicenceApplication application, String value, boolean valid) {
    application.setStartMonth(value == null ? "" : value);
    finish(application, "start-month", valid);
  }

  public static void saveAddress(LicenceApplication application, Address values, boolean valid) {
    application.setAddressLine1(Validation.clean(values.line1()));
    application.setAddressLine2(Validation.clean(values.line2()));
    application.setTown(Validation.clean(values.town()));
    if (valid) {
      application.setPostcode(Validation.normalisePostcode(values.postcode()));
    } else {
      application.setPostcode(Validation.clean(values.postcode()));
    }
    finish(application, "address", valid);
  }

  public static void saveEvidence(
      LicenceApplication application, String filename, boolean hasFile, boolean valid) {
    if (hasFile && valid) {
      application.setEvidenceFilename(filename);
    }
    finish(application, "evidence", valid);
  }

  public static void saveDetails(LicenceApplication application, String value, boolean valid) {
    application.setAdditionalDetails(value == null ? "" : value);
    finish(application, "additional-details", valid);
  }

  public static void savePassword(LicenceApplication application, boolean valid) {
    application.setPasswordCreated(valid);
    finish(application, "create-a-password", valid);
  }

  private static void finish(LicenceApplication application, String id, boolean valid) {
    if (valid) {
      application.markCompleted(id);
    } else {
      application.unmarkCompleted(id);
    }
  }
}
