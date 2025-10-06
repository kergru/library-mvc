package org.kergru.library.web;

import static org.hamcrest.Matchers.containsString;
import static org.kergru.library.util.JwtTestUtils.mockOidcUser;
import static org.kergru.library.util.JwtTestUtils.mockOidcUserLibrarian;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
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
 * Integration test for the {@link LibraryAdminController}. KeyCloak is mocked using mockJwt(), no KeyCloak container required Library Backend is mocked using WireMock Webclient is
 * configured to use a mock JWT
 */
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8081)
@Import(KeycloakTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LibraryAdminControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void expectListAllUsersWithRoleLibrarianShouldReturnUsers() throws Exception {

    mockMvc.perform(get("/library/ui/admin/users")
            .with(oauth2Login().oauth2User(mockOidcUserLibrarian("librarian"))))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("demo_user_1")))
        .andExpect(content().string(containsString("demo_user_2")))
        .andExpect(content().string(containsString("demo_user_3")));
  }

  @Test
  void expectListAllUsersWithNotRoleLibrarianShouldReturnForbidden() throws Exception {

    mockMvc.perform(get("/library/ui/admin/users")
            .with(oauth2Login().oauth2User(mockOidcUser("demo_user_1"))))
        .andExpect(status().isForbidden())
        .andExpect(forwardedUrl("/error/403"));
  }

  @Test
  void expectGetUserWithRoleLibrarianShouldReturnUser() throws Exception {

    mockMvc.perform(get("/library/ui/admin/users/demo_user_1")
            .with(oauth2Login().oauth2User(mockOidcUserLibrarian("librarian"))))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("demo_user_1")));
  }

  @Test
  void expectGetUserWithNotRoleLibrarianShouldReturnForbidden() throws Exception {

    mockMvc.perform(get("/library/ui/admin/users/demo_user_1") //route is protected by role Librarian
            .with(oauth2Login().oauth2User(mockOidcUser("demo_user_1"))))
        .andExpect(status().isForbidden())
        .andExpect(forwardedUrl("/error/403"));
  }
}
