package uk.gov.example.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.demos-enabled=true")
class SupportPagesTest {

  private static final Pattern CSRF = Pattern.compile("name=\"csrf\"\\s+value=\"([^\"]+)\"");

  @Autowired private MockMvc mockMvc;

  @Test
  void supportPagesAndCookies() throws Exception {
    MvcResult start = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();
    Cookie session = start.getResponse().getCookie(SessionFilter.COOKIE_NAME);

    mockMvc.perform(get("/cy").cookie(session)).andExpect(status().isOk());
    mockMvc
        .perform(get("/fees").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Licence fees")));
    mockMvc.perform(get("/help").cookie(session)).andExpect(status().isOk());
    mockMvc.perform(get("/guidance").cookie(session)).andExpect(status().isOk());
    mockMvc.perform(get("/accessibility").cookie(session)).andExpect(status().isOk());
    mockMvc.perform(get("/about").cookie(session)).andExpect(status().isOk());
    mockMvc.perform(get("/updates").cookie(session)).andExpect(status().isOk());
    mockMvc.perform(get("/updates").param("page", "2").cookie(session)).andExpect(status().isOk());
    mockMvc
        .perform(get("/updates").param("page", "9").cookie(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/updates"));

    String csrf =
        csrf(mockMvc.perform(get("/cookies").cookie(session)).andExpect(status().isOk()).andReturn());
    mockMvc
        .perform(
            post("/cookies")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("analytics", "yes"))
        .andExpect(redirectedUrl("/cookies"));
    mockMvc
        .perform(get("/cookies").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("saved")));

    csrf =
        csrf(
            mockMvc
                .perform(get("/cookies").cookie(session))
                .andExpect(status().isOk())
                .andReturn());
    mockMvc
        .perform(
            post("/cookie-choices")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("cookies", "accept")
                .param("returnPath", "/fees"))
        .andExpect(redirectedUrl("/fees"));
    mockMvc
        .perform(
            post("/cookie-choices")
                .cookie(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("csrf", csrf)
                .param("cookies", "hide")
                .param("returnPath", "/help"))
        .andExpect(redirectedUrl("/help"));

    mockMvc.perform(get("/new-application").cookie(session)).andExpect(redirectedUrl("/"));
  }

  private static String csrf(MvcResult result) throws Exception {
    Matcher matcher = CSRF.matcher(result.getResponse().getContentAsString());
    matcher.find();
    return matcher.group(1);
  }
}
