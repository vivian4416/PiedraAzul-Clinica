package co.piedrazul.api.modules.citas;

import co.piedrazul.api.security.AuthUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger log = LoggerFactory.getLogger(CitaController.class);
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

  @PostMapping("/autonoma")
  @PreAuthorize("hasAnyRole('ADMIN','AGENDADOR','PACIENTE')")
  public Map<String, Object> crearAutonoma(@Valid @RequestBody CrearCitaRequest request, Authentication authentication) {
    long startedAt = System.currentTimeMillis();
    AuthUser user = (AuthUser) authentication.getPrincipal();
    log.info("[RF3] iniciar crearAutonoma userId={} rol={} medicoId={} fecha={} hora={}",
      user.id(), user.role(), request.medicoId(), request.fecha(), request.hora());

    CitaCreadaResponse data = citaService.crearAutonoma(request, user.id());

    log.info("[RF3] fin crearAutonoma citaId={} elapsedMs={}", data.citaId(), (System.currentTimeMillis() - startedAt));
    return Map.of("ok", true, "message", "Cita web creada exitosamente", "data", data);
  }
}
