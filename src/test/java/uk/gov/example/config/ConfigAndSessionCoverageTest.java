package uk.gov.example.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.example.session.SessionData;

class ConfigAndSessionCoverageTest {

  @Test
  void appPropertiesDefaultFrontendVersion() {
    AppProperties blank =
        new AppProperties(false, "a", "b", "c", "d", "e", " ");
    assertThat(blank.frontendVersion()).isEqualTo("6.5.1");
    AppProperties nil =
        new AppProperties(false, "a", "b", "c", "d", "e", null);
    assertThat(nil.frontendVersion()).isEqualTo("6.5.1");
    AppProperties set =
        new AppProperties(true, "a", "b", "c", "d", "e", "9.9.9");
    assertThat(set.frontendVersion()).isEqualTo("9.9.9");
  }

  @Test
  void fingerprintMissingAlgorithm() {
    try (MockedStatic<MessageDigest> md = mockStatic(MessageDigest.class)) {
      md.when(() -> MessageDigest.getInstance("SHA-256"))
          .thenThrow(new NoSuchAlgorithmException("gone"));
      assertThatThrownBy(() -> AppConfig.fingerprint("x".getBytes(StandardCharsets.UTF_8)))
          .isInstanceOf(IllegalStateException.class);
    }
    assertThat(AppConfig.fingerprint("ok".getBytes(StandardCharsets.UTF_8))).hasSize(16);
  }

  @Test
  void sessionDataNullFlashAndReference() throws Exception {
    SessionData session = new SessionData();
    session.setFlashErrors("/name", null);
    assertThat(session.takeFlashErrors("/name")).isEmpty();
    session.setNotice("/cookies", null);
    assertThat(session.takeNotice("/cookies")).isNull();
    assertThat(SessionData.referenceFor(null)).isEqualTo("RL");
  }
}
