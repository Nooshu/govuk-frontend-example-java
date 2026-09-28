package uk.gov.example.govuk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Proves the component preview banner's claim ("This HTML matches the official fixture.") uses the
 * same comparison as {@link RenderFixtureParityTest}: Java {@link Render} output versus the
 * installed {@code fixtures.json} {@code html}, not a hard-coded success message.
 */
class PreviewParityBannerContractTest {

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

  @Test
  void everyPreviewWouldShowSuccessBanner() throws IOException {
    List<String> mismatches = new ArrayList<>();
    int checked = 0;
    for (String component : Fixtures.fixtureComponents(componentsDir)) {
      Fixtures.FixtureSet set = Fixtures.loadFixtures(componentsDir, component);
      for (Fixtures.Fixture fixture : set.fixtures()) {
        checked++;
        String rendered = Render.mustRender(component, fixture.options());
        // Exact check used by ComponentsController for the parity banner.
        if (!rendered.equals(fixture.html())) {
          mismatches.add(component + " / " + fixture.name());
        }
      }
    }
    assertThat(checked).as("expected to check every official fixture").isGreaterThan(700);
    assertThat(mismatches)
        .as("preview pages would wrongly claim a match for these fixtures")
        .isEmpty();
  }
}
