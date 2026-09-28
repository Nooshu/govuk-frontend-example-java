package uk.gov.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.Render;

class ServiceCoverageTest {

  @Test
  void saveNullAndInvalidBranches() {
    LicenceApplication app = new LicenceApplication();
    Save.saveRegions(app, null, false);
    assertThat(app.regions()).isEmpty();
    Save.saveMonth(app, null, false);
    assertThat(app.startMonth()).isEmpty();
    Save.saveAddress(app, new Save.Address("1", "2", "Town", "sw1a1aa"), false);
    assertThat(app.postcode()).isEqualTo("sw1a1aa");
    Save.saveEvidence(app, "proof.pdf", false, true);
    assertThat(app.evidenceFilename()).isEmpty();
    Save.saveEvidence(app, "proof.pdf", true, false);
    assertThat(app.evidenceFilename()).isEmpty();
    Save.saveDetails(app, null, true);
    assertThat(app.additionalDetails()).isEmpty();
    assertThat(Options.labelFor(Options.regions(), null)).isEmpty();
  }

  @Test
  void answersSubmittedAndBlankRows() {
    LicenceApplication app = new LicenceApplication();
    for (LicenceApplication.Step step : LicenceApplication.steps()) {
      if (!LicenceApplication.optional(step.id())) {
        app.markCompleted(step.id());
      }
    }
    assertThat(Answers.taskSections(app).get(3).items().get(0).get("href")).isNotNull();
    app.setSubmitted(true);
    assertThat(Answers.taskSections(app).get(3).items().get(0).get("status")).isNotNull();

    app.setDay("0");
    app.setMonth("1");
    app.setYear("2000");
    assertThat(Answers.summaryRows(app, LocalDate.of(2026, 1, 1))).isNotEmpty();
    app.setDay("x");
    assertThat(Answers.summaryRows(app, LocalDate.of(2026, 1, 1))).isNotEmpty();
    app.setFirstName("");
    app.setLastName("");
    app.setEmail("");
    app.setAddressLine2("Flat 1");
    List<Params> rows = Answers.summaryRows(app, LocalDate.of(2026, 1, 1));
    assertThat(Render.mustRender("summary-list", Params.of("rows", rows)))
        .contains("Not provided")
        .contains("Flat 1");
  }

  @Test
  void licenceApplicationGraphAndNullSetters() {
    assertThat(LicenceApplication.steps()).isNotEmpty();
    assertThat(LicenceApplication.optional("evidence")).isTrue();
    assertThat(LicenceApplication.optional("name")).isFalse();
    assertThat(LicenceApplication.stepById("name")).isPresent();
    assertThat(LicenceApplication.stepById("nope")).isEmpty();
    assertThat(LicenceApplication.stepByPath("/name")).isPresent();
    assertThat(LicenceApplication.nextStep("create-a-password")).isEmpty();
    assertThat(LicenceApplication.nextStep("nope")).isEmpty();
    assertThat(LicenceApplication.previousStep("name")).isEmpty();
    assertThat(LicenceApplication.previousStep("nope")).isEmpty();
    assertThat(LicenceApplication.previousStep("email")).isPresent();

    LicenceApplication app = new LicenceApplication();
    app.setRegions(null);
    assertThat(app.regions()).isEmpty();
    app.setLicenceLength(null);
    app.setStartMonth(null);
    app.setAddressLine1(null);
    app.setAddressLine2(null);
    app.setTown(null);
    app.setPostcode(null);
    app.setEvidenceFilename(null);
    app.setAdditionalDetails(null);
    app.setReference(null);
    assertThat(app.licenceLength()).isEmpty();
    assertThat(app.reference()).isEmpty();
    app.markCompleted("name");
    assertThat(app.completed()).contains("name");
    app.unmarkCompleted("name");
    assertThat(app.isCompleted("name")).isFalse();
  }

  @Test
  void formsNullErrorList() {
    assertThat(Forms.errorSummary(null)).isNull();
    LicenceApplication app = new LicenceApplication();
    assertThat(Forms.emailField(app, null).get("id")).isEqualTo("email");
  }
}
