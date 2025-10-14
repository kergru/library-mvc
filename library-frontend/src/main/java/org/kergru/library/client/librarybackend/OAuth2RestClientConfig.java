package org.kergru.library.client.librarybackend;

import org.kergru.library.client.librarybackend.logging.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

/**
 * Configuration for OAuth2 RestClient
 * Defines a RestClient bean with OAuth2 authentication interceptor and logging interceptor.
 */
@Configuration
public class OAuth2RestClientConfig {

  @Bean
  public RestClient oauth2RestClient (
      OAuth2AuthorizedClientManager authorizedClientManager,
      @Value("${library.backend.baseUrl}") String backendBaseUrl){

    // OAuth2 token interceptor
    OAuth2ClientHttpRequestInterceptor oauth2Interceptor =
        new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
    oauth2Interceptor.setClientRegistrationIdResolver(request -> "keycloak");

    return RestClient.builder()
        .baseUrl(backendBaseUrl)
        .requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
        .requestInterceptor(oauth2Interceptor) //append bearer token with interceptor
        .requestInterceptor(new LoggingInterceptor()) //log request and response
        .build();
  }
}