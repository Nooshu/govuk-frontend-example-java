package uk.gov.example.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import uk.gov.example.baseline.Policy;
import uk.gov.example.session.InMemorySessionStore;
import uk.gov.example.web.AssetRegistry;
import uk.gov.example.web.BaselineHeadersFilter;
import uk.gov.example.web.SessionFilter;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {

  private final AppProperties properties;

  public AppConfig(AppProperties properties) {
    this.properties = properties;
  }

  @Bean
  public Policy policy() throws IOException {
    return Policy.load(Path.of(properties.policyPath()).toAbsolutePath().normalize());
  }

  @Bean
  public InMemorySessionStore sessionStore() {
    return new InMemorySessionStore();
  }

  @Bean
  public AssetRegistry assetRegistry() throws IOException {
    Path stylesheet = Path.of(properties.stylesheet()).toAbsolutePath().normalize();
    Path frontendJs = Path.of(properties.frontendJs()).toAbsolutePath().normalize();
    Path assetsRoot = Path.of(properties.assetsRoot()).toAbsolutePath().normalize();
    if (!Files.isRegularFile(stylesheet)) {
      throw new IOException(
          "Missing " + stylesheet + " — run `npm run build:styles` first");
    }
    byte[] css = Files.readAllBytes(stylesheet);
    byte[] script = Files.readAllBytes(frontendJs);
    String cssHref = "/assets/application." + fingerprint(css) + ".css";
    String scriptHref = "/assets/govuk-frontend." + fingerprint(script) + ".min.js";
    byte[] appModule =
        ("import { initAll } from '" + scriptHref + "';\n\ninitAll();\n")
            .getBytes(StandardCharsets.UTF_8);
    String appHref = "/assets/app." + fingerprint(appModule) + ".mjs";
    Map<String, AssetRegistry.Asset> fingerprinted = new LinkedHashMap<>();
    fingerprinted.put(cssHref, new AssetRegistry.Asset(css, "text/css; charset=utf-8", true));
    fingerprinted.put(
        scriptHref, new AssetRegistry.Asset(script, "text/javascript; charset=utf-8", true));
    fingerprinted.put(
        appHref, new AssetRegistry.Asset(appModule, "text/javascript; charset=utf-8", true));
    return new AssetRegistry(cssHref, appHref, assetsRoot, fingerprinted);
  }

  @Bean
  public FilterRegistrationBean<BaselineHeadersFilter> baselineHeadersFilter(Policy policy) {
    FilterRegistrationBean<BaselineHeadersFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new BaselineHeadersFilter(policy));
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<SessionFilter> sessionFilter(
      InMemorySessionStore store, Policy policy) {
    FilterRegistrationBean<SessionFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new SessionFilter(store, policy));
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
    return registration;
  }

  static String fingerprint(byte[] bytes) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
      return HexFormat.of().formatHex(digest).substring(0, 16);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
