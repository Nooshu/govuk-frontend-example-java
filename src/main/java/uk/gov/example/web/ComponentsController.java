package uk.gov.example.web;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.example.config.AppProperties;
import uk.gov.example.govuk.Fixtures;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.Render;
import uk.gov.example.govuk.TrustedHtml;
import uk.gov.example.session.SessionData;

@Controller
public class ComponentsController {

  private final PageChrome chrome;
  private final AppProperties properties;

  public ComponentsController(PageChrome chrome, AppProperties properties) {
    this.chrome = chrome;
    this.properties = properties;
  }

  @GetMapping("/components")
  public String catalogue(HttpServletRequest request, Model model) {
    requireDemos();
    SessionData session = WebSessions.require(request);
    chrome.apply(model, request, session, "Component catalogue", false, "en", false, "");
    model.addAttribute("breadcrumbsHtml", breadcrumbs("Component catalogue"));
    try {
      List<ComponentInfo> components = catalogueEntries();
      model.addAttribute("components", components);
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    return "pages/components";
  }

  @GetMapping("/components/{name}")
  public String component(
      HttpServletRequest request,
      Model model,
      @PathVariable String name,
      @RequestParam(value = "fixture", required = false) String fixtureName) {
    requireDemos();
    SessionData session = WebSessions.require(request);
    try {
      Path root = Path.of(properties.componentsRoot()).toAbsolutePath().normalize();
      if (!Fixtures.fixtureComponents(root).contains(name)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
      Fixtures.FixtureSet set = Fixtures.loadFixtures(root, name);
      Fixtures.Fixture fixture = selectFixture(set.fixtures(), fixtureName);
      if (fixture == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
      String rendered = Render.mustRender(name, fixture.options());
      boolean parity = rendered.equals(fixture.html().trim());
      ComponentInfo info = ComponentCatalogue.describe(name);
      chrome.apply(model, request, session, info.title(), false, "en", false, "");
      model.addAttribute(
          "backLinkHtml",
          chrome.html(
              Render.mustRender("back-link", Params.of("text", "Back", "href", "/components"))));
      model.addAttribute("componentName", name);
      model.addAttribute("componentTitle", info.title());
      model.addAttribute("designSystemUrl", info.designSystemUrl());
      model.addAttribute("description", fixture.description());
      model.addAttribute("fixtureName", fixture.name());
      model.addAttribute("renderedHtml", new TrustedHtml(rendered));
      model.addAttribute(
          "parityHtml",
          chrome.html(
              Render.mustRender(
                  "notification-banner",
                  Params.of(
                      "type",
                      parity ? "success" : null,
                      "titleText",
                      parity ? "Success" : "Important",
                      "text",
                      parity
                          ? "This HTML matches the official fixture."
                          : "This HTML does not match the official fixture."))));
      if (!fixture.description().isBlank()) {
        model.addAttribute(
            "descriptionHtml",
            chrome.html(
                Render.mustRender("inset-text", Params.of("text", fixture.description()))));
      } else {
        model.addAttribute("descriptionHtml", null);
      }
      List<FixtureLink> links = new ArrayList<>();
      for (Fixtures.Fixture f : set.fixtures()) {
        links.add(new FixtureLink(f.name(), f.name().equals(fixture.name())));
      }
      model.addAttribute("fixtures", links);
      model.addAttribute("frontendVersion", properties.frontendVersion());
      return "pages/component";
    } catch (ResponseStatusException e) {
      throw e;
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @GetMapping("/components/{name}/fixture")
  public ResponseEntity<String> fixtureFragment(
      @PathVariable String name, @RequestParam(value = "fixture", required = false) String fixtureName) {
    requireDemos();
    try {
      Path root = Path.of(properties.componentsRoot()).toAbsolutePath().normalize();
      if (!Fixtures.fixtureComponents(root).contains(name)) {
        return ResponseEntity.notFound().build();
      }
      Fixtures.FixtureSet set = Fixtures.loadFixtures(root, name);
      Fixtures.Fixture fixture = selectFixture(set.fixtures(), fixtureName);
      if (fixture == null) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok()
          .contentType(MediaType.TEXT_HTML)
          .body(fixture.html());
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  private void requireDemos() {
    if (!properties.demosEnabled()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  private List<ComponentInfo> catalogueEntries() throws IOException {
    Path root = Path.of(properties.componentsRoot()).toAbsolutePath().normalize();
    List<ComponentInfo> entries = new ArrayList<>();
    for (String name : Fixtures.fixtureComponents(root)) {
      entries.add(ComponentCatalogue.describe(name));
    }
    return entries;
  }

  private static Fixtures.Fixture selectFixture(List<Fixtures.Fixture> fixtures, String requested) {
    if (requested != null && !requested.isEmpty()) {
      for (Fixtures.Fixture fixture : fixtures) {
        if (fixture.name().equals(requested)) {
          return fixture;
        }
      }
      return null;
    }
    for (Fixtures.Fixture fixture : fixtures) {
      if (!fixture.hidden()) {
        return fixture;
      }
    }
    return fixtures.isEmpty() ? null : fixtures.get(0);
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

  public record FixtureLink(String name, boolean current) {}

  public record ComponentInfo(
      String name, String title, String description, String designSystemUrl) {}
}
