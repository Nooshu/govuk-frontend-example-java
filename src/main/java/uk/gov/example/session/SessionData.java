package uk.gov.example.session;

import java.util.List;
import java.util.UUID;
import uk.gov.example.service.LicenceApplication;
import uk.gov.example.service.Validation;

/** In-memory session for one applicant journey. */
public final class SessionData {

  private final LicenceApplication application;
  private String csrfToken;
  private String cookieAnalytics; // "yes", "no", or null if unset
  private boolean cookieBannerDismissed;
  /** Transient banner confirmation after accept/reject: "accept", "reject", or null. */
  private String cookieBannerConfirm;

  private String flashErrorsPath;
  private List<Validation.FieldError> flashErrors;
  private String noticePath;
  private String noticeText;

  public SessionData() {
    this.application = new LicenceApplication();
    this.csrfToken = UUID.randomUUID().toString();
  }

  public LicenceApplication application() {
    return application;
  }

  public String csrfToken() {
    return csrfToken;
  }

  public void rotateCsrf() {
    this.csrfToken = UUID.randomUUID().toString();
  }

  public String cookieAnalytics() {
    return cookieAnalytics;
  }

  public void setCookieAnalytics(String cookieAnalytics) {
    this.cookieAnalytics = cookieAnalytics;
  }

  public boolean cookieBannerDismissed() {
    return cookieBannerDismissed;
  }

  public void setCookieBannerDismissed(boolean cookieBannerDismissed) {
    this.cookieBannerDismissed = cookieBannerDismissed;
  }

  public String cookieBannerConfirm() {
    return cookieBannerConfirm;
  }

  public void setCookieBannerConfirm(String cookieBannerConfirm) {
    this.cookieBannerConfirm = cookieBannerConfirm;
  }

  public void setFlashErrors(String path, List<Validation.FieldError> errors) {
    this.flashErrorsPath = path;
    this.flashErrors = errors;
  }

  public void clearFlashErrors() {
    this.flashErrorsPath = null;
    this.flashErrors = null;
  }

  /** Returns and clears flash errors when they match {@code path}. */
  public List<Validation.FieldError> takeFlashErrors(String path) {
    if (flashErrorsPath != null && flashErrorsPath.equals(path) && flashErrors != null) {
      List<Validation.FieldError> items = flashErrors;
      clearFlashErrors();
      return items;
    }
    return List.of();
  }

  public void setNotice(String path, String text) {
    this.noticePath = path;
    this.noticeText = text;
  }

  public void clearNotice() {
    this.noticePath = null;
    this.noticeText = null;
  }

  /** Returns and clears the notice when it matches {@code path}. */
  public String takeNotice(String path) {
    if (noticePath != null && noticePath.equals(path) && noticeText != null) {
      String text = noticeText;
      clearNotice();
      return text;
    }
    return null;
  }

  /** Builds the confirmation reference shown to the applicant from the session id. */
  public static String referenceFor(String sessionId) {
    if (sessionId == null || sessionId.isEmpty()) {
      return "RL";
    }
    String prefix = sessionId.length() > 6 ? sessionId.substring(0, 6) : sessionId;
    return "RL" + prefix.toUpperCase();
  }
}
