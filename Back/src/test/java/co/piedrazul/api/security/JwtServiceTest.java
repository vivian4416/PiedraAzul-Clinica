package co.piedrazul.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
  @Test
  void generaYParseaClaimsDelToken() {
    JwtService service = new JwtService("12345678901234567890123456789012", 15);

    String token = service.generate(7L, "ana", "PACIENTE");
    Claims claims = service.parse(token);

    assertNotNull(token);
    assertEquals("7", claims.getSubject());
    assertEquals("ana", claims.get("login", String.class));
    assertEquals("PACIENTE", claims.get("rol", String.class));
    assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
  }
}

