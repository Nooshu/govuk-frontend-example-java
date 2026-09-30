package uk.gov.example.service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Rod fishing licence application answers and journey step graph. */
public final class LicenceApplication {

  public static final String LICENCE_ONE_DAY = "1-day";
  public static final String LICENCE_EIGHT_DAYS = "8-days";
  public static final String LICENCE_TWELVE_MONTHS = "12-months";

  public record Step(String id, String path, String heading) {}

  private static final List<Step> STEPS =
      List.of(
          new Step(
              "licence-length",
              "/licence-length",
              "How long do you need the licence for?"),
          new Step("name", "/name", "What is your full name?"),
          new Step("date-of-birth", "/date-of-birth", "What is your date of birth?"),
          new Step("where-you-will-fish", "/where-you-will-fish", "Where will you fish?"),
          new Step("email", "/email", "What is your email address?"));

  private String licenceLength = "";
  private String fullName = "";
  private String day = "";
  private String month = "";
  private String year = "";
  private String country = "";
  private String email = "";
  private boolean submitted;
  private String reference = "";
  private final LinkedHashSet<String> completed = new LinkedHashSet<>();

  public static List<Step> steps() {
    return STEPS;
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
      if (!isCompleted(step.id())) {
        return false;
      }
    }
    return true;
  }

  /** First question that is not complete, or empty when the applicant may check answers. */
  public static Optional<Step> firstIncompleteStep(LicenceApplication application) {
    for (Step step : STEPS) {
      if (!application.isCompleted(step.id())) {
        return Optional.of(step);
      }
    }
    return Optional.empty();
  }

  /** Clears all answers and completion so a new application can start in the same session. */
  public void reset() {
    licenceLength = "";
    fullName = "";
    day = "";
    month = "";
    year = "";
    country = "";
    email = "";
    submitted = false;
    reference = "";
    completed.clear();
  }

  public String licenceLength() {
    return licenceLength;
  }

  public void setLicenceLength(String licenceLength) {
    this.licenceLength = Objects.requireNonNullElse(licenceLength, "");
  }

  public String fullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = Objects.requireNonNullElse(fullName, "");
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

  public String country() {
    return country;
  }

  public void setCountry(String country) {
    this.country = Objects.requireNonNullElse(country, "");
  }

  public String email() {
    return email;
  }

  public void setEmail(String email) {
    this.email = Objects.requireNonNullElse(email, "");
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
