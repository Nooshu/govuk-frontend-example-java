package uk.gov.example.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.example.baseline.Policy;
import uk.gov.example.session.InMemorySessionStore;
import uk.gov.example.session.SessionData;

/**
 * Loads or creates the applicant session and sets the session cookie on every HTML response
 * except health and robots.
 */
public class SessionFilter extends OncePerRequestFilter {

  public static final String ATTR_SESSION = "appSession";
  public static final String ATTR_SESSION_ID = "appSessionId";
  public static final String COOKIE_NAME = "rod_session";
  public static final String HOST_COOKIE_NAME = "__Host-session";
  private static final int MAX_AGE = 4 * 60 * 60;

  private final InMemorySessionStore store;
  private final Policy policy;

  public SessionFilter(InMemorySessionStore store, Policy policy) {
    this.store = Objects.requireNonNull(store);
    this.policy = Objects.requireNonNull(policy);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return "/health".equals(path) || "/robots.txt".equals(path) || path.startsWith("/assets/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String id = cookieValue(request, HOST_COOKIE_NAME);
    if (id.isEmpty()) {
      id = cookieValue(request, COOKIE_NAME);
    }
    SessionData session;
    if (!id.isEmpty()) {
      session = store.get(id).orElse(null);
      if (session == null) {
        id = store.create();
        session = store.get(id).orElseThrow();
      }
    } else {
      id = store.create();
      session = store.get(id).orElseThrow();
    }
    request.setAttribute(ATTR_SESSION, session);
    request.setAttribute(ATTR_SESSION_ID, id);

    // Set the session cookie before the controller writes the body. After the response is
    // committed (chunked HTML), Tomcat ignores late Set-Cookie headers — which left browsers
    // without a session and made CSRF checks fail on cookie-banner and form POSTs.
    boolean secure = request.isSecure();
    String cookieName = secure ? HOST_COOKIE_NAME : COOKIE_NAME;
    response.addHeader(
        "Set-Cookie",
        policy.setCookie(
            cookieName,
            id,
            new Policy.CookieOptions(null, secure, true, "/", MAX_AGE, secure)));
    response.setHeader("X-Robots-Tag", "noindex, nofollow");

    filterChain.doFilter(request, response);

    store.put(id, session);
  }

  private static String cookieValue(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return "";
    }
    for (Cookie cookie : cookies) {
      if (name.equals(cookie.getName())) {
        return cookie.getValue() == null ? "" : cookie.getValue();
      }
    }
    return "";
  }
}
