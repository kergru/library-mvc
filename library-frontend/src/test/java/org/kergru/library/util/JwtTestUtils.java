package org.kergru.library.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;

public class JwtTestUtils {

  private static RSAKey rsaJwk;

  static {
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
      gen.initialize(2048);
      KeyPair keyPair = gen.generateKeyPair();

      rsaJwk = new RSAKey.Builder((java.security.interfaces.RSAPublicKey) keyPair.getPublic())
          .privateKey(keyPair.getPrivate())
          .keyID("test-key")
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String createJwt(String username) {
    try {
      JWTClaimsSet claims = new JWTClaimsSet.Builder()
          .issuer("http://localhost:8085/realms/library")
          .subject(username)
          .claim("preferred_username", username)
          .expirationTime(new Date(System.currentTimeMillis() + 3600_000)) // 1h gültig
          .issueTime(new Date())
          .build();

      JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
          .keyID(rsaJwk.getKeyID())
          .type(JOSEObjectType.JWT)
          .build();

      SignedJWT signedJWT = new SignedJWT(header, claims);
      signedJWT.sign(new RSASSASigner(rsaJwk.toPrivateKey()));

      return signedJWT.serialize();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String getJwks() {
    return new JWKSet(rsaJwk.toPublicJWK()).toJSONObject().toString();
  }
}

