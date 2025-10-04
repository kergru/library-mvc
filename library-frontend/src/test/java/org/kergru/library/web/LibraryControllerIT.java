package org.kergru.library.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kergru.library.client.LibraryBackendClient;
import org.kergru.library.web.mock.BackendMockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@Import(LibraryBackendClient.class)
public class LibraryControllerIT {

  private OAuth2User mockUser = new DefaultOAuth2User(
      List.of(new SimpleGrantedAuthority("ROLE_USER")),
      Map.of("preferred_username", "demo_user_1"),
      "preferred_username"
  );

  @Autowired
  private MockMvc mockMvc;

  @BeforeAll
  static void setup() throws Exception {
    BackendMockConfig.start();
  }

  @AfterAll
  static void teardown() {
    BackendMockConfig.stop();
  }

  @Test
  void testShowBooks() throws Exception {
    mockMvc.perform(get("/library/ui/books")
            .with(oauth2Login().oauth2User(mockUser)))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("The Great Gatsby")));
  }

  @Test
  void testShowBookByIsbn() throws Exception {
    mockMvc.perform(get("/library/ui/books/12345")
            .with(oauth2Login().oauth2User(mockUser)))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("The Great Gatsby")));
  }

  @Test
  void testMeEndpoint() throws Exception {
    mockMvc.perform(get("/library/ui/me")
            .with(oauth2Login().oauth2User(mockUser)))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("demo_user_1")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("The Great Gatsby")));
  }
}
