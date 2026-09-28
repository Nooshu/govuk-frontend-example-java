package uk.gov.example.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.Render;
import uk.gov.example.govuk.TrustedHtml;
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

  @GetMapping("/task-list")
  public String taskList(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    chrome.apply(model, request, session, "Your application", false, "en", false, "");
    model.addAttribute(
        "backLinkHtml",
        chrome.html(Render.mustRender("back-link", Params.of("text", "Back", "href", "/"))));
    List<TrustedHtml> sections = new ArrayList<>();
    for (Answers.TaskSection section : Answers.taskSections(session.application())) {
      String heading = "<h2 class=\"govuk-heading-m\">" + section.heading() + "</h2>";
      String list =
          Render.mustRender(
              "task-list", Params.of("idPrefix", section.idPrefix(), "items", section.items()));
      sections.add(new TrustedHtml(heading + list));
    }
    model.addAttribute("sectionsHtml", sections);
    return "pages/task-list";
  }

  @GetMapping({
    "/name",
    "/date-of-birth",
    "/email",
    "/contact-preference",
    "/where-you-will-fish",
    "/licence-length",
    "/start-month",
    "/address",
    "/evidence",
    "/additional-details",
    "/create-a-password"
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
    "/name",
    "/date-of-birth",
    "/email",
    "/contact-preference",
    "/where-you-will-fish",
    "/licence-length",
    "/start-month",
    "/address",
    "/additional-details",
    "/create-a-password"
  })
  public String stepPost(
      HttpServletRequest request,
      @RequestParam Map<String, String> form,
      @RequestParam(value = "regions", required = false) List<String> regions) {
    return handleStepPost(request, form, regions, null);
  }

  @PostMapping(value = "/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String evidencePost(
      HttpServletRequest request,
      @RequestParam Map<String, String> form,
      @RequestParam(value = "evidence", required = false) MultipartFile evidence) {
    return handleStepPost(request, form, null, evidence);
  }

  @PostMapping(value = "/evidence")
  public String evidencePostWithoutFile(
      HttpServletRequest request, @RequestParam Map<String, String> form) {
    return handleStepPost(request, form, null, null);
  }

  private String handleStepPost(
      HttpServletRequest request,
      Map<String, String> form,
      List<String> regions,
      MultipartFile evidence) {
    SessionData session = WebSessions.require(request);
    if (!WebSessions.csrfOk(session, form.get("csrf"))) {
      return "redirect:/session-expired";
    }
    LicenceApplication.Step step =
        LicenceApplication.stepByPath(request.getRequestURI()).orElseThrow();
    List<Validation.FieldError> errors = validate(step, form, regions, evidence);
    apply(step, form, regions, evidence, session.application(), errors.isEmpty());
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
            Render.mustRender(
                "back-link", Params.of("text", "Back", "href", "/create-a-password"))));
    model.addAttribute(
        "summaryHtml",
        chrome.html(
            Render.mustRender(
                "summary-list",
                Params.of("rows", Answers.summaryRows(session.application(), today())))));
    model.addAttribute(
        "submitButtonHtml",
        chrome.html(Render.mustRender("button", Params.of("text", "Submit application"))));
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
      return "redirect:/task-list";
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
    String back = "/task-list";
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
    model.addAttribute("enctype", "evidence".equals(step.id()) ? "multipart/form-data" : null);
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
    switch (step.id()) {
      case "name" -> {
        Map<String, Params> fields = Forms.nameFields(app, errors);
        model.addAttribute("fieldHtml", List.of(
            chrome.html(Render.mustRender("input", fields.get("firstName"))),
            chrome.html(Render.mustRender("input", fields.get("lastName")))));
        model.addAttribute("pageHeading", true);
      }
      case "date-of-birth" -> {
        model.addAttribute(
            "fieldHtml",
            List.of(chrome.html(Render.mustRender("date-input", Forms.dateField(app, errors)))));
        model.addAttribute("pageHeading", false);
      }
      case "email" -> {
        model.addAttribute(
            "fieldHtml",
            List.of(chrome.html(Render.mustRender("input", Forms.emailField(app, errors)))));
        model.addAttribute("pageHeading", false);
      }
      case "contact-preference" -> {
        model.addAttribute(
            "fieldHtml",
            List.of(
                chrome.html(Render.mustRender("radios", Forms.contactFields(app, errors)))));
        model.addAttribute("pageHeading", false);
      }
      case "where-you-will-fish" -> {
        model.addAttribute(
            "fieldHtml",
            List.of(
                chrome.html(Render.mustRender("checkboxes", Forms.regionFields(app, errors)))));
        model.addAttribute("pageHeading", false);
      }
      case "licence-length" -> {
        model.addAttribute(
            "fieldHtml",
            List.of(chrome.html(Render.mustRender("radios", Forms.licenceFields(app, errors)))));
        model.addAttribute("pageHeading", false);
      }
      case "start-month" -> {
        model.addAttribute(
            "fieldHtml",
            List.of(
                chrome.html(
                    Render.mustRender("select", Forms.monthField(app, errors, today())))));
        model.addAttribute("pageHeading", false);
      }
      case "address" -> {
        Map<String, Params> fields = Forms.addressFields(app, errors);
        String lines =
            Render.mustRender("input", fields.get("line1"))
                + Render.mustRender("input", fields.get("line2"))
                + Render.mustRender("input", fields.get("town"))
                + Render.mustRender("input", fields.get("postcode"));
        Params fieldset = fields.get("fieldset");
        fieldset.set("html", new TrustedHtml(lines));
        model.addAttribute(
            "fieldHtml",
            List.of(
                chrome.html(Render.mustRender("inset-text", fields.get("inset"))),
                chrome.html(Render.mustRender("fieldset", fieldset))));
        model.addAttribute("pageHeading", false);
      }
      case "evidence" -> {
        List<TrustedHtml> parts = new ArrayList<>();
        if (!app.evidenceFilename().isEmpty()) {
          parts.add(
              new TrustedHtml(
                  "<p class=\"govuk-body\">Current file: "
                      + uk.gov.example.govuk.Nunjucks.escape(app.evidenceFilename())
                      + "</p>"));
        }
        parts.add(chrome.html(Render.mustRender("file-upload", Forms.evidenceField(app, errors))));
        model.addAttribute("fieldHtml", parts);
        model.addAttribute("pageHeading", false);
      }
      case "additional-details" -> {
        model.addAttribute(
            "fieldHtml",
            List.of(
                chrome.html(
                    Render.mustRender("character-count", Forms.detailsField(app, errors)))));
        model.addAttribute("pageHeading", false);
      }
      default -> {
        Map<String, Params> fields = Forms.passwordFields(errors);
        model.addAttribute(
            "fieldHtml",
            List.of(
                chrome.html(Render.mustRender("password-input", fields.get("password"))),
                chrome.html(Render.mustRender("password-input", fields.get("confirm")))));
        model.addAttribute("pageHeading", false);
      }
    }
  }

  private List<Validation.FieldError> validate(
      LicenceApplication.Step step,
      Map<String, String> form,
      List<String> regions,
      MultipartFile evidence) {
    return switch (step.id()) {
      case "name" -> Validation.validateName(form.get("first-name"), form.get("last-name"));
      case "date-of-birth" ->
          Validation.validateDateOfBirth(
              form.get("date-of-birth-day"),
              form.get("date-of-birth-month"),
              form.get("date-of-birth-year"),
              today());
      case "email" -> Validation.validateEmail(form.get("email"));
      case "contact-preference" ->
          Validation.validateContactPreference(form.get("contact-by"), form.get("telephone"));
      case "where-you-will-fish" ->
          Validation.validateRegions(regions == null ? List.of() : regions);
      case "licence-length" -> Validation.validateLicenceLength(form.get("licence-length"));
      case "start-month" -> Validation.validateStartMonth(form.get("start-month"), today());
      case "address" ->
          Validation.validateAddress(
              form.get("address-line-1"), form.get("town"), form.get("postcode"));
      case "evidence" -> {
        String filename = "";
        if (evidence != null && !evidence.isEmpty()) {
          filename = Validation.safeFilename(evidence.getOriginalFilename());
        }
        yield Validation.validateEvidence(filename);
      }
      case "additional-details" ->
          Validation.validateAdditionalDetails(form.get("additional-details"));
      default -> Validation.validatePassword(form.get("password"), form.get("password-confirm"));
    };
  }

  private void apply(
      LicenceApplication.Step step,
      Map<String, String> form,
      List<String> regions,
      MultipartFile evidence,
      LicenceApplication application,
      boolean valid) {
    switch (step.id()) {
      case "name" -> Save.saveName(application, form.get("first-name"), form.get("last-name"), valid);
      case "date-of-birth" ->
          Save.saveDate(
              application,
              form.get("date-of-birth-day"),
              form.get("date-of-birth-month"),
              form.get("date-of-birth-year"),
              valid);
      case "email" -> Save.saveEmail(application, form.get("email"), valid);
      case "contact-preference" ->
          Save.saveContact(application, form.get("contact-by"), form.get("telephone"), valid);
      case "where-you-will-fish" ->
          Save.saveRegions(application, regions == null ? List.of() : regions, valid);
      case "licence-length" -> Save.saveLicence(application, form.get("licence-length"), valid);
      case "start-month" -> Save.saveMonth(application, form.get("start-month"), valid);
      case "address" ->
          Save.saveAddress(
              application,
              new Save.Address(
                  form.get("address-line-1"),
                  form.get("address-line-2"),
                  form.get("town"),
                  form.get("postcode")),
              valid);
      case "evidence" -> {
        boolean hasFile = evidence != null && !evidence.isEmpty();
        String filename = hasFile ? Validation.safeFilename(evidence.getOriginalFilename()) : "";
        Save.saveEvidence(application, filename, hasFile && !filename.isEmpty(), valid);
      }
      case "additional-details" ->
          Save.saveDetails(application, form.get("additional-details"), valid);
      default -> Save.savePassword(application, valid);
    }
  }

  private LocalDate today() {
    return LocalDate.now(clock);
  }
}
