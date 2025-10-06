package org.kergru.library;

import static org.hamcrest.Matchers.containsString;
import static org.kergru.library.JwtTestUtils.jwtWithRoles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * General test class for the library backend application, validates the context loading KeyCloak and MySQL(testcontainers database url) are started as testcontainers
 */
@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryBackendApplicationTests {

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
        () -> "http://localhost:8085/realms/library/protocol/openid-connect/certs");
  }

  @Autowired
  private MockMvc mockMvc;

  @Test
  void contextLoads() {
  }

  @Test
  @WithMockUser
  public void expectLoadUserWithRoleLibrarianShouldReturnUser() throws Exception {
    var jwt = jwtWithRoles("librarian", "LIBRARIAN");

    mockMvc.perform(get("/library/api/users/demo_user_1")
            .with(jwt))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("demo_user_1")));
  }

  @Test
  @WithMockUser
  public void expectLoadUnknownUserWithRoleLibrarianShouldReturnNotFound() throws Exception {
    var jwt = jwtWithRoles("librarian", "LIBRARIAN");

    mockMvc.perform(get("/library/api/users/UNKNOWN")
            .with(jwt))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  public void expectLoadMyUserWithNotRoleLibrarianShouldReturnUser() throws Exception {
    var jwt = jwtWithRoles("demo_user_1");

    mockMvc.perform(get("/library/api/users/demo_user_1")
            .with(jwt))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("demo_user_1")));
  }

  @Test
  @WithMockUser
  public void expectLoadForeignUserWithNotRoleLibrarianShouldReturnForbidden() throws Exception {
    var jwt = jwtWithRoles("demo_user_1");

    mockMvc.perform(get("/library/api/users/demo_user_2")
            .with(jwt))
        .andExpect(status().isForbidden());
  }
}
