package co.piedrazul.api.modules.pacientes;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pacientes")
public class PacienteController {
  private final PacienteService pacienteService;

  public PacienteController(PacienteService pacienteService) {
    this.pacienteService = pacienteService;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> buscar(@RequestParam String documento) {
    Paciente p = pacienteService.buscarPorDocumento(documento.trim());
    return Map.of("ok", true, "data", p);
  }

  @GetMapping("/sugerencias")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> sugerencias(@RequestParam String documento) {
    String prefijo = documento == null ? "" : documento.trim();
    List<PacienteAutocompleteDto> data = pacienteService.sugerenciasPorDocumento(prefijo);
    return Map.of("ok", true, "data", data);
  }
}
