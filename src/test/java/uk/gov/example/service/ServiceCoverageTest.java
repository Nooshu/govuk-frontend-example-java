package uk.gov.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.Render;

class ServiceCoverageTest {

  @Test
  void answersBlankRows() {
    LicenceApplication app = new LicenceApplication();
    List<Params> rows = Answers.summaryRows(app);
    assertThat(Render.mustRender("summary-list", Params.of("rows", rows))).contains("Not provided");
    app.setDay("1");
    app.setMonth("2");
    app.setYear("2000");
    app.setFullName("Jane");
    assertThat(Render.mustRender("summary-list", Params.of("rows", Answers.summaryRows(app))))
        .contains("1 2 2000")
        .contains("Jane");
  }

  @Test
  void licenceApplicationGraphAndNullSetters() {
    assertThat(LicenceApplication.steps()).hasSize(5);
    assertThat(LicenceApplication.stepById("licence-length")).isPresent();
    assertThat(LicenceApplication.stepById("name")).isPresent();
    assertThat(LicenceApplication.stepById("nope")).isEmpty();
    assertThat(LicenceApplication.stepByPath("/licence-length")).isPresent();
    assertThat(LicenceApplication.nextStep("email")).isEmpty();
    assertThat(LicenceApplication.nextStep("nope")).isEmpty();
    assertThat(LicenceApplication.previousStep("licence-length")).isEmpty();
    assertThat(LicenceApplication.previousStep("nope")).isEmpty();
    assertThat(LicenceApplication.previousStep("name")).isPresent();

    LicenceApplication app = new LicenceApplication();
    app.setLicenceLength(null);
    app.setFullName(null);
    app.setCountry(null);
    app.setEmail(null);
    app.setReference(null);
    assertThat(app.licenceLength()).isEmpty();
    assertThat(app.fullName()).isEmpty();
    assertThat(app.reference()).isEmpty();
    app.markCompleted("name");
    assertThat(app.completed()).contains("name");
    app.unmarkCompleted("name");
    assertThat(app.isCompleted("name")).isFalse();
    assertThat(LicenceApplication.firstIncompleteStep(app)).isPresent();
  }

  @Test
  void formsNullErrorList() {
    assertThat(Forms.errorSummary(null)).isNull();
    LicenceApplication app = new LicenceApplication();
    assertThat(Forms.emailField(app, null).get("id")).isEqualTo("email");
    assertThat(Forms.nameField(app, null).get("id")).isEqualTo("full-name");
  }
}
