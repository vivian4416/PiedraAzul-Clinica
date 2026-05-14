package co.piedrazul.api.modules.citas;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {
  private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
  private final AuditoriaRepository auditoriaRepository;

  public AuditoriaService(AuditoriaRepository auditoriaRepository) {
    this.auditoriaRepository = auditoriaRepository;
  }

  public void registrarReagendamiento(
      Long citaId,
      String usuarioId,
      String medicoAnterior,
      String medicoNuevo,
      LocalDateTime fechaAnterior,
      LocalDateTime fechaNueva
  ) {
    Auditoria auditoria = new Auditoria();
    auditoria.setUsuarioId(usuarioId);
    auditoria.setAccion("REAGENDAR_CITA");
    auditoria.setEntidad("citas");
    auditoria.setEntidadId(citaId);
    auditoria.setDetalle(
        "{\"medicoAnterior\":\"" + medicoAnterior +
            "\",\"medicoNuevo\":\"" + medicoNuevo +
            "\",\"fechaHoraAnterior\":\"" + fechaAnterior +
            "\",\"fechaHoraNueva\":\"" + fechaNueva + "\"}"
    );
    auditoria.setFechaHora(LocalDateTime.now());
    auditoriaRepository.save(auditoria);
  }

  @EventListener
  public void onCitaCreated(CitaCreadaEvent event) {
    long startedAt = System.currentTimeMillis();
    log.info("[RF3] auditoria inicio citaId={} usuarioId={}", event.citaId(), event.creadoPor());

    Auditoria a = new Auditoria();
    a.setUsuarioId(event.creadoPor());
    a.setAccion("CREAR_CITA");
    a.setEntidad("citas");
    a.setEntidadId(event.citaId());
    a.setDetalle("{\"medicoId\":\"" + event.medicoId() + "\",\"esNuevoPaciente\":" + event.esNuevoPaciente() + "}");
    a.setFechaHora(LocalDateTime.now());
    auditoriaRepository.save(a);

    log.info("[RF3] auditoria fin citaId={} elapsedMs={}", event.citaId(), (System.currentTimeMillis() - startedAt));
  }
}