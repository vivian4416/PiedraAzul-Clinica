package co.piedrazul.api.modules.pacientes;

import co.piedrazul.api.core.AppException;
import java.util.Map;
import org.springframework.http.HttpStatus;
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
  @PreAuthorize("hasAnyRole('ADMIN','AGENDADOR','MEDICO')")
  public Map<String, Object> buscar(@RequestParam String documento) {
    Paciente p = pacienteService.buscarPorDocumento(documento.trim());
    if (p == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Paciente no encontrado");
    }
    return Map.of("ok", true, "data", p);
  }
}
