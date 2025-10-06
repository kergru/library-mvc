package org.kergru.library;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

public class JwtTestUtils {

  public static OidcUser mockOidcUser(String username) {
    Map<String, Object> claims = Map.of(
        "sub", "123",
        "preferred_username", username
    );
    OidcIdToken idToken = new OidcIdToken("token123", Instant.now(),
        Instant.now().plusSeconds(3600), claims);
    return new DefaultOidcUser(List.of(), idToken);
  }

  public static OidcUser mockOidcUserLibrarian(String username) {
    Map<String, Object> claims = Map.of(
        "sub", "123",
        "preferred_username", username,
        "realm_access", Map.of("roles", List.of("LIBRARIAN"))
    );
    OidcIdToken idToken = new OidcIdToken("token123", Instant.now(),
        Instant.now().plusSeconds(3600), claims);
    return new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_LIBRARIAN")), idToken);
  }

  public static Jwt createJwt(OidcUser user) {
    OidcIdToken idToken = user.getIdToken();
    Map<String, Object> claims = idToken.getClaims();

    return Jwt.withTokenValue("test-token")
        .header("alg", "none")
        .claim(JwtClaimNames.SUB, claims.get("sub"))
        .claim("preferred_username", claims.get("preferred_username"))
        .claim("realm_access", claims.get("realm_access"))
        .claim("scope", "openid profile email")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
  }

  public static Jwt createJwtWithAuthorities(OidcUser user, String... authorities) {
    OidcIdToken idToken = user.getIdToken();
    Map<String, Object> claims = idToken.getClaims();

    // Authorities anhängen
    Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    for (String authority : authorities) {
      grantedAuthorities.add(new SimpleGrantedAuthority(authority));
    }

    return Jwt.withTokenValue("test-token")
        .header("alg", "none")
        .claim(JwtClaimNames.SUB, claims.get("sub"))
        .claim("preferred_username", claims.get("preferred_username"))
        .claim("realm_access", claims.get("realm_access"))
        .claim("scope", "openid profile email")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
  }

  public static JwtRequestPostProcessor jwtWithRoles(String username, String... roles) {
    Jwt jwt = Jwt.withTokenValue("test-token")
        .header("alg", "none")
        .claim(JwtClaimNames.SUB, UUID.randomUUID().toString())
        .claim("preferred_username", username)
        .claim("realm_access", Map.of("roles", List.of(roles)))
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();

    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String role : roles) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
    }

    return SecurityMockMvcRequestPostProcessors.jwt()
        .jwt(jwt)
        .authorities(authorities);
  }
}
