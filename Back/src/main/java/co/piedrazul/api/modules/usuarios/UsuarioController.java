package co.piedrazul.api.modules.usuarios;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
  private final UsuarioService usuarioService;

  public UsuarioController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN')")
  public Map<String, Object> listar() {
    return Map.of("ok", true, "data", usuarioService.listar());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public Map<String, Object> obtener(@PathVariable String id) {
    return Map.of("ok", true, "data", usuarioService.obtener(id));
  }

  @GetMapping("/me")
  public Map<String, Object> me(Authentication authentication) {
    return Map.of("ok", true, "data", usuarioService.obtenerActual(authentication));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN')")
  public Map<String, Object> crear(@Valid @RequestBody UsuarioCreateRequest request) {
    return Map.of("ok", true, "message", "Usuario creado exitosamente", "data", usuarioService.crear(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public Map<String, Object> actualizar(@PathVariable String id, @Valid @RequestBody UsuarioUpdateRequest request) {
    return Map.of("ok", true, "message", "Usuario actualizado exitosamente", "data", usuarioService.actualizar(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public Map<String, Object> desactivar(@PathVariable String id) {
    return Map.of("ok", true, "message", "Usuario desactivado exitosamente", "data", usuarioService.desactivar(id));
  }
}