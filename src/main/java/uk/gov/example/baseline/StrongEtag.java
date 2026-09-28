package uk.gov.example.baseline;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/** Strong ETag helper: SHA-256 of the body, Base64URL-encoded, quoted. */
public final class StrongEtag {

  private StrongEtag() {}

  /** Returns a strong validator for a response body. */
  public static String of(byte[] body) {
    if (body == null) {
      throw new NullPointerException("body must not be null");
    }
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(body);
      return "\"" + Base64.getUrlEncoder().withoutPadding().encodeToString(digest) + "\"";
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  /** Returns a strong validator for a UTF-8 string body. */
  public static String of(String body) {
    if (body == null) {
      throw new NullPointerException("body must not be null");
    }
    return of(body.getBytes(StandardCharsets.UTF_8));
  }
}
