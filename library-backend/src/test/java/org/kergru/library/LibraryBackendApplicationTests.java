package org.kergru.library;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.nimbusds.jwt.SignedJWT;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kergru.library.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * General test class for the library backend application, validates the context loading
 * KeyCloak and MySQL(testcontainers database url) are started as testcontainers
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryBackendApplicationTests {

  static KeycloakContainer keycloak
      = new KeycloakContainer("quay.io/keycloak/keycloak:26.3.1")
      .withCopyFileToContainer(
          MountableFile.forHostPath("../docker/keycloak-init/library-realm.json"),
          "/opt/keycloak/data/import/library-realm.json"
      )
      .withEnv("KEYCLOAK_ADMIN", "admin")
      .withEnv("KEYCLOAK_ADMIN_PASSWORD", "pwd")
      .withEnv("KC_HTTP_PORT", "8080")
      .withEnv("KC_IMPORT", "/opt/keycloak/data/import/library-realm.json")
      .withExposedPorts(8080)
      .waitingFor(
          Wait.forHttp("/realms/library/.well-known/openid-configuration")
              .forStatusCode(200)
              .withStartupTimeout(Duration.ofMinutes(2))
      )
      .withCreateContainerCmdModifier(cmd ->
          cmd.getHostConfig().withPortBindings(
              new PortBinding(Ports.Binding.bindPort(8085), new ExposedPort(8080))
          )
      );

  @DynamicPropertySource
  static void jwtProps(DynamicPropertyRegistry registry) {
    waitForJwks(); // <— wichtig
    String base = keycloak.getAuthServerUrl().replaceAll("/$", "");
    registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
        () -> base + "/realms/library/protocol/openid-connect/certs");
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeAll
  static void setup() {
    keycloak.start();
  }

  @AfterAll
  static void teardown() {
    keycloak.stop();
  }

  @Test
  void contextLoads() {
  }

  @Test
  public void expectLoadUserWithRoleLibrarianShouldReturnUser() {
    String token = getAccessToken("librarian", "pwd");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<UserDto> response = restTemplate.exchange(
        "/library/api/users/demo_user_1", HttpMethod.GET, request, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().userName()).isEqualTo("demo_user_1");
  }

  @Test
  public void expectLoadUnknownUserWithRoleLibrarianShouldReturnNotFound() {
    String token = getAccessToken("librarian", "pwd");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<UserDto> response = restTemplate.exchange(
        "/library/api/users/UNKNOWN_USER", HttpMethod.GET, request, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void expectLoadMyUserWithNotRoleLibrarianShouldReturnUser() {
    String token = getAccessToken("demo_user_1", "pwd");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<UserDto> response = restTemplate.exchange(
        "/library/api/users/demo_user_1", HttpMethod.GET, request, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().userName()).isEqualTo("demo_user_1");
  }

  @Test
  public void expectLoadForeignUserWithNotRoleLibrarianShouldReturnForbidden() {
    String token = getAccessToken("demo_user_1", "pwd");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<UserDto> response = restTemplate.exchange(
        "/library/api/users/bob", HttpMethod.GET, request, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private String getAccessToken(String username, String password) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("client_id", "library-frontend");
    form.add("client_secret", "secret");
    form.add("grant_type", "password");
    form.add("username", username);
    form.add("password", password);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
        keycloak.getAuthServerUrl() + "/realms/library/protocol/openid-connect/token",
        new HttpEntity<>(form, headers),
        Map.class
    );

    assertThat(tokenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    String token = (String) tokenResponse.getBody().get("access_token");
    System.out.println("Token: " + token);
    try {
      SignedJWT jwt = SignedJWT.parse(token);
      System.out.println(jwt.getJWTClaimsSet().toJSONObject());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return token;
  }

  private static void waitForJwks() {
    RestTemplate rt = new RestTemplate();
    String jwks = "http://localhost:8085/realms/library/protocol/openid-connect/certs";
    for (int i = 0; i < 30; i++) {
      try {
        var r = rt.getForEntity(jwks, String.class);
        if (r.getStatusCode().is2xxSuccessful()) return;
      } catch (Exception ignored) {}
      try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }
    throw new IllegalStateException("JWKS not available after 30s: " + jwks);
  }
}
