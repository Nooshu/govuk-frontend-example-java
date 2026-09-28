package uk.gov.example.session;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InMemorySessionStoreTest {

  @Test
  void createGetPutRemove() {
    InMemorySessionStore store = new InMemorySessionStore();
    String id = store.create();
    assertThat(id).isNotBlank();
    assertThat(store.get(id)).isPresent();
    assertThat(store.get(id).orElseThrow().application()).isNotNull();
    assertThat(store.get(id).orElseThrow().csrfToken()).isNotBlank();

    SessionData data = new SessionData();
    data.application().setFirstName("Jane");
    store.put(id, data);
    assertThat(store.get(id).orElseThrow().application().firstName()).isEqualTo("Jane");

    store.remove(id);
    assertThat(store.get(id)).isEmpty();
  }

  @Test
  void sessionDataDefaults() {
    SessionData empty = new SessionData();
    assertThat(empty.application().firstName()).isEmpty();
    assertThat(empty.cookieAnalytics()).isNull();
    assertThat(empty.cookieBannerDismissed()).isFalse();
    empty.rotateCsrf();
    assertThat(empty.csrfToken()).isNotBlank();
  }
}
