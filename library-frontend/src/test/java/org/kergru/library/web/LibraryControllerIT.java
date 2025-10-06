package org.kergru.library.web;

import static org.kergru.library.util.JwtTestUtils.mockOidcUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.kergru.library.util.KeycloakTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for the {@link LibraryController}. It uses MockMvc KeyCloak login is mocked, but Keycloak container is required too Library Backend is mocked using WireMock
 * Webclient is configured to use a mock JWT
 */
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8081)
@Import(KeycloakTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LibraryControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void expectListBooksReturnsBook() throws Exception {
    mockMvc.perform(get("/library/ui/books")
            .with(oauth2Login().oauth2User(mockOidcUser("demo_user_1"))))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("The Great Gatsby")));
  }

  @Test
  void testShowBookByIsbn() throws Exception {
    mockMvc.perform(get("/library/ui/books/12345")
            .with(oauth2Login().oauth2User(mockOidcUser("demo_user_1"))))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("The Great Gatsby")));
  }

  @Test
  void testMeEndpoint() throws Exception {
    mockMvc.perform(get("/library/ui/me")
            .with(oauth2Login().oauth2User(mockOidcUser("demo_user_1"))))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("demo_user_1")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("The Great Gatsby")));
  }
}
