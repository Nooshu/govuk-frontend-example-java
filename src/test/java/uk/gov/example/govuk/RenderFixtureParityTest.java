package uk.gov.example.govuk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Parity gate: for every component GOV.UK Frontend ships and every fixture it declares, the Java
 * renderer must return the fixture's HTML byte for byte (outer whitespace already trimmed by
 * {@link Render#render}).
 */
class RenderFixtureParityTest {

  private static Path componentsDir;

  @BeforeAll
  static void resolveComponentsDir() {
    componentsDir =
        Path.of(System.getProperty("user.dir"))
            .resolve("node_modules/govuk-frontend/dist/govuk/components");
    if (!Files.isDirectory(componentsDir)) {
      fail(
          "GOV.UK Frontend is not installed at "
              + componentsDir
              + "; run `npm install` from the project root");
    }
  }

  static Stream<Arguments> fixtures() throws IOException {
    List<String> components = Fixtures.fixtureComponents(componentsDir);
    if (components.isEmpty()) {
      fail("no components with fixtures found under " + componentsDir);
    }
    Stream.Builder<Arguments> builder = Stream.builder();
    for (String component : components) {
      Fixtures.FixtureSet set = Fixtures.loadFixtures(componentsDir, component);
      for (Fixtures.Fixture fixture : set.fixtures()) {
        builder.add(Arguments.of(component, fixture.name(), fixture));
      }
    }
    return builder.build();
  }

  @ParameterizedTest(name = "{0} / {1}")
  @MethodSource("fixtures")
  void renderMatchesFixtureHtml(String component, String fixtureName, Fixtures.Fixture fixture) {
    String got = Render.render(component, fixture.options());
    assertEquals(
        fixture.html(),
        got,
        () ->
            "HTML does not match fixture for "
                + component
                + " / "
                + fixtureName
                + "\n"
                + firstDifference(fixture.html(), got));
  }

  @Test
  void everyFixtureComponentHasARenderer() throws IOException {
    List<String> shipped = Fixtures.fixtureComponents(componentsDir);
    Set<String> supported = new HashSet<>(Render.components());
    for (String name : shipped) {
      assertTrue(
          supported.contains(name),
          "component \"" + name + "\" ships fixtures but has no Java renderer");
    }
  }

  private static String firstDifference(String want, String got) {
    String[] wantLines = want.split("\n", -1);
    String[] gotLines = got.split("\n", -1);
    int max = Math.max(wantLines.length, gotLines.length);
    for (int i = 0; i < max; i++) {
      String wantLine = i < wantLines.length ? wantLines[i] : "<missing line>";
      String gotLine = i < gotLines.length ? gotLines[i] : "<missing line>";
      if (!wantLine.equals(gotLine)) {
        return "first difference on line "
            + (i + 1)
            + "\nwant: "
            + quote(wantLine)
            + "\ngot:  "
            + quote(gotLine);
      }
    }
    return "line-by-line equal but strings differ\nwant: "
        + quote(want)
        + "\ngot:  "
        + quote(got);
  }

  private static String quote(String s) {
    return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }
}
