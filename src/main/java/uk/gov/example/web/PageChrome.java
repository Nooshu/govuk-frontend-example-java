package uk.gov.example.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import uk.gov.example.config.AppProperties;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.Render;
import uk.gov.example.govuk.TrustedHtml;
import uk.gov.example.session.SessionData;

/** Shared chrome HTML (header, banners, footer) for Thymeleaf layouts. */
@Component
public class PageChrome {

  private final AppProperties properties;
  private final AssetRegistry assets;
  private final uk.gov.example.baseline.Policy policy;

  public PageChrome(
      AppProperties properties,
      AssetRegistry assets,
      uk.gov.example.baseline.Policy policy) {
    this.properties = Objects.requireNonNull(properties);
    this.assets = Objects.requireNonNull(assets);
    this.policy = Objects.requireNonNull(policy);
  }

  public void apply(
      Model model,
      HttpServletRequest request,
      SessionData session,
      String heading,
      boolean hasErrors,
      String lang,
      boolean showFeedback,
      String mainClasses) {
    boolean welsh = "cy".equals(lang);
    String serviceName =
        welsh ? "Gwneud cais am drwydded bysgota" : "Apply for a rod fishing licence";
    String pageTitle = (hasErrors ? "Error: " : "") + heading + " – " + serviceName;
    String homepageUrl = welsh ? "/cy" : "/";
    String skipLinkText = welsh ? "Neidio i'r prif gynnwys" : "Skip to main content";

    model.addAttribute("pageTitle", pageTitle);
    model.addAttribute("heading", heading);
    model.addAttribute("htmlLang", welsh ? "cy" : "en");
    model.addAttribute("homepageUrl", homepageUrl);
    model.addAttribute("skipLinkText", skipLinkText);
    model.addAttribute("csrf", session.csrfToken());
    model.addAttribute("returnPath", safeLocalPath(request));
    model.addAttribute("stylesheetHref", assets.stylesheetHref());
    model.addAttribute("appModuleHref", assets.appModuleHref());
    model.addAttribute("jsEnabledSnippet", policy.jsEnabledSnippet());
    model.addAttribute("mainClasses", mainClasses == null ? "" : mainClasses);
    model.addAttribute("showFeedback", showFeedback);
    model.addAttribute("demosEnabled", properties.demosEnabled());
    model.addAttribute("frontendVersion", properties.frontendVersion());
    model.addAttribute("hasErrors", hasErrors);
    model.addAttribute("backLinkHtml", null);
    model.addAttribute("breadcrumbsHtml", null);

    model.addAttribute(
        "skipLinkHtml",
        html(Render.mustRender("skip-link", Params.of("href", "#main-content", "text", skipLinkText))));
    model.addAttribute(
        "headerHtml",
        html(Render.mustRender("header", Params.of("homepageUrl", homepageUrl))));
    model.addAttribute("serviceNavigationHtml", html(serviceNavigation(welsh, serviceName)));
    model.addAttribute("phaseBannerHtml", html(Render.mustRender("phase-banner", phaseBanner(welsh))));
    model.addAttribute("demoBannerHtml", html(Render.mustRender("notification-banner", demoBanner(welsh))));
    model.addAttribute("footerHtml", html(Render.mustRender("footer", footer(welsh))));
    model.addAttribute("feedbackHtml", html(Render.mustRender("feedback", feedback())));

    Params cookieBanner = cookieBanner(session);
    if (cookieBanner != null) {
      model.addAttribute(
          "cookieBannerHtml", html(Render.mustRender("cookie-banner", cookieBanner)));
    } else {
      model.addAttribute("cookieBannerHtml", null);
    }
  }

  public TrustedHtml html(String value) {
    return new TrustedHtml(value);
  }

  private String serviceNavigation(boolean welsh, String serviceName) {
    String ariaLabel = welsh ? "Iaith" : "Language";
    List<Object> items;
    if (welsh) {
      items =
          List.of(
              Params.of("text", "English", "lang", "en", "href", "/"),
              Params.of("text", "Cymraeg", "lang", "cy", "current", true));
    } else {
      items =
          List.of(
              Params.of("text", "English", "lang", "en", "current", true),
              Params.of("text", "Cymraeg", "lang", "cy", "href", "/cy"));
    }
    String languages =
        Render.mustRender(
            "language-navigation", Params.of("ariaLabel", ariaLabel, "items", items));
    return Render.mustRender(
        "service-navigation",
        Params.of(
            "serviceName",
            serviceName,
            "serviceUrl",
            welsh ? "/cy" : "/",
            "slots",
            Params.of("end", new TrustedHtml(languages))));
  }

  private static Params phaseBanner(boolean welsh) {
    if (welsh) {
      return Params.of(
          "tag",
          Params.of("text", "Enghraifft"),
          "html",
          new TrustedHtml(
              "Mae hon yn arddangosiad – nid gwasanaeth llywodraeth byw mohono. Bydd eich <a class=\"govuk-link\" href=\"/about\">adborth</a> yn helpu i wella’r enghraifft."));
    }
    return Params.of(
        "tag",
        Params.of("text", "Example"),
        "html",
        new TrustedHtml(
            "This is a demonstration – it is not a live government service. Your <a class=\"govuk-link\" href=\"/about\">feedback</a> will help us improve the example."));
  }

  private static Params demoBanner(boolean welsh) {
    Params banner =
        Params.of("classes", "app-demo-banner", "titleId", "app-demo-banner-title");
    if (welsh) {
      banner.set("titleText", "Pwysig");
      banner.set(
          "text", "Mae hwn yn arddangosiad byw. Nid gwasanaeth llywodraeth go iawn mohono.");
    } else {
      banner.set("titleText", "Important");
      banner.set("text", "This is a live demo. It is not a real government service.");
    }
    return banner;
  }

  private Params footer(boolean welsh) {
    List<Object> items = new ArrayList<>();
    items.add(Params.of("href", "/help", "text", "Help"));
    items.add(Params.of("href", "/fees", "text", "Licence fees"));
    items.add(Params.of("href", "/updates", "text", "Service updates"));
    items.add(Params.of("href", "/guidance", "text", "Guidance"));
    items.add(Params.of("href", "/cookies", "text", "Cookies"));
    items.add(Params.of("href", "/accessibility", "text", "Accessibility"));
    items.add(Params.of("href", "/about", "text", "About this example"));
    if (properties.demosEnabled()) {
      items.add(Params.of("href", "/components", "text", "Component catalogue"));
    }
    Params footer = Params.of("meta", Params.of("items", items));
    if (welsh) {
      footer.set(
          "contentLicence",
          Params.of(
              "html",
              new TrustedHtml(
                  "Mae’r holl gynnwys ar gael dan <a class=\"govuk-footer__link\" href=\"https://www.nationalarchives.gov.uk/doc/open-government-licence-cymraeg/version/3/\" rel=\"license\">Drwydded y Llywodraeth Agored v3.0</a>, ac eithrio lle nodir yn wahanol")));
      footer.set("copyright", Params.of("html", new TrustedHtml("<span>Hawlfraint y Goron</span>")));
    }
    return footer;
  }

  private static Params feedback() {
    return Params.of(
        "titleText",
        "Help us improve this service",
        "html",
        new TrustedHtml(
            "<p class=\"govuk-body\">This example does not send feedback. <a class=\"govuk-link\" href=\"/help\">Get help with this example</a>.</p>"));
  }

  private static Params cookieBanner(SessionData session) {
    String confirm = session.cookieBannerConfirm();
    if ("accept".equals(confirm)) {
      return confirmationBanner("You have accepted analytics cookies.");
    }
    if ("reject".equals(confirm)) {
      return confirmationBanner("You have rejected analytics cookies.");
    }
    if (session.cookieAnalytics() != null) {
      return null;
    }
    return Params.of(
        "messages",
        List.of(
            Params.of(
                "headingText",
                "Cookies on Apply for a rod fishing licence",
                "text",
                "We use analytics cookies to understand how you use this example service. This example does not set analytics cookies.",
                "actions",
                List.of(
                    Params.of(
                        "text",
                        "Accept analytics cookies",
                        "type",
                        "submit",
                        "name",
                        "cookies",
                        "value",
                        "accept"),
                    Params.of(
                        "text",
                        "Reject analytics cookies",
                        "type",
                        "submit",
                        "name",
                        "cookies",
                        "value",
                        "reject"),
                    Params.of("text", "View cookies", "href", "/cookies")))));
  }

  private static Params confirmationBanner(String text) {
    return Params.of(
        "messages",
        List.of(
            Params.of(
                "text",
                text,
                "role",
                "alert",
                "actions",
                List.of(
                    Params.of(
                        "text", "Hide cookie message", "type", "submit", "name", "cookies", "value",
                        "hide")))));
  }

  private static String safeLocalPath(HttpServletRequest request) {
    String path = request.getRequestURI();
    String query = request.getQueryString();
    if (query != null && !query.isEmpty()) {
      path = path + "?" + query;
    }
    if (!path.startsWith("/")
        || path.startsWith("//")
        || path.contains("://")
        || path.contains("\\")
        || path.contains("\r")
        || path.contains("\n")) {
      return "/";
    }
    return path;
  }
}
