package uk.gov.example.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.example.baseline.Policy;

class BaselineHeadersFilterTest {

  @Test
  void appliesDocumentHeadersByDefault() throws Exception {
    Policy policy =
        Policy.load(Path.of(System.getProperty("user.dir"), "baseline", "policy.json"));
    BaselineHeadersFilter filter = new BaselineHeadersFilter(policy);

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.isSecure()).thenReturn(true);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    assertThat(response.getHeader("Content-Security-Policy"))
        .contains(policy.jsEnabledScriptHash());
    assertThat(response.getHeader("Cache-Control")).isEqualTo("no-cache");
    assertThat(response.getHeader("Strict-Transport-Security")).isNotBlank();
    verify(chain).doFilter(any(), any());
  }

  @Test
  void omitsHstsOnInsecureRequest() throws Exception {
    Policy policy =
        Policy.load(Path.of(System.getProperty("user.dir"), "baseline", "policy.json"));
    BaselineHeadersFilter filter = new BaselineHeadersFilter(policy);

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.isSecure()).thenReturn(false);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    assertThat(response.getHeader("Strict-Transport-Security")).isNull();
    verify(chain).doFilter(any(), any());
  }
}
