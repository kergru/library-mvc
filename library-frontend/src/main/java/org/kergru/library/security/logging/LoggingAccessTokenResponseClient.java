package org.kergru.library.security.logging;

import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

/**
 * OAuth2 access token response client with logging.
 * Logs the authorization code and state before delegating to the delegate.
 * Logs the access token response after delegating to the delegate.
 */
public class LoggingAccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

  private final RestClientAuthorizationCodeTokenResponseClient delegate = new RestClientAuthorizationCodeTokenResponseClient();

  @Override
  public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest grantRequest) {
    String code = grantRequest.getAuthorizationExchange()
        .getAuthorizationResponse()
        .getCode();

    String state = grantRequest.getAuthorizationExchange()
        .getAuthorizationResponse()
        .getState();

    System.out.println("Token exchange - code=" + code);
    System.out.println("Token exchange - state=" + state);

    OAuth2AccessTokenResponse response = delegate.getTokenResponse(grantRequest);

    logTokenResponse(response);
    return response;
  }

  private void logTokenResponse(OAuth2AccessTokenResponse response) {
    System.out.println("OAuth2 Token Response: Access Token: " + response.getAccessToken().getTokenValue());
    System.out.println("OAuth2 Token Response: Token Type: " + response.getAccessToken().getTokenType().getValue());
    System.out.println("OAuth2 Token Response: Expires At: " + response.getAccessToken().getExpiresAt());
    System.out.println("OAuth2 Token Response: Refresh Token: " +
        (response.getRefreshToken() != null ? response.getRefreshToken().getTokenValue() : "No refresh token"));
    System.out.println("OAuth2 Token Response: Scopes: " + response.getAccessToken().getScopes());
  }
}
