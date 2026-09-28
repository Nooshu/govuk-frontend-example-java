package uk.gov.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class OptionsAndSaveTest {

  @Test
  void optionsAndLabels() {
    assertThat(Options.regions()).hasSize(6);
    assertThat(Options.licenceLengths()).hasSize(3);
    assertThat(Options.contactOptions()).hasSize(2);
    assertThat(Options.licenceLengthOptions()).extracting(Options.Option::value).contains("1-day");
    assertThat(Options.startMonths(LocalDate.of(2026, 3, 15))).hasSize(12);
    assertThat(Options.startMonths(LocalDate.of(2026, 3, 15)).get(0).value()).isEqualTo("2026-03");
    assertThat(Options.labelFor(Options.regions(), "wales")).isEqualTo("Wales");
    assertThat(Options.labelFor(Options.regions(), "unknown")).isEqualTo("unknown");
    assertThat(Options.NOT_SURE).isEqualTo("not-sure");
  }

  @Test
  void saveMarksCompletion() {
    LicenceApplication app = new LicenceApplication();
    Save.saveName(app, " Jane ", " Doe ", true);
    assertThat(app.firstName()).isEqualTo("Jane");
    assertThat(app.isCompleted("name")).isTrue();
    Save.saveName(app, "", "Doe", false);
    assertThat(app.isCompleted("name")).isFalse();
    Save.saveName(app, "Jane", "Doe", true);
    Save.saveDate(app, "1", "1", "2000", true);
    Save.saveEmail(app, "a@b.co", true);
    Save.saveContact(app, "email", "", true);
    Save.saveRegions(app, java.util.List.of("north-west", "bogus"), true);
    assertThat(app.regions()).containsExactly("north-west");
    Save.saveLicence(app, "1-day", true);
    Save.saveMonth(app, "2026-03", true);
    Save.saveAddress(app, new Save.Address("1 High St", "", "London", "sw1a1aa"), true);
    assertThat(app.postcode()).isEqualTo("SW1A 1AA");
    Save.saveEvidence(app, "proof.pdf", true, true);
    Save.saveDetails(app, "hello", true);
    Save.savePassword(app, true);
    assertThat(app.passwordCreated()).isTrue();
    assertThat(app.requiredComplete()).isTrue();
    assertThat(LicenceApplication.firstIncompleteStep(app)).isEmpty();
    app.reset();
    assertThat(app.firstName()).isEmpty();
    assertThat(app.requiredComplete()).isFalse();
  }
}
