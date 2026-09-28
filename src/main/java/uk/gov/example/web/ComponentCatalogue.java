package uk.gov.example.web;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Catalogue copy for the local component preview pages. */
public final class ComponentCatalogue {

  private static final String DESIGN_SYSTEM = "https://design-system.service.gov.uk/components";

  private static final Map<String, ComponentsController.ComponentInfo> DETAILS = buildDetails();

  private ComponentCatalogue() {}

  public static ComponentsController.ComponentInfo describe(String name) {
    ComponentsController.ComponentInfo known = DETAILS.get(name);
    if (known != null) {
      return new ComponentsController.ComponentInfo(
          name, known.title(), known.description(), known.designSystemUrl());
    }
    return new ComponentsController.ComponentInfo(
        name, titleFromKebab(name), "GOV.UK Frontend component.", DESIGN_SYSTEM + "/" + name + "/");
  }

  private static Map<String, ComponentsController.ComponentInfo> buildDetails() {
    Map<String, ComponentsController.ComponentInfo> map = new HashMap<>();
    put(map, "accordion", "Accordion", "Lets users show and hide sections of related content.");
    put(map, "back-link", "Back link", "Link to the previous page in a journey.");
    put(map, "breadcrumbs", "Breadcrumbs", "Helps users move between levels of a section.");
    put(map, "button", "Button", "Starts or continues an action.");
    put(
        map,
        "character-count",
        "Character count",
        "Shows how many characters are left in a textarea.");
    put(map, "checkboxes", "Checkboxes", "Lets users select one or more options.");
    put(map, "cookie-banner", "Cookie banner", "Asks users to accept or reject analytics cookies.");
    put(map, "date-input", "Date input", "Asks users for a date they already know.");
    put(map, "details", "Details", "Hides content that only some users need.");
    put(
        map,
        "error-message",
        "Error message",
        "Tells users how to fix a field that failed validation.");
    put(map, "error-summary", "Error summary", "Summarises form errors at the top of the page.");
    put(
        map,
        "exit-this-page",
        "Exit this page",
        "Lets users leave a page quickly. For services where someone may be in danger.");
    put(
        map,
        "feedback",
        "Feedback",
        "Asks users what they think of a page. Trial component in Frontend 6.5.");
    put(map, "fieldset", "Fieldset", "Groups related form fields, such as an address.");
    put(map, "file-upload", "File upload", "Lets users select a file to upload.");
    put(
        map,
        "footer",
        "Footer",
        "Page footer with Open Government Licence and Crown copyright.");
    put(
        map,
        "generic-header",
        "Generic header",
        "Header for services that are not branded as GOV.UK. Shown in the catalogue only.");
    put(map, "header", "Header", "The GOV.UK masthead.");
    put(
        map,
        "hint",
        "Hint",
        "Extra help for a form field. Form controls include it; the catalogue shows it on its own.");
    put(map, "input", "Text input", "Lets users enter a single line of text.");
    put(map, "inset-text", "Inset text", "Draws attention to important content on the page.");
    put(
        map,
        "label",
        "Label",
        "Labels a form field. Form controls include it; the catalogue shows it on its own.");
    put(
        map,
        "language-navigation",
        "Language navigation",
        "Lets users switch between languages. Trial component in Frontend 6.5.");
    put(
        map,
        "notification-banner",
        "Notification banner",
        "Tells users about something that affects the whole service.");
    put(map, "pagination", "Pagination", "Splits a long list across pages.");
    put(map, "panel", "Panel", "Confirms a transaction is complete.");
    put(
        map,
        "password-input",
        "Password input",
        "Lets users enter a password, with a control to show or hide it.");
    put(
        map,
        "phase-banner",
        "Phase banner",
        "Shows users that the service is still being tried out.");
    put(map, "radios", "Radios", "Lets users select one option from a list.");
    put(map, "select", "Select", "Lets users choose one option from a long list.");
    put(
        map,
        "service-navigation",
        "Service navigation",
        "Shows the service name under the GOV.UK masthead.");
    put(map, "skip-link", "Skip link", "Lets keyboard users skip to the main content.");
    put(map, "summary-list", "Summary list", "Summarises answers so users can check them.");
    put(map, "table", "Table", "Shows information in rows and columns.");
    put(
        map,
        "tabs",
        "Tabs",
        "Lets users switch between related views. Content stays in the page without JavaScript.");
    put(map, "tag", "Tag", "Shows a short status, such as on a task list.");
    put(
        map,
        "task-list",
        "Task list",
        "Shows the tasks in an application and whether they are done.");
    put(
        map,
        "textarea",
        "Textarea",
        "Lets users enter more than one line of text. This service uses character count, which includes a textarea.");
    put(
        map,
        "warning-text",
        "Warning text",
        "Tells users about something important before they continue.");
    return Map.copyOf(map);
  }

  private static void put(
      Map<String, ComponentsController.ComponentInfo> map,
      String name,
      String title,
      String description) {
    String url =
        switch (name) {
          case "generic-header" ->
              "https://design-system.service.gov.uk/styles/page-template/";
          case "hint", "label" ->
              "https://design-system.service.gov.uk/get-started/labels-legends-headings/";
          case "input" -> DESIGN_SYSTEM + "/text-input/";
          default -> DESIGN_SYSTEM + "/" + name + "/";
        };
    map.put(name, new ComponentsController.ComponentInfo(name, title, description, url));
  }

  private static String titleFromKebab(String name) {
    String[] parts = name.split("-");
    StringBuilder title = new StringBuilder();
    for (String part : parts) {
      if (part.isEmpty()) {
        continue;
      }
      if (!title.isEmpty()) {
        title.append(' ');
      }
      title.append(part.substring(0, 1).toUpperCase(Locale.UK)).append(part.substring(1));
    }
    return title.toString();
  }
}
