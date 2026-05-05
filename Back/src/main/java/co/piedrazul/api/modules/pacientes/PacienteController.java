package co.piedrazul.api.modules.pacientes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/v1/pacientes")
public class PacienteController {
  private final PacienteService pacienteService;

  public PacienteController(PacienteService pacienteService) {
    this.pacienteService = pacienteService;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> buscar(@RequestParam String documento) {
    Paciente p = pacienteService.buscarPorDocumento(documento.trim());
    if (p == null) {
      Map<String, Object> body = new HashMap<>();
      body.put("ok", false);
      body.put("message", "Paciente no encontrado");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    Map<String, Object> body = new HashMap<>();
    body.put("ok", true);
    body.put("data", p);
    return ResponseEntity.ok(body);
  }

  @GetMapping("/sugerencias")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> sugerencias(@RequestParam String documento) {
    String prefijo = documento == null ? "" : documento.trim();
    List<PacienteAutocompleteDto> data = pacienteService.sugerenciasPorDocumento(prefijo);
    return Map.of("ok", true, "data", data);
  }

  @PostMapping("/registro")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> registrar(@Valid @RequestBody PacienteInput input) {
    FindOrCreateResult result = pacienteService.findOrCreate(input);
    return Map.of(
      "ok", true,
      "message", result.esNuevo() ? "Paciente registrado" : "Paciente ya existe",
      "data", result.paciente(),
      "esNuevo", result.esNuevo()
    );
  }
}
