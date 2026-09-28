package uk.gov.example.web;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.example.session.SessionData;

final class WebSessions {

  private WebSessions() {}

  static SessionData require(HttpServletRequest request) {
    Object value = request.getAttribute(SessionFilter.ATTR_SESSION);
    if (!(value instanceof SessionData session)) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No session");
    }
    return session;
  }

  static String id(HttpServletRequest request) {
    Object value = request.getAttribute(SessionFilter.ATTR_SESSION_ID);
    return value == null ? "" : String.valueOf(value);
  }

  static boolean csrfOk(SessionData session, String token) {
    if (token == null || token.isEmpty() || session.csrfToken() == null || session.csrfToken().isEmpty()) {
      return false;
    }
    return MessageDigest.isEqual(
        token.getBytes(StandardCharsets.UTF_8),
        session.csrfToken().getBytes(StandardCharsets.UTF_8));
  }

  static String safeReturn(String value) {
    if (value == null
        || !value.startsWith("/")
        || value.startsWith("//")
        || value.contains("://")
        || value.contains("\\")
        || value.contains("\r")
        || value.contains("\n")) {
      return "/";
    }
    return value;
  }
}
