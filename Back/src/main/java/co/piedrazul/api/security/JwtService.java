package co.piedrazul.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
  private final String secret;
  private final long expirationMinutes;

  public JwtService(@Value("${app.jwt.secret}") String secret,
                    @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
    this.secret = secret;
    this.expirationMinutes = expirationMinutes;
  }

  public String generate(Long userId, String login, String role) {
    Instant now = Instant.now();
    return Jwts.builder()
      .subject(String.valueOf(userId))
      .claim("login", login)
      .claim("rol", role)
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
      .signWith(signingKey())
      .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser()
      .verifyWith((javax.crypto.SecretKey) signingKey())
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

  private Key signingKey() {
    if (secret.length() >= 32) {
      return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }
}
