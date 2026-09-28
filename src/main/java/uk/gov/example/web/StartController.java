package uk.gov.example.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.example.config.AppProperties;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.Render;
import uk.gov.example.govuk.TrustedHtml;
import uk.gov.example.session.SessionData;

@Controller
public class StartController {

  private final PageChrome chrome;
  private final AppProperties properties;

  public StartController(PageChrome chrome, AppProperties properties) {
    this.chrome = chrome;
    this.properties = properties;
  }

  @GetMapping("/")
  public String startEn(HttpServletRequest request, Model model) {
    return start(request, model, "en");
  }

  @GetMapping("/cy")
  public String startCy(HttpServletRequest request, Model model) {
    return start(request, model, "cy");
  }

  @GetMapping("/new-application")
  public String newApplication(HttpServletRequest request) {
    SessionData session = WebSessions.require(request);
    session.application().reset();
    session.clearFlashErrors();
    session.clearNotice();
    return "redirect:/";
  }

  private String start(HttpServletRequest request, Model model, String lang) {
    SessionData session = WebSessions.require(request);
    boolean welsh = "cy".equals(lang);
    String heading =
        welsh ? "Gwneud cais am drwydded bysgota" : "Apply for a rod fishing licence";
    chrome.apply(model, request, session, heading, false, lang, true, "");

    model.addAttribute(
        "lede",
        welsh
            ? "Defnyddiwch y gwasanaeth hwn i wneud cais am drwydded i bysgota gyda gwialen."
            : "Use this service to apply for a licence to fish with a rod.");
    model.addAttribute(
        "timing",
        welsh ? "Mae’n cymryd tua 10 munud." : "Applying takes about 10 minutes.");
    model.addAttribute(
        "startButtonHtml",
        chrome.html(
            Render.mustRender(
                "button",
                Params.of(
                    "text",
                    welsh ? "Dechrau nawr" : "Start now",
                    "href",
                    "/task-list",
                    "isStartButton",
                    true))));
    model.addAttribute(
        "notificationHtml",
        chrome.html(
            Render.mustRender(
                "notification-banner",
                Params.of(
                    "titleText",
                    welsh ? "Pwysig" : "Important",
                    "text",
                    welsh
                        ? "Mae trwydded gwialen 2026 i 2027 ar gael nawr."
                        : "The 2026 to 2027 rod licence is now available."))));
    model.addAttribute(
        "warningHtml",
        chrome.html(
            Render.mustRender(
                "warning-text",
                Params.of(
                    "text",
                    welsh
                        ? "Rhaid i chi gael trwydded gwialen ddilys cyn i chi bysgota."
                        : "You must have a valid rod licence before you fish.",
                    "iconFallbackText",
                    welsh ? "Rhybudd" : "Warning"))));
    model.addAttribute(
        "insetHtml",
        chrome.html(
            Render.mustRender(
                "inset-text",
                Params.of(
                    "text",
                    welsh
                        ? "Mae gweddill yr enghraifft hon yn Saesneg."
                        : "You need to be 13 or over. This example does not take payment."))));
    model.addAttribute(
        "detailsHtml",
        chrome.html(
            Render.mustRender(
                "details",
                Params.of(
                    "summaryText",
                    welsh ? "Beth fydd ei angen arnoch" : "What you will need",
                    "html",
                    new TrustedHtml(
                        welsh
                            ? "<ul class=\"govuk-list govuk-list--bullet\"><li>Eich enw</li><li>Eich dyddiad geni</li><li>Eich cyfeiriad</li></ul>"
                            : "<ul class=\"govuk-list govuk-list--bullet\"><li>Your name</li><li>Your date of birth</li><li>Your address</li></ul>")))));
    model.addAttribute("demosEnabled", properties.demosEnabled());
    return "pages/start";
  }
}
