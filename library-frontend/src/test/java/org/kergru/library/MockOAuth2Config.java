package org.kergru.library;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * A test configuration class that provides a mock OAuth2 client registration for OpenID Connect (OIDC) authentication.
 * This configuration is used in tests to simulate the OAuth2 authentication process without needing an actual Keycloak
 * server running. It configures a mock {@link ClientRegistration} with predefined values for a typical OIDC provider
 * (such as Keycloak) and stores it in an {@link InMemoryClientRegistrationRepository}.
 */
@TestConfiguration
public class MockOAuth2Config {

  @Bean
  public InMemoryClientRegistrationRepository clientRegistrationRepository() {
    ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("keycloak")
        .clientId("test-client-id")
        .clientSecret("test-client-secret")
        .scope("openid", "profile", "email")
        .authorizationUri("http://localhost:8085/realms/library/protocol/openid-connect/auth")
        .tokenUri("http://localhost:8085/realms/library/protocol/openid-connect/token")
        .userInfoUri("http://localhost:8085/realms/library/protocol/openid-connect/userinfo")
        .issuerUri("http://localhost:8085/realms/library")
        .clientName("Keycloak")
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .build();
    return new InMemoryClientRegistrationRepository(clientRegistration);
  }
}

