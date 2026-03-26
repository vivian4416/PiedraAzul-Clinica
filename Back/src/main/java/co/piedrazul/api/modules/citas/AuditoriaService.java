package co.piedrazul.api.modules.citas;

import java.time.LocalDateTime;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {
  private final AuditoriaRepository auditoriaRepository;

  public AuditoriaService(AuditoriaRepository auditoriaRepository) {
    this.auditoriaRepository = auditoriaRepository;
  }

  @EventListener
  public void onCitaCreated(CitaCreadaEvent event) {
    Auditoria a = new Auditoria();
    a.setUsuarioId(event.creadoPor());
    a.setAccion("CREAR_CITA");
    a.setEntidad("citas");
    a.setEntidadId(event.citaId());
    a.setDetalle("{\"medicoId\":" + event.medicoId() + ",\"esNuevoPaciente\":" + event.esNuevoPaciente() + "}");
    a.setFechaHora(LocalDateTime.now());
    auditoriaRepository.save(a);
  }
}
