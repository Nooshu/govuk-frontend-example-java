package uk.gov.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.example.govuk.Render;

class FormsAndAnswersTest {

  @Test
  void buildsAndRendersAllQuestionFields() {
    LicenceApplication app = new LicenceApplication();
    app.setFullName("Jane Doe");
    app.setDay("1");
    app.setMonth("1");
    app.setYear("2000");
    app.setEmail("a@b.co");
    app.setCountry("England");
    app.setLicenceLength("1-day");
    List<Validation.FieldError> errors =
        List.of(new Validation.FieldError("email", "#email", "bad"));

    assertThat(Forms.errorSummary(List.of())).isNull();
    assertThat(Render.mustRender("error-summary", Forms.errorSummary(errors))).contains("problem");

    assertThat(Render.mustRender("input", Forms.nameField(app, errors))).contains("full-name");
    assertThat(Render.mustRender("input", Forms.emailField(app, errors)))
        .contains("email")
        .contains("browser session only");
    assertThat(Render.mustRender("date-input", Forms.dateField(app, errors)))
        .contains("date")
        .contains("31 3 1980");
    assertThat(Render.mustRender("radios", Forms.countryFields(app, errors)))
        .contains("England")
        .contains("fictional");
    assertThat(Render.mustRender("radios", Forms.licenceFields(app, errors)))
        .contains("1 day")
        .doesNotContain("£");
    assertThat(Render.mustRender("radios", Forms.cookieFields("yes", errors))).contains("analytics");
    assertThat(Render.mustRender("table", Forms.feesTable())).contains("Licence").contains("£7.10");
    assertThat(Render.mustRender("accordion", Forms.helpAccordion())).contains("Who can apply");
    assertThat(Render.mustRender("tabs", Forms.guidanceTabs())).contains("Before you apply");
    assertThat(Render.mustRender("panel", Forms.confirmationPanel("FRABC123")))
        .contains("FRABC123")
        .contains("Your example reference number");

    assertThat(Answers.summaryRows(app)).hasSize(5);
    assertThat(Render.mustRender("summary-list", uk.gov.example.govuk.Params.of("rows", Answers.summaryRows(app))))
        .contains("1 1 2000")
        .contains("Jane Doe")
        .contains("England");
  }

  @Test
  void blankSummaryAndCookieChoices() {
    LicenceApplication app = new LicenceApplication();
    assertThat(
            Render.mustRender(
                "summary-list",
                uk.gov.example.govuk.Params.of("rows", Answers.summaryRows(app))))
        .contains("Not provided");
    assertThat(Render.mustRender("radios", Forms.cookieFields("no", List.of()))).contains("No");
    assertThat(Render.mustRender("radios", Forms.cookieFields(null, List.of()))).contains("Yes");
  }
}
