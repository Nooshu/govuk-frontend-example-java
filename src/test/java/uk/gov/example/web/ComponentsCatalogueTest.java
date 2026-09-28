package uk.gov.example.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.demos-enabled=true")
class ComponentsCatalogueTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void catalogueAndComponentPreviewWhenDemosOn() throws Exception {
    mockMvc
        .perform(get("/components"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Component catalogue")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Button")));

    mockMvc
        .perform(get("/components/button"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Component preview")));

    mockMvc
        .perform(get("/components/button/fixture").param("fixture", "default"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("govuk-button")));
  }
}
