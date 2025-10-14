package org.kergru.library.client.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class KeycloakTokenProvider {

  private final RestClient keycloakAdminRestClient;
  private final String clientId;
  private final String username;
  private final String password;
  private final String realm;

  private String cachedToken;
  private long expiryTime;

  public KeycloakTokenProvider(
      RestClient keycloakAdminRestClient,
      @Value("${keycloak.admin.client-id}") String clientId,
      @Value("${keycloak.admin.username}") String username,
      @Value("${keycloak.admin.password}") String password,
      @Value("${keycloak.admin.realm}") String realm) {

    this.keycloakAdminRestClient = keycloakAdminRestClient;
    this.clientId = clientId;
    this.username = username;
    this.password = password;
    this.realm = realm;
  }

  @SuppressWarnings("unchecked")
  public String getToken() {
    if (cachedToken == null || System.currentTimeMillis() > expiryTime) {
      Map<String, Object> response = keycloakAdminRestClient.post()
          .uri("/realms/{realm}/protocol/openid-connect/token", realm)
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body("grant_type=password&client_id=" + clientId +
              "&username=" + username +
              "&password=" + password)
          .retrieve()
          .body(Map.class);

      cachedToken = (String) response.get("access_token");
      int expiresIn = ((Number) response.get("expires_in")).intValue();
      expiryTime = System.currentTimeMillis() + (expiresIn - 30) * 1000L;
    }
    return cachedToken;
  }
}
