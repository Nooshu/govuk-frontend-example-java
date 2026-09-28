package uk.gov.example.govuk;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fixture loading helpers used by the parity suite.
 */
public final class Fixtures {

  private Fixtures() {}

  /**
   * One entry from a component's {@code fixtures.json}: options and the HTML GOV.UK Frontend
   * produces for them.
   */
  public record Fixture(
      String name, Params options, boolean hidden, String description, String html) {}

  /** A component's whole {@code fixtures.json} file. */
  public record FixtureSet(String component, List<Fixture> fixtures) {}

  /**
   * Component names under {@code componentsDir} that ship a {@code fixtures.json}, sorted.
   */
  public static List<String> fixtureComponents(Path componentsDir) throws IOException {
    List<String> names = new ArrayList<>();
    try (DirectoryStream<Path> entries = Files.newDirectoryStream(componentsDir)) {
      for (Path entry : entries) {
        if (!Files.isDirectory(entry)) {
          continue;
        }
        if (Files.isRegularFile(entry.resolve("fixtures.json"))) {
          names.add(entry.getFileName().toString());
        }
      }
    }
    Collections.sort(names);
    return names;
  }

  /** Reads one component's {@code fixtures.json} from the installed GOV.UK Frontend package. */
  public static FixtureSet loadFixtures(Path componentsDir, String component) throws IOException {
    Path path = componentsDir.resolve(component).resolve("fixtures.json");
    String raw = Files.readString(path);
    Object decoded;
    try {
      decoded = Params.parseJson(raw);
    } catch (IOException e) {
      throw new IOException("govuk: parsing " + path + ": " + e.getMessage(), e);
    }
    if (!(decoded instanceof Params root)) {
      throw new IOException("govuk: parsing " + path + ": expected a JSON object");
    }

    String componentName = Nunjucks.str(root.get("component"));
    List<Fixture> fixtures = new ArrayList<>();
    for (Object entry : Nunjucks.items(root.get("fixtures"))) {
      if (!(entry instanceof Params f)) {
        continue;
      }
      Object optionsValue = f.get("options");
      Params options =
          optionsValue instanceof Params p
              ? p
              : optionsValue instanceof Undefined || optionsValue == null
                  ? new Params()
                  : new Params();
      boolean hidden = Boolean.TRUE.equals(f.get("hidden"));
      fixtures.add(
          new Fixture(
              Nunjucks.str(f.get("name")),
              options,
              hidden,
              Nunjucks.str(f.get("description")),
              Nunjucks.str(f.get("html"))));
    }
    return new FixtureSet(componentName, List.copyOf(fixtures));
  }

  /** Unchecked wrapper for callers that prefer runtime exceptions. */
  public static FixtureSet loadFixturesUnchecked(Path componentsDir, String component) {
    try {
      return loadFixtures(componentsDir, component);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
