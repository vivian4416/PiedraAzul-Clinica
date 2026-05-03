package co.piedrazul.api.modules.auth;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  @PostMapping("/login")
  @ResponseStatus(HttpStatus.GONE)
  public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
    return Map.of(
      "ok", false,
      "code", "GONE",
      "message", "Login local deshabilitado. Usa Keycloak (OIDC) para autenticacion."
    );
  }
}
