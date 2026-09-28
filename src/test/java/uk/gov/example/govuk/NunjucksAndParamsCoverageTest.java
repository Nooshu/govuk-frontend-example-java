package uk.gov.example.govuk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class NunjucksAndParamsCoverageTest {

  @Test
  void nunjucksHelpers() {
    assertThat(Nunjucks.escape("a&b<'\">\\")).contains("&amp;").contains("&#39;");
    assertThat(Nunjucks.escape(null)).isEmpty();
    assertThat(Nunjucks.out(new TrustedHtml("<b>"))).isEqualTo("<b>");
    assertThat(Nunjucks.out("x")).isEqualTo("x");
    assertThat(Nunjucks.out(null)).isEmpty();
    assertThat(Nunjucks.str(null)).isEmpty();
    assertThat(Nunjucks.str(Undefined.INSTANCE)).isEmpty();
    assertThat(Nunjucks.str(true)).isEqualTo("true");
    assertThat(Nunjucks.str(false)).isEqualTo("false");
    assertThat(Nunjucks.str(new JsonNumber("1.0"))).isNotBlank();
    assertThat(Nunjucks.str(List.of("a", "b"))).isEqualTo("a,b");
    assertThat(Nunjucks.truthy(null)).isFalse();
    assertThat(Nunjucks.truthy("")).isFalse();
    assertThat(Nunjucks.truthy(Undefined.INSTANCE)).isFalse();
    assertThat(Nunjucks.truthy("a")).isTrue();
    assertThat(Nunjucks.truthy(false)).isFalse();
    assertThat(Nunjucks.truthy(new JsonNumber("0"))).isFalse();
    assertThat(Nunjucks.truthy(new JsonNumber("2"))).isTrue();
    assertThat(Nunjucks.trim(null)).isEmpty();
    assertThat(Nunjucks.trim(" a ")).isEqualTo("a");
    assertThat(Nunjucks.indent("a\nb", 2, true)).contains("a");
    assertThat(Nunjucks.length(null)).isEqualTo(0);
    assertThat(Nunjucks.length("ab")).isEqualTo(2);
    assertThat(Nunjucks.length(List.of(1, 2, 3))).isEqualTo(3);
    assertThat(Nunjucks.length(Params.of("a", 1))).isEqualTo(1);
    assertThat(Nunjucks.length(true)).isEqualTo(0);
    assertThat(Nunjucks.at(List.of("x", "y"), 1)).isEqualTo("y");
    assertThat(Nunjucks.at(List.of("x"), 5)).isEqualTo(Undefined.INSTANCE);
    assertThat(Nunjucks.at(null, 0)).isEqualTo(Undefined.INSTANCE);
    assertThat(Nunjucks.looseEq(null, null)).isTrue();
    assertThat(Nunjucks.looseEq("a", "a")).isTrue();
    assertThat(Nunjucks.looseEq("a", "b")).isFalse();
    assertThat(Nunjucks.looseEq(true, true)).isTrue();
    assertThat(Nunjucks.looseEq(true, new JsonNumber("1"))).isTrue();
    assertThat(Nunjucks.strictEq("1", "1")).isTrue();
    assertThat(Nunjucks.strictEq(null, null)).isTrue();
    assertThat(Nunjucks.strictEq("1", new JsonNumber("1"))).isFalse();
    assertThat(Nunjucks.contains("b", List.of("a", "b"))).isTrue();
    assertThat(Nunjucks.contains("b", "abc")).isTrue();
    assertThat(Nunjucks.contains("a", null)).isFalse();
    assertThat(Nunjucks.isUndefined(Undefined.INSTANCE)).isTrue();
  }

  @Test
  void paramsAndJsonNumber() throws Exception {
    assertThatThrownBy(() -> Params.of("only")).isInstanceOf(IllegalArgumentException.class);
    Params p = Params.of("a", 1, "b", "two");
    assertThat(p.has("a")).isTrue();
    assertThat(p.has("missing")).isFalse();
    assertThat(Params.get(null, "a")).isEqualTo(Undefined.INSTANCE);
    assertThat(Params.get(p, "a")).isEqualTo(1);
    p.decodeJson("{\"x\":1,\"y\":true,\"z\":null,\"n\":1.5,\"s\":\"t\",\"arr\":[1]}");
    assertThat(p.has("x")).isTrue();
    assertThatThrownBy(() -> p.decodeJson("[]")).isInstanceOf(IllegalArgumentException.class);
    assertThat(Params.parseJson("\"hi\"")).isEqualTo("hi");
    JsonNumber n = new JsonNumber("2.50");
    assertThat(n.doubleValue()).isEqualTo(2.5);
    assertThat(n.toJsString()).isNotBlank();
    assertThat(n.toString()).isEqualTo("2.50");
    assertThat(new JsonNumber("0").toJsString()).isNotBlank();
  }

  @Test
  void formsEdgeCasesRender() {
    assertThat(Render.mustRender("textarea", Params.of("name", "t", "id", "t"))).contains("textarea");
    assertThat(
            Render.mustRender(
                "input",
                Params.of(
                    "id",
                    "i",
                    "name",
                    "i",
                    "label",
                    Params.of("text", "L"),
                    "attributes",
                    Params.of("data-x", "1"))))
        .contains("data-x");
  }
}
