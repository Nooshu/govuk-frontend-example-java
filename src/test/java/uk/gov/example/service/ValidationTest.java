package uk.gov.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ValidationTest {

  @Test
  void validatesName() {
    assertThat(Validation.validateName("")).isNotEmpty();
    assertThat(Validation.validateName("A")).isNotEmpty();
    assertThat(Validation.validateName("Jane Doe")).isEmpty();
    assertThat(Validation.validateName("a".repeat(101))).isNotEmpty();
  }

  @Test
  void validatesDateOfBirth() {
    LocalDate now = LocalDate.of(2026, 9, 28);
    assertThat(Validation.validateDateOfBirth("", "1", "2000", now))
        .extracting(Validation.FieldError::text)
        .contains("Enter your date of birth");
    assertThat(Validation.validateDateOfBirth("31", "2", "2000", now))
        .extracting(Validation.FieldError::text)
        .contains("Enter a real date of birth");
    assertThat(Validation.validateDateOfBirth("1", "1", "2099", now)).isNotEmpty();
    assertThat(Validation.validateDateOfBirth("1", "1", "2020", now)).isNotEmpty();
    assertThat(Validation.validateDateOfBirth("1", "1", "2000", now)).isEmpty();
  }

  @Test
  void validatesEmailCountryAndLicence() {
    assertThat(Validation.validateEmail("bad")).isNotEmpty();
    assertThat(Validation.validateEmail("a@b.co")).isEmpty();
    assertThat(Validation.validateCountry("")).isNotEmpty();
    assertThat(Validation.validateCountry("France")).isNotEmpty();
    assertThat(Validation.validateCountry("England")).isEmpty();
    assertThat(Validation.validateLicenceLength("nope")).isNotEmpty();
    assertThat(Validation.validateLicenceLength("1-day")).isEmpty();
    assertThat(Validation.validateLicenceLength("8-days")).isEmpty();
    assertThat(Validation.validateLicenceLength("12-months")).isEmpty();
  }

  @Test
  void validatesCookieAndHelpers() {
    assertThat(Validation.validateCookieChoice("maybe")).isNotEmpty();
    assertThat(Validation.validateCookieChoice("yes")).isEmpty();
    assertThat(Validation.asLicenceLength("1-day")).isEqualTo("1-day");
    assertThat(Validation.asLicenceLength("8-days")).isEqualTo("8-days");
    assertThat(Validation.asLicenceLength("12-months")).isEqualTo("12-months");
    assertThat(Validation.asLicenceLength("8-day")).isEmpty();
    assertThat(Validation.clean(null)).isEmpty();
  }
}
