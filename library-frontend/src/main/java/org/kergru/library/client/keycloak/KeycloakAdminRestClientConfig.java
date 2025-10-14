package org.kergru.library.client.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class KeycloakAdminRestClientConfig {

  @Bean
  public RestClient keycloakAdminRestClient(
      @Value("${keycloak.admin.base-url}") String keycloakBaseUrl) {

    return RestClient.builder()
        .baseUrl(keycloakBaseUrl)
        .requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
        .build();
  }
}
