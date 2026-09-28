package uk.gov.example.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.example.govuk.Params;
import uk.gov.example.govuk.Render;
import uk.gov.example.govuk.TrustedHtml;

/** Thymeleaf-facing helper that renders GOV.UK components via {@link Render}. */
@Component("govuk")
public class GovukViewHelper {

  public TrustedHtml render(String name, Params params) {
    return new TrustedHtml(Render.mustRender(name, params));
  }

  /** Convenience for templates that pass a simple map of scalar options. */
  public TrustedHtml render(String name, Map<String, Object> options) {
    return render(name, toParams(options));
  }

  public static Params toParams(Map<String, Object> options) {
    Params params = new Params();
    if (options == null) {
      return params;
    }
    for (Map.Entry<String, Object> entry : options.entrySet()) {
      params.set(entry.getKey(), convert(entry.getValue()));
    }
    return params;
  }

  @SuppressWarnings("unchecked")
  private static Object convert(Object value) {
    if (value instanceof Map<?, ?> map) {
      Params nested = new Params();
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        nested.set(String.valueOf(entry.getKey()), convert(entry.getValue()));
      }
      return nested;
    }
    if (value instanceof List<?> list) {
      return list.stream().map(GovukViewHelper::convert).toList();
    }
    return value;
  }

  public TrustedHtml button(String text) {
    return render("button", Params.of("text", text));
  }

  public TrustedHtml buttonLink(String text, String href) {
    return render("button", Params.of("text", text, "href", href));
  }

  public TrustedHtml startButton(String text, String href) {
    return render("button", Params.of("text", text, "href", href, "isStartButton", true));
  }

  /** Builds a LinkedHashMap preserving insertion order for catalogue links etc. */
  public static Map<String, Object> map(Object... pairs) {
    if (pairs.length % 2 != 0) {
      throw new IllegalArgumentException("map needs even arguments");
    }
    Map<String, Object> result = new LinkedHashMap<>();
    for (int i = 0; i < pairs.length; i += 2) {
      result.put(String.valueOf(pairs[i]), pairs[i + 1]);
    }
    return result;
  }
}
