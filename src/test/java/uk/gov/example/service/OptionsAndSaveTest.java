package uk.gov.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OptionsAndSaveTest {

  @Test
  void optionsAndLabels() {
    assertThat(Options.countries()).hasSize(3);
    assertThat(Options.licenceLengths()).hasSize(3);
    assertThat(Options.licenceFees()).hasSize(3);
    assertThat(Options.licenceLengths()).extracting(Options.Option::value).contains("8-days", "12-months");
    assertThat(Options.labelFor(Options.countries(), "Wales")).isEqualTo("Wales");
    assertThat(Options.labelFor(Options.countries(), "unknown")).isEqualTo("unknown");
    assertThat(Options.labelFor(Options.countries(), null)).isEmpty();
  }

  @Test
  void saveMarksCompletion() {
    LicenceApplication app = new LicenceApplication();
    Save.saveLicence(app, "1-day", true);
    assertThat(app.licenceLength()).isEqualTo("1-day");
    assertThat(app.isCompleted("licence-length")).isTrue();
    Save.saveLicence(app, "nope", false);
    assertThat(app.isCompleted("licence-length")).isFalse();
    Save.saveLicence(app, "1-day", true);
    Save.saveName(app, " Jane Doe ", true);
    assertThat(app.fullName()).isEqualTo("Jane Doe");
    Save.saveName(app, "A", false);
    assertThat(app.isCompleted("name")).isFalse();
    Save.saveName(app, "Jane Doe", true);
    Save.saveDate(app, "1", "1", "2000", true);
    Save.saveCountry(app, "England", true);
    Save.saveEmail(app, "a@b.co", true);
    assertThat(app.requiredComplete()).isTrue();
    assertThat(LicenceApplication.firstIncompleteStep(app)).isEmpty();
    app.reset();
    assertThat(app.fullName()).isEmpty();
    assertThat(app.requiredComplete()).isFalse();
  }
}
