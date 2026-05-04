package co.piedrazul.api.modules.citas;

import java.time.LocalDate;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.piedrazul.api.core.AppException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/citas")
/** Endpoints de citas: listado, slots disponibles y creacion manual/autonoma. */
public class CitaController {
  private static final Logger log = LoggerFactory.getLogger(CitaController.class);
  private final CitaService citaService;

  public CitaController(CitaService citaService) {
    this.citaService = citaService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','AGENDADOR','MEDICO')")
  public Map<String, Object> listar(
    @RequestParam String medicoId,
    @RequestParam LocalDate fecha,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "25") int size
  ) {
    CitasPorFechaResponse data = citaService.listarPorMedicoYFechaPaginado(medicoId, fecha, page, size);
    return Map.of("ok", true, "data", data);
  }

  @GetMapping("/slots")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> slots(@RequestParam String medicoId, @RequestParam LocalDate fecha) {
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
    String creadoPor = keycloakUserId(authentication);
    CitaCreadaResponse data = citaService.crearManual(request, creadoPor, "MANUAL");
    return Map.of("ok", true, "message", "Cita creada exitosamente", "data", data);
  }

  @PostMapping("/autonoma")
  @PreAuthorize("hasAnyRole('ADMIN','AGENDADOR','PACIENTE')")
  public Map<String, Object> crearAutonoma(@Valid @RequestBody CrearCitaRequest request, Authentication authentication) {
    long startedAt = System.currentTimeMillis();
    String creadoPor = keycloakUserId(authentication);
    log.info("[RF3] iniciar crearAutonoma sub={} medicoId={} fecha={} hora={}",
      creadoPor, request.medicoId(), request.fecha(), request.hora());

    CitaCreadaResponse data = citaService.crearAutonoma(request, creadoPor);

    log.info("[RF3] fin crearAutonoma citaId={} elapsedMs={}", data.citaId(), (System.currentTimeMillis() - startedAt));
    return Map.of("ok", true, "message", "Cita web creada exitosamente", "data", data);
  }

  private String keycloakUserId(Authentication authentication) {
    if (authentication == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Usuario no autenticado");
    }

    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
        return jwt.getSubject();
      }
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof Jwt jwt && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
      return jwt.getSubject();
    }

    String name = authentication.getName();
    if (name == null || name.isBlank()) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Usuario no autenticado");
    }
    return name;
  }
}
