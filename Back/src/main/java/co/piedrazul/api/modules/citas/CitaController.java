package co.piedrazul.api.modules.citas;

import co.piedrazul.api.security.AuthUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {
  private final CitaService citaService;

  public CitaController(CitaService citaService) {
    this.citaService = citaService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','AGENDADOR','MEDICO')")
  public Map<String, Object> listar(@RequestParam Long medicoId, @RequestParam LocalDate fecha) {
    CitasPorFechaResponse data = citaService.listarPorMedicoYFecha(medicoId, fecha);
    return Map.of("ok", true, "data", data);
  }

  @GetMapping("/slots")
  @PreAuthorize("hasAnyRole('ADMIN','AGENDADOR','MEDICO')")
  public Map<String, Object> slots(@RequestParam Long medicoId, @RequestParam LocalDate fecha) {
    CitasPorFechaResponse data = citaService.listarPorMedicoYFecha(medicoId, fecha);
    return Map.of(
      "ok", true,
      "data", Map.of(
        "slots", data.slots(),
        "disponibles", data.disponibles(),
        "ocupados", data.total()
      )
    );
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','AGENDADOR')")
  public Map<String, Object> crear(@Valid @RequestBody CrearCitaRequest request, Authentication authentication) {
    AuthUser user = (AuthUser) authentication.getPrincipal();
    CitaCreadaResponse data = citaService.crearManual(request, user.id(), "MANUAL");
    return Map.of("ok", true, "message", "Cita creada exitosamente", "data", data);
  }
}
