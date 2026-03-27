package co.piedrazul.api.modules.medicos;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/medicos")
public class MedicoController {
  private final MedicoService medicoService;

  public MedicoController(MedicoService medicoService) {
    this.medicoService = medicoService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','AGENDADOR','MEDICO')")
  public Map<String, Object> listar() {
    List<Medico> data = medicoService.listarActivos();
    return Map.of("ok", true, "data", data);
  }

  @GetMapping("/configuracion")
  @PreAuthorize("hasAnyRole('ADMIN','AGENDADOR')")
  public Map<String, Object> obtenerConfiguracion() {
    ConfiguracionAgendamientoResponse data = medicoService.obtenerConfiguracionAgendamiento();
    return Map.of("ok", true, "data", data);
  }

  @PutMapping("/configuracion")
  @PreAuthorize("hasAnyRole('ADMIN','AGENDADOR')")
  public Map<String, Object> guardarConfiguracion(@Valid @RequestBody GuardarConfiguracionAgendamientoRequest request) {
    ConfiguracionAgendamientoResponse data = medicoService.guardarConfiguracionAgendamiento(request);
    return Map.of("ok", true, "message", "Configuracion actualizada", "data", data);
  }
}
