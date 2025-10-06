package org.kergru.library.util;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class JwtTestUtils {

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

  public static OidcUser mockOidcUser(String username) {
    Map<String, Object> claims = Map.of(
        "sub", "123",
        "preferred_username", username
    );
    OidcIdToken idToken = new OidcIdToken("token123", Instant.now(),
        Instant.now().plusSeconds(3600), claims);
    return new DefaultOidcUser(List.of(), idToken);
  }
}

