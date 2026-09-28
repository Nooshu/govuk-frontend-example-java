package uk.gov.example.session;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory session store keyed by cryptographically random session ids.
 *
 * <p>Suitable for local development and early scaffolding only — not for multi-instance
 * production.
 */
public final class InMemorySessionStore {

  private static final int ID_BYTES = 32;

  private final ConcurrentHashMap<String, SessionData> sessions = new ConcurrentHashMap<>();
  private final SecureRandom random = new SecureRandom();

  /** Creates a new empty session and returns its id. */
  public String create() {
    String id = newId();
    sessions.put(id, new SessionData());
    return id;
  }

  /** Returns the session for {@code id}, if present. */
  public Optional<SessionData> get(String id) {
    Objects.requireNonNull(id, "id");
    return Optional.ofNullable(sessions.get(id));
  }

  /** Replaces the session data for {@code id}. */
  public void put(String id, SessionData data) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(data, "data");
    sessions.put(id, data);
  }

  /** Removes the session for {@code id}. */
  public void remove(String id) {
    Objects.requireNonNull(id, "id");
    sessions.remove(id);
  }

  private String newId() {
    byte[] bytes = new byte[ID_BYTES];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
