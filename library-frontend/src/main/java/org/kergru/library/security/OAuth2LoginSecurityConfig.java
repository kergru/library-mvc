package org.kergru.library.security;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.kergru.library.security.logging.LoggingAccessTokenResponseClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

/**
 * Configuration for OAuth2 login security.
 * Defines a SecurityFilterChain bean with OAuth2 login configuration.
 */
@Configuration
@EnableWebSecurity
public class OAuth2LoginSecurityConfig {

  @Bean
  public SpringSecurityDialect securityDialect() {
    return new SpringSecurityDialect();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      ClientRegistrationRepository clientRegistrationRepository) throws Exception {

    //browser-to-service communication with session cookie, csrf per default enabled
    http
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/", "/login", "/public/**").permitAll()
            .requestMatchers("/library/ui/admin/**").hasAuthority("ROLE_LIBRARIAN")
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
            .tokenEndpoint(token -> token
                .accessTokenResponseClient(new LoggingAccessTokenResponseClient())) // add custom access token response client for logging
            .userInfoEndpoint(userInfo -> userInfo
                .oidcUserService(this.oidcUserService())
            )
            .successHandler(authenticationSuccessHandler())
        )
        .logout(logout -> logout
            .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")
        )
        .exceptionHandling(exception -> exception
            .accessDeniedPage("/error/403")
        );

    return http.build();
  }

  private OidcUserService oidcUserService() {
    return new OidcUserService() {
      @Override
      public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        //log OidcUserService call
        System.out.println("Response of OidcUserService.loadUser: " + oidcUser);

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

        //Load user roles from realm_access in token
        Map<String, Object> realmAccess = oidcUser.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
          @SuppressWarnings("unchecked")
          List<String> roles = (List<String>) realmAccess.get("roles");
          roles.forEach(role ->
              authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
          );
        }

        //log authorities
        System.out.println("Authorities: " + authorities);

        return new CustomOidcUser(oidcUser, authorities);
      }
    };
  }

  private AuthenticationSuccessHandler authenticationSuccessHandler() {
    return (request, response, authentication) -> {
      boolean isLibrarian = authentication.getAuthorities().stream()
          .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_LIBRARIAN"));

      if (isLibrarian) {
        response.sendRedirect("/library/ui/admin/users");
      } else {
        response.sendRedirect("/library/ui/me");
      }
    };
  }

  private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler(
      ClientRegistrationRepository clientRegistrationRepository) {
    OidcClientInitiatedLogoutSuccessHandler successHandler =
        new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
    successHandler.setPostLogoutRedirectUri("{baseUrl}");
    return successHandler;
  }

  /**
   * Custom OidcUser implementation if you need to add custom fields or behavior (Set preferred_username as username and roles as authorities)
   */
  private static class CustomOidcUser extends org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser {

    public CustomOidcUser(OidcUser user, Set<GrantedAuthority> authorities) {
      super(authorities, user.getIdToken(), user.getUserInfo(), "preferred_username");
    }
  }
}
