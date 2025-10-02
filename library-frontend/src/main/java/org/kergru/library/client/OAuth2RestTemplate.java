package org.kergru.library.client;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

public class OAuth2RestTemplate extends RestTemplate {

  public OAuth2RestTemplate (
      OAuth2AuthorizedClientManager authorizedClientManager,
      String backendBaseUrl) {

    this.setUriTemplateHandler(new DefaultUriBuilderFactory(backendBaseUrl));

    this.getInterceptors().add((request, body, execution) -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
          .withClientRegistrationId("keycloak")
          .principal(authentication)
          .build();

      OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
      if (authorizedClient != null) {
        request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());

        // Logging
        System.out.println("Outgoing request to: " + request.getURI());
        System.out.println("Bearer token: " + request.getHeaders().getFirst("Authorization"));
      }
      return execution.execute(request, body);
    });
  }
}
