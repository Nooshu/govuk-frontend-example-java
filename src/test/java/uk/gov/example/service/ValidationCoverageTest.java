package uk.gov.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
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
  void contactRegionsAddressAndCookieEdges() {
    assertThat(Validation.validateContactPreference("email", "")).isEmpty();
    assertThat(Validation.validateContactPreference("telephone", "bad")).isNotEmpty();
    assertThat(Validation.validateContactPreference("email", "not-a-phone")).isNotEmpty();
    assertThat(Validation.validateRegions(null)).isNotEmpty();
    assertThat(Validation.validateRegions(List.of("not-sure"))).isEmpty();
    assertThat(Validation.validateRegions(List.of("bogus-region"))).isNotEmpty();
    assertThat(Validation.validateAddress("a".repeat(101), "Town", "SW1A 1AA")).isNotEmpty();
    assertThat(Validation.validateAddress("1 Street", "", "SW1A 1AA")).isNotEmpty();
    assertThat(Validation.validateAddress("1 Street", "Town", "XX")).isNotEmpty();
    assertThat(Validation.validateCookieChoice("no")).isEmpty();
    assertThat(Validation.validateEvidence(null)).isEmpty();
    assertThat(Validation.validateAdditionalDetails(null)).isEmpty();
    assertThat(Validation.validateAdditionalDetails("ok")).isEmpty();
    assertThat(Validation.validatePassword(null, null)).isNotEmpty();
  }

  @Test
  void helpersAndSafeFilename() {
    assertThat(Validation.clean(null)).isEmpty();
    assertThat(Validation.normalisePostcode("ab1")).isEmpty();
    assertThat(Validation.asContactBy("fax")).isEmpty();
    assertThat(Validation.asContactBy("telephone")).isEqualTo("telephone");
    assertThat(Validation.asLicenceLength("nope")).isEmpty();
    assertThat(Validation.asLicenceLength("8-day")).isEqualTo("8-day");
    assertThat(Validation.asLicenceLength("12-month")).isEqualTo("12-month");
    assertThat(Validation.safeFilename(null)).isEmpty();
    assertThat(Validation.safeFilename("")).isEmpty();
    assertThat(Validation.safeFilename(".")).isEmpty();
    assertThat(Validation.safeFilename("C:\\uploads\\proof.pdf")).isEqualTo("proof.pdf");
    assertThat(Validation.safeFilename("a".repeat(121) + ".pdf")).isEmpty();
    assertThat(Validation.safeFilename("bad*.pdf")).isEmpty();
  }
}
