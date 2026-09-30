package uk.gov.example.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.example.config.AppProperties;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.Render;
import uk.gov.example.service.Forms;
import uk.gov.example.service.Validation;
import uk.gov.example.session.SessionData;

@Controller
public class SupportController {

  private final PageChrome chrome;
  private final AppProperties properties;

  public SupportController(PageChrome chrome, AppProperties properties) {
    this.chrome = chrome;
    this.properties = properties;
  }

  @GetMapping("/fees")
  public String fees(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    chrome.apply(model, request, session, "Licence fees", false, "en", false, "");
    model.addAttribute("breadcrumbsHtml", breadcrumbs("Licence fees"));
    model.addAttribute(
        "tableHtml", chrome.html(Render.mustRender("table", Forms.feesTable())));
    return "pages/fees";
  }

  @GetMapping("/help")
  public String help(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    chrome.apply(model, request, session, "Help", false, "en", true, "");
    model.addAttribute("breadcrumbsHtml", breadcrumbs("Help"));
    model.addAttribute(
        "accordionHtml", chrome.html(Render.mustRender("accordion", Forms.helpAccordion())));
    return "pages/help";
  }

  @GetMapping("/guidance")
  public String guidance(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    chrome.apply(model, request, session, "Guidance", false, "en", false, "");
    model.addAttribute("breadcrumbsHtml", breadcrumbs("Guidance"));
    model.addAttribute(
        "tabsHtml", chrome.html(Render.mustRender("tabs", Forms.guidanceTabs())));
    return "pages/guidance";
  }

  @GetMapping("/accessibility")
  public String accessibility(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    chrome.apply(model, request, session, "Accessibility statement", false, "en", true, "");
    model.addAttribute("breadcrumbsHtml", breadcrumbs("Accessibility statement"));
    return "pages/accessibility";
  }

  @GetMapping("/about")
  public String about(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    chrome.apply(model, request, session, "About this example", false, "en", true, "");
    model.addAttribute("breadcrumbsHtml", breadcrumbs("About this example"));
    model.addAttribute("frontendVersion", properties.frontendVersion());
    model.addAttribute("demosEnabled", properties.demosEnabled());
    return "pages/about";
  }

  @GetMapping("/examples")
  public String examples(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    chrome.apply(model, request, session, "Example pages", false, "en", true, "");
    model.addAttribute("breadcrumbsHtml", breadcrumbs("Example pages"));
    return "pages/examples";
  }

  @GetMapping("/examples/exit-this-page")
  public String exitThisPage(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    chrome.apply(model, request, session, "Exit this page", false, "en", true, "");
    model.addAttribute(
        "backLinkHtml",
        chrome.html(Render.mustRender("back-link", Params.of("text", "Back", "href", "/examples"))));
    model.addAttribute(
        "exitThisPageHtml",
        chrome.html(
            Render.mustRender(
                "exit-this-page",
                Params.of("redirectUrl", "https://www.bbc.co.uk/weather"))));
    model.addAttribute(
        "warningHtml",
        chrome.html(
            Render.mustRender(
                "warning-text",
                Params.of(
                    "text",
                    "Use this component only on services where someone may be in danger.",
                    "iconFallbackText",
                    "Warning"))));
    model.addAttribute(
        "insetHtml",
        chrome.html(
            Render.mustRender(
                "inset-text",
                Params.of(
                    "text",
                    "This page is an example of the component. It is not part of the rod licence application. Choosing the button leaves this example and opens the BBC weather forecast."))));
    return "pages/exit-this-page";
  }

  @GetMapping("/updates")
  public String updates(
      HttpServletRequest request,
      Model model,
      @RequestParam(value = "page", required = false) String pageParam) {
    if (pageParam != null && !"1".equals(pageParam) && !"2".equals(pageParam)) {
      return "redirect:/updates";
    }
    int page = "2".equals(pageParam) ? 2 : 1;
    SessionData session = WebSessions.require(request);
    chrome.apply(model, request, session, "Service updates", false, "en", false, "");
    model.addAttribute("breadcrumbsHtml", breadcrumbs("Service updates"));
    model.addAttribute(
        "body",
        page == 2
            ? "There are no further fee changes planned in this example."
            : "Example fees for the 2026 to 2027 season are on the fees page.");
    Params pagination =
        Params.of(
            "items",
            List.of(
                Params.of("number", 1, "href", "/updates", "current", page == 1),
                Params.of("number", 2, "href", "/updates?page=2", "current", page == 2)));
    if (page > 1) {
      pagination.set("previous", Params.of("href", "/updates"));
    }
    if (page < 2) {
      pagination.set("next", Params.of("href", "/updates?page=2"));
    }
    model.addAttribute(
        "paginationHtml", chrome.html(Render.mustRender("pagination", pagination)));
    return "pages/updates";
  }

  @GetMapping("/cookies")
  public String cookiesGet(HttpServletRequest request, Model model) {
    SessionData session = WebSessions.require(request);
    List<Validation.FieldError> errors = session.takeFlashErrors("/cookies");
    String notice = session.takeNotice("/cookies");
    chrome.apply(model, request, session, "Cookies", !errors.isEmpty(), "en", false, "");
    model.addAttribute("breadcrumbsHtml", breadcrumbs("Cookies"));
    Params summary = Forms.errorSummary(errors);
    model.addAttribute(
        "errorSummaryHtml",
        summary == null ? null : chrome.html(Render.mustRender("error-summary", summary)));
    if (notice != null) {
      model.addAttribute(
          "noticeHtml",
          chrome.html(
              Render.mustRender(
                  "notification-banner",
                  Params.of("type", "success", "titleText", "Success", "text", notice))));
    } else {
      model.addAttribute("noticeHtml", null);
    }
    model.addAttribute(
        "radiosHtml",
        chrome.html(
            Render.mustRender(
                "radios", Forms.cookieFields(session.cookieAnalytics(), errors))));
    model.addAttribute(
        "saveButtonHtml",
        chrome.html(Render.mustRender("button", Params.of("text", "Save cookie settings"))));
    return "pages/cookies";
  }

  @PostMapping("/cookies")
  public String cookiesPost(
      HttpServletRequest request,
      @RequestParam(value = "csrf", required = false) String csrf,
      @RequestParam(value = "analytics", required = false) String analytics) {
    SessionData session = WebSessions.require(request);
    if (!WebSessions.csrfOk(session, csrf)) {
      return "redirect:/session-expired";
    }
    List<Validation.FieldError> errors = Validation.validateCookieChoice(analytics);
    if (!errors.isEmpty()) {
      session.setFlashErrors("/cookies", errors);
      session.clearNotice();
      return "redirect:/cookies";
    }
    session.setCookieAnalytics(analytics);
    session.setCookieBannerConfirm(null);
    session.setCookieBannerDismissed(true);
    session.clearFlashErrors();
    session.setNotice("/cookies", "Your cookie settings were saved");
    return "redirect:/cookies";
  }

  @PostMapping("/cookie-choices")
  public String cookieChoices(
      HttpServletRequest request,
      @RequestParam(value = "csrf", required = false) String csrf,
      @RequestParam(value = "cookies", required = false) String choice,
      @RequestParam(value = "returnPath", required = false) String returnPath) {
    SessionData session = WebSessions.require(request);
    if (!WebSessions.csrfOk(session, csrf)) {
      return "redirect:/session-expired";
    }
    if ("accept".equals(choice) || "reject".equals(choice)) {
      session.setCookieAnalytics("accept".equals(choice) ? "yes" : "no");
      session.setCookieBannerConfirm(choice);
      session.setCookieBannerDismissed(false);
    } else if ("hide".equals(choice)) {
      session.setCookieBannerConfirm(null);
      session.setCookieBannerDismissed(true);
    }
    return "redirect:" + WebSessions.safeReturn(returnPath);
  }

  private Object breadcrumbs(String current) {
    return chrome.html(
        Render.mustRender(
            "breadcrumbs",
            Params.of(
                "items",
                List.of(
                    Params.of("href", "/", "text", "Home"),
                    Params.of("text", current)))));
  }
}
