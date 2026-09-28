package uk.gov.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationTest {

  @Test
  void validatesName() {
    assertThat(Validation.validateName("", "Smith")).isNotEmpty();
    assertThat(Validation.validateName("Jane", "")).isNotEmpty();
    assertThat(Validation.validateName("Jane", "Smith")).isEmpty();
    assertThat(Validation.validateName("a".repeat(101), "Smith")).isNotEmpty();
  }

  @Test
  void validatesDateOfBirth() {
    LocalDate now = LocalDate.of(2026, 9, 28);
    assertThat(Validation.validateDateOfBirth("", "1", "2000", now)).isNotEmpty();
    assertThat(Validation.validateDateOfBirth("31", "2", "2000", now)).isNotEmpty();
    assertThat(Validation.validateDateOfBirth("1", "1", "2099", now)).isNotEmpty();
    assertThat(Validation.validateDateOfBirth("1", "1", "2020", now)).isNotEmpty();
    assertThat(Validation.validateDateOfBirth("1", "1", "2000", now)).isEmpty();
  }

  @Test
  void validatesEmailContactLicenceRegionsAddress() {
    assertThat(Validation.validateEmail("bad")).isNotEmpty();
    assertThat(Validation.validateEmail("a@b.co")).isEmpty();
    assertThat(Validation.validateContactPreference("fax", "")).isNotEmpty();
    assertThat(Validation.validateContactPreference("telephone", "")).isNotEmpty();
    assertThat(Validation.validateContactPreference("telephone", "01632 960 001")).isEmpty();
    assertThat(Validation.validateRegions(List.of())).isNotEmpty();
    assertThat(Validation.validateRegions(List.of("north-west", "not-sure"))).isNotEmpty();
    assertThat(Validation.validateRegions(List.of("north-west"))).isEmpty();
    assertThat(Validation.validateLicenceLength("nope")).isNotEmpty();
    assertThat(Validation.validateLicenceLength("1-day")).isEmpty();
    assertThat(Validation.validateStartMonth("nope", LocalDate.of(2026, 1, 1))).isNotEmpty();
    assertThat(Validation.validateAddress("", "Town", "SW1A 1AA")).isNotEmpty();
    assertThat(Validation.validateAddress("1 Street", "Town", "SW1A 1AA")).isEmpty();
    assertThat(Validation.normalisePostcode("sw1a1aa")).isEqualTo("SW1A 1AA");
  }

  @Test
  void validatesOptionalAndPassword() {
    assertThat(Validation.validateEvidence("")).isEmpty();
    assertThat(Validation.validateEvidence("x.exe")).isNotEmpty();
    assertThat(Validation.validateEvidence("x.pdf")).isEmpty();
    assertThat(Validation.validateAdditionalDetails("a".repeat(201))).isNotEmpty();
    assertThat(Validation.validatePassword("short", "short")).isNotEmpty();
    assertThat(Validation.validatePassword("longenough", "different")).isNotEmpty();
    assertThat(Validation.validatePassword("longenough", "longenough")).isEmpty();
    assertThat(Validation.validateCookieChoice("maybe")).isNotEmpty();
    assertThat(Validation.validateCookieChoice("yes")).isEmpty();
    assertThat(Validation.safeFilename("../etc/passwd")).isEqualTo("passwd");
    assertThat(Validation.safeFilename("..")).isEmpty();
    assertThat(Validation.safeFilename("proof.pdf")).isEqualTo("proof.pdf");
    assertThat(Validation.asContactBy("email")).isEqualTo("email");
    assertThat(Validation.asLicenceLength("1-day")).isEqualTo("1-day");
  }
}
