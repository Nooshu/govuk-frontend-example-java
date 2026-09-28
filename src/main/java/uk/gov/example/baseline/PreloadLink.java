package uk.gov.example.baseline;

/**
 * One same-origin resource to announce in the {@code Link} header.
 *
 * @param href a same-origin path, such as {@code /assets/fonts/light-94a07e06a1-v2.woff2}
 * @param as the destination: {@code font}, {@code style}, {@code script}, {@code image}, or {@code
 *     fetch}
 * @param type the MIME type when it is worth stating; may be {@code null}
 */
public record PreloadLink(String href, String as, String type) {
  public PreloadLink {
    if (href == null) {
      throw new NullPointerException("preload href must not be null");
    }
    if (as == null) {
      throw new NullPointerException("preload as must not be null");
    }
  }

  public PreloadLink(String href, String as) {
    this(href, as, null);
  }
}
