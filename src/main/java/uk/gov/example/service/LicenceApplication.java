package uk.gov.example.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Rod fishing licence application answers and journey step graph. */
public final class LicenceApplication {

  public static final String CONTACT_BY_EMAIL = "email";
  public static final String CONTACT_BY_TELEPHONE = "telephone";

  public static final String LICENCE_ONE_DAY = "1-day";
  public static final String LICENCE_EIGHT_DAY = "8-day";
  public static final String LICENCE_TWELVE_MTH = "12-month";

  public record Step(String id, String path, String heading) {}

  private static final List<Step> STEPS =
      List.of(
          new Step("name", "/name", "What is your name?"),
          new Step("date-of-birth", "/date-of-birth", "What is your date of birth?"),
          new Step("email", "/email", "What is your email address?"),
          new Step("contact-preference", "/contact-preference", "How should we contact you?"),
          new Step("where-you-will-fish", "/where-you-will-fish", "Where will you fish?"),
          new Step("licence-length", "/licence-length", "How long do you need a licence for?"),
          new Step("start-month", "/start-month", "When should the licence start?"),
          new Step("address", "/address", "What is your address?"),
          new Step("evidence", "/evidence", "Upload evidence of a concession"),
          new Step(
              "additional-details",
              "/additional-details",
              "Is there anything else we should know?"),
          new Step("create-a-password", "/create-a-password", "Create a password"));

  private static final Set<String> OPTIONAL =
      Set.of("evidence", "additional-details");

  private String firstName = "";
  private String lastName = "";
  private String day = "";
  private String month = "";
  private String year = "";
  private String email = "";
  private String contactBy = "";
  private String telephone = "";
  private List<String> regions = new ArrayList<>();
  private String licenceLength = "";
  private String startMonth = "";
  private String addressLine1 = "";
  private String addressLine2 = "";
  private String town = "";
  private String postcode = "";
  private String evidenceFilename = "";
  private String additionalDetails = "";
  private boolean passwordCreated;
  private boolean submitted;
  private String reference = "";
  private final LinkedHashSet<String> completed = new LinkedHashSet<>();

  public static List<Step> steps() {
    return STEPS;
  }

  public static boolean optional(String id) {
    return OPTIONAL.contains(id);
  }

  public static Optional<Step> stepById(String id) {
    return STEPS.stream().filter(s -> s.id().equals(id)).findFirst();
  }

  public static Optional<Step> stepByPath(String path) {
    return STEPS.stream().filter(s -> s.path().equals(path)).findFirst();
  }

  public static Optional<Step> nextStep(String id) {
    int index = indexOf(id);
    if (index < 0 || index + 1 >= STEPS.size()) {
      return Optional.empty();
    }
    return Optional.of(STEPS.get(index + 1));
  }

  public static Optional<Step> previousStep(String id) {
    int index = indexOf(id);
    if (index <= 0) {
      return Optional.empty();
    }
    return Optional.of(STEPS.get(index - 1));
  }

  private static int indexOf(String id) {
    for (int i = 0; i < STEPS.size(); i++) {
      if (STEPS.get(i).id().equals(id)) {
        return i;
      }
    }
    return -1;
  }

  public boolean isCompleted(String id) {
    return completed.contains(id);
  }

  public void markCompleted(String id) {
    completed.add(id);
  }

  public void unmarkCompleted(String id) {
    completed.remove(id);
  }

  public Set<String> completed() {
    return Collections.unmodifiableSet(completed);
  }

  public boolean requiredComplete() {
    for (Step step : STEPS) {
      if (!optional(step.id()) && !isCompleted(step.id())) {
        return false;
      }
    }
    return true;
  }

  /** First required question that is not complete, or empty when the applicant may check answers. */
  public static Optional<Step> firstIncompleteStep(LicenceApplication application) {
    for (Step step : STEPS) {
      if (!optional(step.id()) && !application.isCompleted(step.id())) {
        return Optional.of(step);
      }
    }
    return Optional.empty();
  }

  /** Clears all answers and completion so a new application can start in the same session. */
  public void reset() {
    firstName = "";
    lastName = "";
    day = "";
    month = "";
    year = "";
    email = "";
    contactBy = "";
    telephone = "";
    regions = new ArrayList<>();
    licenceLength = "";
    startMonth = "";
    addressLine1 = "";
    addressLine2 = "";
    town = "";
    postcode = "";
    evidenceFilename = "";
    additionalDetails = "";
    passwordCreated = false;
    submitted = false;
    reference = "";
    completed.clear();
  }

  public String firstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = Objects.requireNonNullElse(firstName, "");
  }

  public String lastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = Objects.requireNonNullElse(lastName, "");
  }

  public String day() {
    return day;
  }

  public void setDay(String day) {
    this.day = Objects.requireNonNullElse(day, "");
  }

  public String month() {
    return month;
  }

  public void setMonth(String month) {
    this.month = Objects.requireNonNullElse(month, "");
  }

  public String year() {
    return year;
  }

  public void setYear(String year) {
    this.year = Objects.requireNonNullElse(year, "");
  }

  public String email() {
    return email;
  }

  public void setEmail(String email) {
    this.email = Objects.requireNonNullElse(email, "");
  }

  public String contactBy() {
    return contactBy;
  }

  public void setContactBy(String contactBy) {
    this.contactBy = Objects.requireNonNullElse(contactBy, "");
  }

  public String telephone() {
    return telephone;
  }

  public void setTelephone(String telephone) {
    this.telephone = Objects.requireNonNullElse(telephone, "");
  }

  public List<String> regions() {
    return Collections.unmodifiableList(regions);
  }

  public void setRegions(List<String> regions) {
    this.regions = new ArrayList<>(regions == null ? List.of() : regions);
  }

  public String licenceLength() {
    return licenceLength;
  }

  public void setLicenceLength(String licenceLength) {
    this.licenceLength = Objects.requireNonNullElse(licenceLength, "");
  }

  public String startMonth() {
    return startMonth;
  }

  public void setStartMonth(String startMonth) {
    this.startMonth = Objects.requireNonNullElse(startMonth, "");
  }

  public String addressLine1() {
    return addressLine1;
  }

  public void setAddressLine1(String addressLine1) {
    this.addressLine1 = Objects.requireNonNullElse(addressLine1, "");
  }

  public String addressLine2() {
    return addressLine2;
  }

  public void setAddressLine2(String addressLine2) {
    this.addressLine2 = Objects.requireNonNullElse(addressLine2, "");
  }

  public String town() {
    return town;
  }

  public void setTown(String town) {
    this.town = Objects.requireNonNullElse(town, "");
  }

  public String postcode() {
    return postcode;
  }

  public void setPostcode(String postcode) {
    this.postcode = Objects.requireNonNullElse(postcode, "");
  }

  public String evidenceFilename() {
    return evidenceFilename;
  }

  public void setEvidenceFilename(String evidenceFilename) {
    this.evidenceFilename = Objects.requireNonNullElse(evidenceFilename, "");
  }

  public String additionalDetails() {
    return additionalDetails;
  }

  public void setAdditionalDetails(String additionalDetails) {
    this.additionalDetails = Objects.requireNonNullElse(additionalDetails, "");
  }

  public boolean passwordCreated() {
    return passwordCreated;
  }

  public void setPasswordCreated(boolean passwordCreated) {
    this.passwordCreated = passwordCreated;
  }

  public boolean submitted() {
    return submitted;
  }

  public void setSubmitted(boolean submitted) {
    this.submitted = submitted;
  }

  public String reference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = Objects.requireNonNullElse(reference, "");
  }
}
