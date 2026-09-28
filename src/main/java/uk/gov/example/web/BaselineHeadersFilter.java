package uk.gov.example.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.example.baseline.HeaderOptions;
import uk.gov.example.baseline.Policy;
import uk.gov.example.baseline.ResponseHeaders;
import uk.gov.example.baseline.ResponseKind;

/**
 * Skeleton filter that applies baseline document headers to every response.
 *
 * <p>Wire a {@link Policy} bean and register this filter when the application is ready. Controllers
 * that serve assets or downloads should override the kind later (or skip this filter) once routing
 * is in place.
 */
public class BaselineHeadersFilter extends OncePerRequestFilter {

  private final Policy policy;

  public BaselineHeadersFilter(Policy policy) {
    this.policy = Objects.requireNonNull(policy, "policy");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    boolean secureTransport = request.isSecure();
    ResponseHeaders.applyHeaders(
        policy,
        response,
        new HeaderOptions(ResponseKind.DOCUMENT, secureTransport, false));
    filterChain.doFilter(request, response);
  }
}
