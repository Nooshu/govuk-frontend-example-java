package uk.gov.example.govuk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Renderer registry and {@link #render} entrypoint for all GOV.UK Frontend components.
 */
public final class Render {

  private static final Map<String, Function<Params, String>> RENDERERS;

  static {
    Map<String, Function<Params, String>> map = new LinkedHashMap<>();
    map.put("accordion", ComponentsLists::renderAccordion);
    map.put("back-link", ComponentsText::renderBackLink);
    map.put("breadcrumbs", ComponentsChrome::renderBreadcrumbs);
    map.put("button", ComponentsButton::renderButton);
    map.put("character-count", ComponentsForms::renderCharacterCount);
    map.put("checkboxes", ComponentsForms::renderCheckboxes);
    map.put("cookie-banner", ComponentsChrome::renderCookieBanner);
    map.put("date-input", ComponentsForms::renderDateInput);
    map.put("details", ComponentsText::renderDetails);
    map.put("error-message", ComponentsText::renderErrorMessage);
    map.put("error-summary", ComponentsLists::renderErrorSummary);
    map.put("exit-this-page", ComponentsButton::renderExitThisPage);
    map.put("feedback", ComponentsText::renderFeedback);
    map.put("fieldset", ComponentsText::renderFieldset);
    map.put("file-upload", ComponentsForms::renderFileUpload);
    map.put("footer", ComponentsChrome::renderFooter);
    map.put("generic-header", ComponentsChrome::renderGenericHeader);
    map.put("header", ComponentsChrome::renderHeader);
    map.put("hint", ComponentsText::renderHint);
    map.put("input", ComponentsForms::renderInput);
    map.put("inset-text", ComponentsText::renderInsetText);
    map.put("label", ComponentsText::renderLabel);
    map.put("language-navigation", ComponentsChrome::renderLanguageNavigation);
    map.put("notification-banner", ComponentsLists::renderNotificationBanner);
    map.put("pagination", ComponentsChrome::renderPagination);
    map.put("panel", ComponentsText::renderPanel);
    map.put("password-input", ComponentsForms::renderPasswordInput);
    map.put("phase-banner", ComponentsText::renderPhaseBanner);
    map.put("radios", ComponentsForms::renderRadios);
    map.put("select", ComponentsForms::renderSelect);
    map.put("service-navigation", ComponentsChrome::renderServiceNavigation);
    map.put("skip-link", ComponentsText::renderSkipLink);
    map.put("summary-list", ComponentsLists::renderSummaryList);
    map.put("table", ComponentsLists::renderTable);
    map.put("tabs", ComponentsLists::renderTabs);
    map.put("tag", ComponentsText::renderTag);
    map.put("task-list", ComponentsLists::renderTaskList);
    map.put("textarea", ComponentsForms::renderTextarea);
    map.put("warning-text", ComponentsText::renderWarningText);
    RENDERERS = Collections.unmodifiableMap(map);
  }

  private Render() {}

  /**
   * Returns the HTML for one GOV.UK Frontend component, trimmed to match fixtures.
   *
   * @throws IllegalArgumentException when {@code component} is not registered
   */
  public static String render(String component, Params params) {
    Function<Params, String> renderer = RENDERERS.get(component);
    if (renderer == null) {
      throw new IllegalArgumentException(
          "govuk: \"" + component + "\" is not a GOV.UK Frontend component");
    }
    return renderer.apply(params == null ? new Params() : params).trim();
  }

  /** {@link #render} for call sites where the component name is a constant. */
  public static String mustRender(String component, Params params) {
    return render(component, params);
  }

  /** Names this package can render, sorted. */
  public static List<String> components() {
    List<String> names = new ArrayList<>(RENDERERS.keySet());
    Collections.sort(names);
    return names;
  }

  /** Heading level option, falling back to {@code fallback} when not set. */
  static String heading(Object level, String fallback) {
    if (Nunjucks.truthy(level)) {
      return Nunjucks.str(level);
    }
    return fallback;
  }

  /** {@code (" " + option if option)} idiom for appending to a class list. */
  static String concatIf(String prefix, Object value) {
    if (!Nunjucks.truthy(value)) {
      return "";
    }
    return prefix + Nunjucks.str(value);
  }
}
