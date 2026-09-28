package uk.gov.example.web;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/** Fingerprinted stylesheet / Frontend JS / initAll module, plus Frontend static assets. */
public final class AssetRegistry {

  public record Asset(byte[] body, String contentType, boolean fingerprinted) {}

  private final String stylesheetHref;
  private final String appModuleHref;
  private final Path assetsRoot;
  private final Map<String, Asset> fingerprinted;

  public AssetRegistry(
      String stylesheetHref,
      String appModuleHref,
      Path assetsRoot,
      Map<String, Asset> fingerprinted) {
    this.stylesheetHref = stylesheetHref;
    this.appModuleHref = appModuleHref;
    this.assetsRoot = assetsRoot;
    this.fingerprinted = Map.copyOf(fingerprinted);
  }

  public String stylesheetHref() {
    return stylesheetHref;
  }

  public String appModuleHref() {
    return appModuleHref;
  }

  public Optional<Asset> resolve(String path) {
    Asset fingerprintedAsset = fingerprinted.get(path);
    if (fingerprintedAsset != null) {
      return Optional.of(fingerprintedAsset);
    }
    if (!path.startsWith("/assets/")) {
      return Optional.empty();
    }
    String relative = path.substring("/assets/".length());
    if (relative.isEmpty() || relative.startsWith("/")) {
      return Optional.empty();
    }
    Path target = assetsRoot.resolve(relative).normalize();
    if (!target.startsWith(assetsRoot) || !Files.isRegularFile(target)) {
      return Optional.empty();
    }
    try {
      byte[] body = Files.readAllBytes(target);
      return Optional.of(new Asset(body, contentTypeFor(relative), false));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private static String contentTypeFor(String relative) {
    String lower = relative.toLowerCase();
    if (lower.endsWith(".css")) {
      return "text/css; charset=utf-8";
    }
    if (lower.endsWith(".js") || lower.endsWith(".mjs")) {
      return "text/javascript; charset=utf-8";
    }
    if (lower.endsWith(".woff2")) {
      return "font/woff2";
    }
    if (lower.endsWith(".woff")) {
      return "font/woff";
    }
    if (lower.endsWith(".svg")) {
      return "image/svg+xml";
    }
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".ico")) {
      return "image/x-icon";
    }
    if (lower.endsWith(".json")) {
      return "application/json; charset=utf-8";
    }
    return "application/octet-stream";
  }
}
