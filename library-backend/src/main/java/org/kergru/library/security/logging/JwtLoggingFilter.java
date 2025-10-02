package org.kergru.library.security.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Enumeration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtLoggingFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;

  public JwtLoggingFilter() {
    this.objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .registerModule(new JavaTimeModule());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, java.io.IOException {

    System.out.println("Incoming request: " + request.getRequestURI());

    Enumeration<String> headerNames = request.getHeaderNames();
    if (headerNames != null) {
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        if ("authorization".equals(headerName)) {
          System.out.println("Authorization header -  " + headerName + ": " + Collections.list(request.getHeaders(headerName)));
        }
      }
    }

    // Logge JWT Token falls vorhanden
    // JWT wurde aus dem Authorization Header extrahiert und durch JWKS validiert
    if (SecurityContextHolder.getContext().getAuthentication() != null &&
        SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
      try {
        String jwtJson = objectMapper.writeValueAsString(jwt.getClaims());
        System.out.println("JWT Token Claims:\n" + jwtJson);
      } catch (JsonProcessingException e) {
        System.out.println("Could not parse JWT token: " + e.getMessage());
      }
    } else {
      System.out.println("No JWT token found in security context");
    }

    filterChain.doFilter(request, response);
  }
}