package uk.gov.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.Render;

class FormsAndAnswersTest {

  @Test
  void buildsAndRendersAllQuestionFields() {
    LicenceApplication app = new LicenceApplication();
    app.setFirstName("Jane");
    app.setLastName("Doe");
    app.setDay("1");
    app.setMonth("1");
    app.setYear("2000");
    app.setEmail("a@b.co");
    app.setContactBy("telephone");
    app.setTelephone("01632 960 001");
    app.setRegions(List.of("north-west"));
    app.setLicenceLength("1-day");
    app.setStartMonth("2026-03");
    app.setAddressLine1("1 High St");
    app.setTown("London");
    app.setPostcode("SW1A 1AA");
    app.setEvidenceFilename("proof.pdf");
    app.setAdditionalDetails("notes");
    app.setPasswordCreated(true);
    List<Validation.FieldError> errors =
        List.of(new Validation.FieldError("email", "#email", "bad"));

    assertThat(Forms.errorSummary(List.of())).isNull();
    assertThat(Render.mustRender("error-summary", Forms.errorSummary(errors))).contains("problem");

    Forms.nameFields(app, errors)
        .values()
        .forEach(p -> assertThat(Render.mustRender("input", p)).contains("govuk-input"));
    assertThat(Render.mustRender("input", Forms.emailField(app, errors))).contains("email");
    assertThat(Render.mustRender("date-input", Forms.dateField(app, errors))).contains("date");
    assertThat(Render.mustRender("radios", Forms.contactFields(app, errors))).contains("Telephone");
    assertThat(Render.mustRender("checkboxes", Forms.regionFields(app, errors)))
        .contains("North West");
    assertThat(Render.mustRender("radios", Forms.licenceFields(app, errors))).contains("1 day");
    assertThat(
            Render.mustRender(
                "select", Forms.monthField(app, errors, LocalDate.of(2026, 3, 1))))
        .contains("start-month");
    Forms.addressFields(app, errors)
        .values()
        .forEach(p -> assertThat(p).isInstanceOf(Params.class));
    assertThat(Render.mustRender("file-upload", Forms.evidenceField(app, errors)))
        .contains("evidence");
    assertThat(Render.mustRender("character-count", Forms.detailsField(app, errors)))
        .contains("additional-details");
    Forms.passwordFields(errors)
        .values()
        .forEach(p -> assertThat(Render.mustRender("password-input", p)).contains("password"));
    assertThat(Render.mustRender("radios", Forms.cookieFields("yes", errors))).contains("analytics");
    assertThat(Render.mustRender("table", Forms.feesTable())).contains("Licence");
    assertThat(Render.mustRender("accordion", Forms.helpAccordion())).contains("Who can apply");
    assertThat(Render.mustRender("tabs", Forms.guidanceTabs())).contains("Before you apply");
    assertThat(Render.mustRender("panel", Forms.confirmationPanel("RLABC123")))
        .contains("RLABC123");

    assertThat(Answers.summaryRows(app, LocalDate.of(2026, 3, 1))).isNotEmpty();
    assertThat(Answers.taskSections(app)).hasSize(4);
    app.markCompleted("name");
    assertThat(Render.mustRender("task-list", Params.of("items", Answers.taskSections(app).get(0).items())))
        .contains("Completed");
  }

  @Test
  void regionNotSureAndCookieNoChoice() {
    LicenceApplication app = new LicenceApplication();
    app.setRegions(List.of(Options.NOT_SURE));
    assertThat(
            Render.mustRender(
                "summary-list", Params.of("rows", Answers.summaryRows(app, LocalDate.of(2026, 1, 1)))))
        .contains("Not decided");
    assertThat(Render.mustRender("radios", Forms.cookieFields("no", List.of()))).contains("No");
    assertThat(Render.mustRender("radios", Forms.cookieFields(null, List.of()))).contains("Yes");
  }
}
