package uk.gov.example.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.Render;
import uk.gov.example.service.Answers;
import uk.gov.example.service.Forms;
import uk.gov.example.service.LicenceApplication;
import uk.gov.example.service.Save;
import uk.gov.example.service.Validation;
import uk.gov.example.session.SessionData;

@Controller
public class JourneyController {

  private final PageChrome chrome;
  private final Clock clock;

  @org.springframework.beans.factory.annotation.Autowired
  public JourneyController(PageChrome chrome) {
    this(chrome, Clock.systemUTC());
  }

  JourneyController(PageChrome chrome, Clock clock) {
    this.chrome = chrome;
    this.clock = clock;
  }

  @GetMapping({
    "/licence-length",
    "/name",
    "/date-of-birth",
    "/where-you-will-fish",
    "/email"
  })
  public String stepGet(
      HttpServletRequest request,
      Model model,
      @RequestParam(value = "return", required = false) String returnParam) {
    SessionData session = WebSessions.require(request);
    LicenceApplication.Step step =
        LicenceApplication.stepByPath(request.getRequestURI()).orElseThrow();
    List<Validation.FieldError> errors = session.takeFlashErrors(step.path());
    return renderStep(request, model, session, step, errors, returnParam);
  }

  @PostMapping({
    "/licence-length",
    "/name",
    "/date-of-birth",
    "/where-you-will-fish",
    "/email"
  })
  public String stepPost(
      HttpServletRequest request, @RequestParam Map<String, String> form) {
    SessionData session = WebSessions.require(request);
    if (!WebSessions.csrfOk(session, form.get("csrf"))) {
      return "redirect:/session-expired";
    }
    LicenceApplication.Step step =
        LicenceApplication.stepByPath(request.getRequestURI()).orElseThrow();
    List<Validation.FieldError> errors = validate(step, form);
    apply(step, form, session.application(), errors.isEmpty());
    if (!errors.isEmpty()) {
      session.setFlashErrors(step.path(), errors);
      String redirect = step.path();
      if ("check-answers".equals(form.get("returnTo"))) {
        redirect = step.path() + "?return=check-answers";
      }
      return "redirect:" + redirect;
    }
    session.clearFlashErrors();
    if ("check-answers".equals(form.get("returnTo"))) {
      return "redirect:/check-answers";
    }
    return LicenceApplication.nextStep(step.id())
        .map(next -> "redirect:" + next.path())
        .orElse("redirect:/check-answers");
  }

  @GetMapping("/check-answers")
  public String checkAnswersGet(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    if (session.application().submitted()) {
      return "redirect:/confirmation";
    }
    Optional<LicenceApplication.Step> incomplete =
        LicenceApplication.firstIncompleteStep(session.application());
    if (incomplete.isPresent()) {
      return "redirect:" + incomplete.get().path();
    }
    chrome.apply(
        model, request, session, "Check your answers", false, "en", false, "govuk-main-wrapper--l");
    model.addAttribute(
        "backLinkHtml",
        chrome.html(
            Render.mustRender("back-link", Params.of("text", "Back", "href", "/email"))));
    model.addAttribute(
        "summaryHtml",
        chrome.html(
            Render.mustRender(
                "summary-list", Params.of("rows", Answers.summaryRows(session.application())))));
    model.addAttribute(
        "submitButtonHtml",
        chrome.html(Render.mustRender("button", Params.of("text", "Accept and continue"))));
    return "pages/check-answers";
  }

  @PostMapping("/check-answers")
  public String checkAnswersPost(
      HttpServletRequest request, @RequestParam(value = "csrf", required = false) String csrf) {
    SessionData session = WebSessions.require(request);
    if (!WebSessions.csrfOk(session, csrf)) {
      return "redirect:/session-expired";
    }
    if (session.application().submitted()) {
      return "redirect:/confirmation";
    }
    Optional<LicenceApplication.Step> incomplete =
        LicenceApplication.firstIncompleteStep(session.application());
    if (incomplete.isPresent()) {
      return "redirect:" + incomplete.get().path();
    }
    session.application().setSubmitted(true);
    session.application().setReference(SessionData.referenceFor(WebSessions.id(request)));
    return "redirect:/confirmation";
  }

  @GetMapping("/confirmation")
  public String confirmation(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    if (!session.application().submitted()) {
      return "redirect:/";
    }
    chrome.apply(model, request, session, "Application complete", false, "en", true, "");
    model.addAttribute(
        "panelHtml",
        chrome.html(
            Render.mustRender(
                "panel", Forms.confirmationPanel(session.application().reference()))));
    return "pages/confirmation";
  }

  @GetMapping("/session-expired")
  public String sessionExpired(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    chrome.apply(
        model, request, session, "Sorry, your session has expired", false, "en", false, "");
    model.addAttribute(
        "startAgainHtml",
        chrome.html(
            Render.mustRender(
                "button", Params.of("text", "Start again", "href", "/new-application"))));
    return "pages/session-expired";
  }

  private String renderStep(
      HttpServletRequest request,
      Model model,
      SessionData session,
      LicenceApplication.Step step,
      List<Validation.FieldError> errors,
      String returnParam) {
    String returnTo = "check-answers".equals(returnParam) ? "check-answers" : "";
    String back = "/";
    if (!returnTo.isEmpty()) {
      back = "/check-answers";
    } else {
      Optional<LicenceApplication.Step> previous = LicenceApplication.previousStep(step.id());
      if (previous.isPresent()) {
        back = previous.get().path();
      }
    }
    chrome.apply(
        model,
        request,
        session,
        step.heading(),
        !errors.isEmpty(),
        "en",
        false,
        "govuk-main-wrapper--l");
    model.addAttribute(
        "backLinkHtml",
        chrome.html(Render.mustRender("back-link", Params.of("text", "Back", "href", back))));
    Params summary = Forms.errorSummary(errors);
    model.addAttribute(
        "errorSummaryHtml",
        summary == null ? null : chrome.html(Render.mustRender("error-summary", summary)));
    model.addAttribute("returnTo", returnTo);
    model.addAttribute("csrf", session.csrfToken());
    model.addAttribute("formAction", step.path());
    model.addAttribute("enctype", null);
    model.addAttribute(
        "continueButtonHtml",
        chrome.html(Render.mustRender("button", Params.of("text", "Continue"))));
    addStepFields(model, session, step, errors);
    return "pages/question";
  }

  private void addStepFields(
      Model model,
      SessionData session,
      LicenceApplication.Step step,
      List<Validation.FieldError> errors) {
    LicenceApplication app = session.application();
    model.addAttribute("stepId", step.id());
    model.addAttribute("pageHeading", false);
    switch (step.id()) {
      case "licence-length" ->
          model.addAttribute(
              "fieldHtml",
              List.of(chrome.html(Render.mustRender("radios", Forms.licenceFields(app, errors)))));
      case "name" ->
          model.addAttribute(
              "fieldHtml",
              List.of(chrome.html(Render.mustRender("input", Forms.nameField(app, errors)))));
      case "date-of-birth" ->
          model.addAttribute(
              "fieldHtml",
              List.of(chrome.html(Render.mustRender("date-input", Forms.dateField(app, errors)))));
      case "where-you-will-fish" ->
          model.addAttribute(
              "fieldHtml",
              List.of(chrome.html(Render.mustRender("radios", Forms.countryFields(app, errors)))));
      default ->
          model.addAttribute(
              "fieldHtml",
              List.of(chrome.html(Render.mustRender("input", Forms.emailField(app, errors)))));
    }
  }

  private List<Validation.FieldError> validate(
      LicenceApplication.Step step, Map<String, String> form) {
    return switch (step.id()) {
      case "licence-length" -> Validation.validateLicenceLength(form.get("licence-length"));
      case "name" -> Validation.validateName(form.get("full-name"));
      case "date-of-birth" ->
          Validation.validateDateOfBirth(
              form.get("date-of-birth-day"),
              form.get("date-of-birth-month"),
              form.get("date-of-birth-year"),
              today());
      case "where-you-will-fish" -> Validation.validateCountry(form.get("country"));
      default -> Validation.validateEmail(form.get("email"));
    };
  }

  private void apply(
      LicenceApplication.Step step,
      Map<String, String> form,
      LicenceApplication application,
      boolean valid) {
    switch (step.id()) {
      case "licence-length" -> Save.saveLicence(application, form.get("licence-length"), valid);
      case "name" -> Save.saveName(application, form.get("full-name"), valid);
      case "date-of-birth" ->
          Save.saveDate(
              application,
              form.get("date-of-birth-day"),
              form.get("date-of-birth-month"),
              form.get("date-of-birth-year"),
              valid);
      case "where-you-will-fish" -> Save.saveCountry(application, form.get("country"), valid);
      default -> Save.saveEmail(application, form.get("email"), valid);
    }
  }

  private LocalDate today() {
    return LocalDate.now(clock);
  }
}
