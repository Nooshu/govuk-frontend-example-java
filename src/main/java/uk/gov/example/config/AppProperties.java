package uk.gov.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    boolean demosEnabled,
    String componentsRoot,
    String assetsRoot,
    String frontendJs,
    String stylesheet,
    String policyPath,
    String frontendVersion) {

  public AppProperties {
    if (frontendVersion == null || frontendVersion.isBlank()) {
      frontendVersion = "6.5.1";
    }
  }
}
