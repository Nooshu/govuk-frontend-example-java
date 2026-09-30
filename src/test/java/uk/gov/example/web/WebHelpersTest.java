package uk.gov.example.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.example.govuk.Params;
import uk.gov.example.service.Validation;
import uk.gov.example.session.SessionData;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.demos-enabled=true")
class WebHelpersTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private AssetRegistry assets;
  @Autowired private GovukViewHelper govuk;

  @Test
  void assetsAndHelpers() throws Exception {
    mockMvc.perform(get(assets.stylesheetHref())).andExpect(status().isOk());
    mockMvc.perform(get(assets.appModuleHref())).andExpect(status().isOk());
    mockMvc.perform(get("/assets/images/favicon.ico")).andExpect(status().isOk());
    mockMvc.perform(get("/assets/missing-file.nope")).andExpect(status().isNotFound());

    assertThat(govuk.button("Continue").value()).contains("govuk-button");
    assertThat(govuk.buttonLink("Continue", "/").value()).contains("href");
    assertThat(govuk.startButton("Start", "/licence-length").value()).contains("Start");
    assertThat(govuk.render("tag", Params.of("text", "Done")).value()).contains("Done");
    assertThat(govuk.render("tag", Map.of("text", "Mapped")).value()).contains("Mapped");
    assertThat(GovukViewHelper.map("a", 1, "b", 2)).containsEntry("a", 1);

    assertThat(ComponentCatalogue.describe("button").title()).isEqualTo("Button");
    assertThat(ComponentCatalogue.describe("brand-new-thing").title()).contains("Brand");

    SessionData session = new SessionData();
    session.setFlashErrors("/name", List.of(new Validation.FieldError("x", "#x", "y")));
    assertThat(session.takeFlashErrors("/other")).isEmpty();
    session.setFlashErrors("/name", List.of(new Validation.FieldError("x", "#x", "y")));
    assertThat(session.takeFlashErrors("/name")).hasSize(1);
    session.setNotice("/cookies", "ok");
    assertThat(session.takeNotice("/other")).isNull();
    session.setNotice("/cookies", "ok");
    assertThat(session.takeNotice("/cookies")).isEqualTo("ok");
    session.setCookieBannerConfirm("accept");
    assertThat(session.cookieBannerConfirm()).isEqualTo("accept");
    assertThat(SessionData.referenceFor("abcdef123")).matches("FR\\d{8}");
    assertThat(SessionData.referenceFor("ab")).matches("FR\\d{8}");
    assertThat(SessionData.referenceFor("")).isEqualTo("FR00000000");
    assertThat(WebSessions.safeReturn("/fees")).isEqualTo("/fees");
    assertThat(WebSessions.safeReturn("//evil")).isEqualTo("/");
    assertThat(WebSessions.csrfOk(session, session.csrfToken())).isTrue();
    assertThat(WebSessions.csrfOk(session, "nope")).isFalse();
  }
}
