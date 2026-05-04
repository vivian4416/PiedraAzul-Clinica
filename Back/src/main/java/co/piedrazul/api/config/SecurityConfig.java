package co.piedrazul.api.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
    http
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .csrf(csrf -> csrf.disable())
      .httpBasic(AbstractHttpConfigurer::disable)
      .formLogin(AbstractHttpConfigurer::disable)
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint(authenticationEntryPoint())
        .accessDeniedHandler(accessDeniedHandler())
      )
      .oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt
          .jwtAuthenticationConverter(this::jwtAuthentication)
        )
      )
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/health").permitAll()
        .anyRequest().authenticated()
      );

    // Filtros de logging SOLO dentro del SecurityFilterChain (no como @Bean)
    // - Antes del BearerTokenAuthenticationFilter: inspección rápida + intento de decode (para ver por qué falla)
    http.addFilterBefore(requestLoggingFilter(jwtDecoder), BearerTokenAuthenticationFilter.class);
    // - Justo después del BearerTokenAuthenticationFilter: confirmar si se pobló el SecurityContext
    http.addFilterAfter(securityContextLoggingFilter("AFTER_BEARER"), BearerTokenAuthenticationFilter.class);
    // - Al final (después de AuthorizationFilter): estado final del contexto
    http.addFilterAfter(securityContextLoggingFilter("AFTER_AUTHZ"), AuthorizationFilter.class);

    return http.build();
  }

  private AbstractAuthenticationToken jwtAuthentication(Jwt jwt) {
    String principalName = jwt.getClaimAsString("preferred_username");
    if (principalName == null || principalName.isBlank()) {
      principalName = jwt.getSubject();
    }
    try {
      log.info("[CONVERT] iss={} sub={} preferred_username={} authorities={}",
        jwt.getIssuer(),
        jwt.getSubject(),
        jwt.getClaimAsString("preferred_username"),
        extractAuthorities(jwt));
    } catch (Exception ex) {
      log.info("[CONVERT] ERROR type={} msg={}", ex.getClass().getName(), ex.getMessage());
    }
    return new JwtAuthenticationToken(jwt, extractAuthorities(jwt), principalName);
  }

  private OncePerRequestFilter requestLoggingFilter(JwtDecoder jwtDecoder) {
    return new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String jwtInfo = inspectJwt(authorization);
        log.info("[REQ] {} {} authPresent={} authPrefix={}",
          request.getMethod(),
          request.getRequestURI(),
          authorization != null,
          authorization == null ? "none" : (authorization.startsWith("Bearer ") ? "Bearer" : "other"));
        if (jwtInfo != null) {
          log.info("[JWT] {} {}", request.getRequestURI(), jwtInfo);
        }

        // Validación real contra el JwtDecoder configurado (firma, issuer, exp, etc.)
        if (authorization != null && authorization.startsWith("Bearer ")) {
          String token = authorization.substring("Bearer ".length()).trim();
          try {
            Jwt decoded = jwtDecoder.decode(token);
            log.info("[DECODE] {} ok iss={} sub={} aud={} exp={} preferred_username={}",
              request.getRequestURI(),
              decoded.getIssuer(),
              decoded.getSubject(),
              decoded.getAudience(),
              decoded.getExpiresAt(),
              decoded.getClaimAsString("preferred_username"));
          } catch (JwtException ex) {
            log.info("[DECODE] {} FAIL type={} msg={}",
              request.getRequestURI(),
              ex.getClass().getName(),
              ex.getMessage());
          } catch (Exception ex) {
            log.info("[DECODE] {} ERROR type={} msg={}",
              request.getRequestURI(),
              ex.getClass().getName(),
              ex.getMessage());
          }
        }
        filterChain.doFilter(request, response);
      }

      private String inspectJwt(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
          return null;
        }

        try {
          String token = authorization.substring("Bearer ".length()).trim();
          String[] parts = token.split("\\.");
          if (parts.length < 2) {
            return "tokenParts=" + parts.length;
          }

          String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
          Map<?, ?> payload = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payloadJson, Map.class);

          Object roles = null;
          Object realmAccess = payload.get("realm_access");
          if (realmAccess instanceof Map<?, ?> realmMap) {
            roles = realmMap.get("roles");
          }

          return "iss=" + payload.get("iss")
            + " azp=" + payload.get("azp")
            + " sub=" + payload.get("sub")
            + " preferred_username=" + payload.get("preferred_username")
            + " aud=" + payload.get("aud")
            + " exp=" + payload.get("exp")
            + " roles=" + roles;
        } catch (Exception ex) {
          return "jwtParseError=" + ex.getClass().getSimpleName() + ":" + ex.getMessage();
        }
      }
    };
  }

  private OncePerRequestFilter securityContextLoggingFilter(String phase) {
    return new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/v1/")) {
          var before = SecurityContextHolder.getContext().getAuthentication();
          log.info("[SEC:{}:BEFORE] {} {} authClass={} principal={} authorities={}",
            phase,
            request.getMethod(),
            request.getRequestURI(),
            before == null ? "null" : before.getClass().getName(),
            before == null ? "null" : before.getName(),
            before == null ? "null" : before.getAuthorities());
        }

        filterChain.doFilter(request, response);

        if (request.getRequestURI().startsWith("/api/v1/")) {
          var after = SecurityContextHolder.getContext().getAuthentication();
          log.info("[SEC:{}:AFTER] {} {} status={} authClass={} principal={} authorities={}",
            phase,
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            after == null ? "null" : after.getClass().getName(),
            after == null ? "null" : after.getName(),
            after == null ? "null" : after.getAuthorities());
        }
      }
    };
  }

  @Bean
  AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, authException) -> {
      Throwable cause = authException.getCause();
      log.info("[AUTH-401] {} {} type={} msg={} causeType={} causeMsg={}",
        request.getMethod(),
        request.getRequestURI(),
        authException.getClass().getName(),
        authException.getMessage(),
        cause == null ? "none" : cause.getClass().getName(),
        cause == null ? "none" : cause.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"ok\":false,\"code\":\"UNAUTHORIZED\",\"message\":\"No autenticado o token invalido\"}");
    };
  }

  @Bean
  AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
      log.warn("[AUTH-403] {} {} msg={}", request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("application/json");
      response.getWriter().write("{\"ok\":false,\"code\":\"FORBIDDEN\",\"message\":\"No tienes permisos para ejecutar esta operacion\"}");
    };
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    LinkedHashSet<String> roles = new LinkedHashSet<>();

    Object realmAccess = jwt.getClaim("realm_access");
    if (realmAccess instanceof Map<?, ?> realmMap) {
      Object roleValues = realmMap.get("roles");
      if (roleValues instanceof Collection<?> collection) {
        for (Object value : collection) {
          if (value != null) {
            String role = value.toString().trim();
            if (!role.isEmpty()) {
              roles.add(role);
              roles.add(role.toUpperCase());
              // administrador tiene permisos de ADMIN y AGENDADOR (son el mismo perfil)
              if ("administrador".equalsIgnoreCase(role)) {
                roles.add("ADMIN");
                roles.add("AGENDADOR");
              } else {
                roles.add(aliasRole(role));
              }
            }
          }
        }
      }
    }

    return roles.stream()
      .filter(role -> role != null && !role.isBlank())
      .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
      .distinct()
      .map(SimpleGrantedAuthority::new)
      .collect(Collectors.toList());
  }

  private String aliasRole(String role) {
    return switch (role.toLowerCase()) {
      case "medico" -> "MEDICO";
      case "paciente" -> "PACIENTE";
      case "agendador" -> "AGENDADOR";
      default -> role.toUpperCase();
    };
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
    config.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
