package org.kergru.library.client.keycloak;

import java.util.List;
import java.util.Map;
import org.kergru.library.model.UserDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Client for keycloak admin API.
 */
@Component
public class KeycloakAdminClient {

  private final RestClient keycloakAdminRestClient;
  private final KeycloakTokenProvider tokenProvider;

  public KeycloakAdminClient(RestClient keycloakAdminRestClient, KeycloakTokenProvider tokenProvider) {
    this.keycloakAdminRestClient = keycloakAdminRestClient;
    this.tokenProvider = tokenProvider;
  }

  /**
   * Creates a new user in keycloak in realm "library".
   */
  public void createUser(UserDto user, String password) {
    var userBody = Map.of(
        "username", user.userName(),
        "firstName", user.firstName(),
        "lastName", user.lastName(),
        "email", user.email(),
        "enabled", true,
        "emailVerified", true,
        "credentials", List.of(
            Map.of(
                "type", "password",
                "value", password,
                "temporary", false
            )
        )
    );

    keycloakAdminRestClient.post()
        .uri("/admin/realms/library/users")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getToken())
        .contentType(MediaType.APPLICATION_JSON)
        .body(userBody)
        .retrieve()
        .toBodilessEntity();
  }
}
