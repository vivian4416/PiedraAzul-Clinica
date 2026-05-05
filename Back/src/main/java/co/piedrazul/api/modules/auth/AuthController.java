package co.piedrazul.api.modules.auth;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import co.piedrazul.api.modules.usuarios.UsuarioResponse;
import co.piedrazul.api.modules.usuarios.UsuarioService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final UsuarioService usuarioService;

  public AuthController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.GONE)
  public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
    return Map.of(
      "ok", false,
      "code", "GONE",
      "message", "Login local deshabilitado. Usa Keycloak (OIDC) para autenticacion."
    );
  }

  @PostMapping("/registro")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> registrar(@Valid @RequestBody RegisterPacienteRequest request) {
    UsuarioResponse data = usuarioService.registrarPaciente(request);
    return Map.of("ok", true, "message", "Paciente registrado", "data", data);
  }
}
