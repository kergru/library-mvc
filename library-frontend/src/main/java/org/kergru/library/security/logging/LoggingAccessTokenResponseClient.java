package org.kergru.library.security.logging;

import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

public class LoggingAccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

  private final DefaultAuthorizationCodeTokenResponseClient delegate = new DefaultAuthorizationCodeTokenResponseClient();

  @Override
  public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest grantRequest) {
    // PKCE code_verifier
    String codeVerifier = (String) grantRequest.getAuthorizationExchange()
        .getAuthorizationRequest()
        .getAttributes()
        .get("code_verifier");

    // Authorization Code
    String code = grantRequest.getAuthorizationExchange().getAuthorizationResponse().getCode();

    // State
    String state = grantRequest.getAuthorizationExchange().getAuthorizationResponse().getState();

    System.out.println("PKCE token exchange - code=" + code);
    System.out.println("PKCE token exchange - state=" + state);
    System.out.println("PKCE token exchange - code_verifier=" + codeVerifier);

    OAuth2AccessTokenResponse response = delegate.getTokenResponse(grantRequest);

    logTokenResponse(response);

    return response;
  }

  private void logTokenResponse(OAuth2AccessTokenResponse response) {
    System.out.println("OAuth2 Token Response: Access Token: " + response.getAccessToken().getTokenValue());
    System.out.println("OAuth2 Token Response: Token Type: " + response.getAccessToken().getTokenType().getValue());
    System.out.println("OAuth2 Token Response: Expires In: " + response.getAccessToken().getExpiresAt());
    System.out.println("OAuth2 Token Response: Refresh Token: " + (response.getRefreshToken() != null ? response.getRefreshToken().getTokenValue() : "No refresh token"));
    System.out.println("OAuth2 Token Response: Scopes: " + response.getAccessToken().getScopes());
  }
}
