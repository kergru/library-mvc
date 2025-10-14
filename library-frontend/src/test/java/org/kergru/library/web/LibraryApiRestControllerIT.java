package org.kergru.library.web;

import static org.kergru.library.util.JwtTestUtils.mockOidcUser;
import static org.kergru.library.util.JwtTestUtils.mockOidcUserLibrarian;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.kergru.library.MockOAuth2Config;
import org.kergru.library.client.keycloak.KeycloakAdminClient;
import org.kergru.library.model.UserDto;
import org.kergru.library.web.LibraryApiRestController.CreateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration test for the {@link LibraryApiRestController}.
 * - it uses MockMvc,
 * - KeyCloak is mocked using mockJwt(), no KeyCloak container required
 * - Library Backend is mocked using WireMock.
 * - Webclient is configured to use a mock JWT
 */
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@Import(MockOAuth2Config.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryApiRestControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private KeycloakAdminClient keycloakAdminClient;

  @Test
  void expectBorrowBookReturnsLoan() throws Exception {
    mockMvc.perform(post("/library/rest/me/borrowBook/success-isbn")
            .with(oauth2Login().oauth2User(mockOidcUser("demo_user_1")))
            .with(csrf()) //send csrf token
        )
        .andExpect(status().isOk());
  }

  @Test
  void expectBorrowAlreadyBorrowedBookReturnsError() throws Exception {
    mockMvc.perform(post("/library/rest/me/borrowBook/conflict-isbn")
            .with(oauth2Login().oauth2User(mockOidcUser("demo_user_1")))
            .with(csrf()) //send csrf token
        )
        .andExpect(status().isConflict());
  }

  @Test
  void expectReturnBookReturnsOk() throws Exception {
    mockMvc.perform(post("/library/rest/me/returnBook/1")
            .with(oauth2Login().oauth2User(mockOidcUser("demo_user_1")))
            .with(csrf()) //send csrf token
        )
        .andExpect(status().isOk());
  }

  @Test
  void expectCreateUserReturnsUser() throws Exception {
    CreateUserRequest createUserRequest = new CreateUserRequest(
        "unique-username",
        "John",
        "Doe",
        "john.doe@example.com",
        "password"
    );
    UserDto userDto = new UserDto(
        createUserRequest.username(),
        createUserRequest.firstName(),
        createUserRequest.lastName(),
        createUserRequest.email()
    );
    // mock keycloak admin client
    doNothing().when(keycloakAdminClient).createUser(eq(userDto), eq(createUserRequest.password()));

    mockMvc.perform(post("/library/rest/admin/users")
            .with(oauth2Login().oauth2User(mockOidcUserLibrarian("librarian")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createUserRequest))
            .with(csrf()) //send csrf token
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userName").value("unique-username"))
        .andExpect(jsonPath("$.email").value("john.doe@example.com"));

    verify(keycloakAdminClient, times(1)).createUser(eq(userDto), eq(createUserRequest.password()));
  }

  @Test
  void expectCreateUserWithAlreadyExistingUsernameReturnsConflict() throws Exception {
    CreateUserRequest createUserRequest = new CreateUserRequest(
        "duplicate-username",
        "John",
        "Doe",
        "john.doe@example.com",
        "password"
    );
    UserDto userDto = new UserDto(
        createUserRequest.username(),
        createUserRequest.firstName(),
        createUserRequest.lastName(),
        createUserRequest.email()
    );

    mockMvc.perform(post("/library/rest/admin/users")
            .with(oauth2Login().oauth2User(mockOidcUserLibrarian("librarian")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createUserRequest))
            .with(csrf()) //send csrf token
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("User already exists"))
        .andExpect(jsonPath("$.hints").value("username"));

    verify(keycloakAdminClient, never()).createUser(any(), any());
  }
}
