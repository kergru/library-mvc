package org.kergru.library.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.kergru.library.security.logging.JwtLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  /**
   * Configures the security filter chain for OAuth2 Resource Server with JWT authentication.
   * This configuration sets up the application as an OAuth2 Resource Server that validates
   * JWT (JSON Web Token) access tokens. The JWT configuration is loaded from application
   * properties/YAML, including:
   * <ul>
   *   <li>issuer-uri - The URI of the authorization server that issues the JWTs</li>
   *   <li>jwk-set-uri - The URI to fetch the public keys for JWT signature validation</li>
   *   <li>jws-algorithms - The allowed signing algorithms for the JWTs</li>
   * </ul>
   *
   * The OAuth2 Resource Server is configured using the default JWT handling provided by
   * Spring Security, which includes automatic validation of:
   * <ul>
   *   <li>Token signature using the authorization server's public keys</li>
   *   <li>Token expiration time</li>
   *   <li>Token issuer claim</li>
   * </ul>
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .addFilterAfter(new JwtLoggingFilter(), BearerTokenAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
        )
        .build();
  }

  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
    scopesConverter.setAuthorityPrefix("SCOPE_"); // falls du auch Scopes behalten willst

    return jwt -> {
      Collection<GrantedAuthority> authorities = new ArrayList<>(scopesConverter.convert(jwt));

      Map<String, Object> realmAccess = jwt.getClaim("realm_access");
      if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
      }

      return new JwtAuthenticationToken(jwt, authorities);
    };
  }
}
