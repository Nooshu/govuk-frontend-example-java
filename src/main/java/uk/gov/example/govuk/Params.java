package uk.gov.example.govuk;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ordered set of component options — the Java equivalent of the object literal a Nunjucks macro
 * receives. Order is preserved because GOV.UK Frontend renders {@code attributes} by iterating
 * keys.
 *
 * <p>A {@code null} Params behaves as an empty object: reading any key returns {@link Undefined}.
 */
public final class Params {

  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  private final List<String> keys = new ArrayList<>();
  private final Map<String, Object> values = new HashMap<>();

  public Params() {}

  /** Builds a parameter object from alternating key and value arguments. */
  public static Params of(Object... pairs) {
    if (pairs.length % 2 != 0) {
      throw new IllegalArgumentException("Params.of needs an even number of arguments");
    }
    Params p = new Params();
    for (int i = 0; i < pairs.length; i += 2) {
      if (!(pairs[i] instanceof String key)) {
        throw new IllegalArgumentException("Params.of key " + i + " is not a string");
      }
      p.set(key, pairs[i + 1]);
    }
    return p;
  }

  /** Stores a value, appending the key the first time it is seen so insertion order is kept. */
  public void set(String key, Object value) {
    if (!values.containsKey(key)) {
      keys.add(key);
    }
    values.put(key, value);
  }

  /**
   * Returns the value stored under key, or {@link Undefined} when absent. Explicit JSON null is
   * returned as {@code null}, which is distinct from {@link Undefined}.
   */
  public Object get(String key) {
    if (!values.containsKey(key)) {
      return Undefined.INSTANCE;
    }
    return values.get(key);
  }

  /** Reports whether key is present, including when its value is null. */
  public boolean has(String key) {
    return values.containsKey(key);
  }

  /** Returns the keys in insertion order. */
  public List<String> keys() {
    return Collections.unmodifiableList(keys);
  }

  /** Number of keys — what Nunjucks' {@code length} filter reports for an object. */
  public int size() {
    return keys.size();
  }

  /** Decodes a JSON object while preserving key order and number spelling. */
  public void decodeJson(String json) {
    try {
      Object value = parseJson(json);
      if (!(value instanceof Params decoded)) {
        throw new IllegalArgumentException("govuk: expected a JSON object, got " + typeName(value));
      }
      keys.clear();
      values.clear();
      keys.addAll(decoded.keys);
      values.putAll(decoded.values);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Decodes JSON into the value model this package uses: {@link Params} for objects, {@link List}
   * for arrays, {@link String}, {@link Boolean}, {@link JsonNumber}, and {@code null} for null.
   */
  public static Object parseJson(String json) throws IOException {
    try (JsonParser parser = JSON_FACTORY.createParser(json)) {
      JsonToken first = parser.nextToken();
      if (first == null) {
        throw new IOException("govuk: empty JSON");
      }
      Object value = parseValue(parser);
      if (parser.nextToken() != null) {
        throw new IOException("govuk: unexpected data after JSON value");
      }
      return value;
    }
  }

  static Object parseValue(JsonParser parser) throws IOException {
    JsonToken token = parser.currentToken();
    if (token == null) {
      token = parser.nextToken();
    }
    if (token == null) {
      throw new IOException("govuk: unexpected end of JSON");
    }
    return switch (token) {
      case START_OBJECT -> {
        Params object = new Params();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
          String name = parser.currentName();
          parser.nextToken();
          object.set(name, parseValue(parser));
        }
        yield object;
      }
      case START_ARRAY -> {
        List<Object> items = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
          items.add(parseValue(parser));
        }
        yield items;
      }
      case VALUE_STRING -> parser.getText();
      case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> new JsonNumber(parser.getText());
      case VALUE_TRUE -> Boolean.TRUE;
      case VALUE_FALSE -> Boolean.FALSE;
      case VALUE_NULL -> null;
      default -> throw new IOException("govuk: unexpected JSON token " + token);
    };
  }

  /** Null-safe get: a null Params behaves like an empty object. */
  public static Object get(Params params, String key) {
    if (params == null) {
      return Undefined.INSTANCE;
    }
    return params.get(key);
  }

  private static String typeName(Object value) {
    return value == null ? "null" : value.getClass().getName();
  }
}
