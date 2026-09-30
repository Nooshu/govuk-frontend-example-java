package uk.gov.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ValidationCoverageTest {

  @Test
  void dateOfBirthFormatAndAgeEdges() {
    LocalDate now = LocalDate.of(2026, 9, 28);
    assertThat(Validation.validateDateOfBirth("1", "", "2000", now)).isNotEmpty();
    assertThat(Validation.validateDateOfBirth("1", "1", "", now)).isNotEmpty();
    assertThat(Validation.validateDateOfBirth("abc", "1", "2000", now)).isNotEmpty();
    assertThat(Validation.validateDateOfBirth("1", "13", "2000", now)).isNotEmpty();
    assertThat(Validation.validateDateOfBirth("1", "1", "20", now)).isNotEmpty();
    // Birthday not yet reached this year → under 13
    assertThat(Validation.validateDateOfBirth("29", "9", "2013", now)).isNotEmpty();
    // Exactly 13 today
    assertThat(Validation.validateDateOfBirth("28", "9", "2013", now)).isEmpty();
    // Same month, day already passed
    assertThat(Validation.validateDateOfBirth("1", "9", "2013", now)).isEmpty();
  }

  @Test
  void cookieAndCountryEdges() {
    assertThat(Validation.validateCountry("Scotland")).isEmpty();
    assertThat(Validation.validateCountry("Wales")).isEmpty();
    assertThat(Validation.validateCookieChoice("no")).isEmpty();
    assertThat(Validation.asLicenceLength("nope")).isEmpty();
  }
}
