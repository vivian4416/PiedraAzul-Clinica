package co.piedrazul.api.modules.citas;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import co.piedrazul.api.core.AppException;
import co.piedrazul.api.modules.medicos.Medico;
import co.piedrazul.api.modules.medicos.MedicoService;
import co.piedrazul.api.modules.pacientes.PacienteRepository;
import co.piedrazul.api.modules.pacientes.PacienteService;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

class CitaServiceTest {

  @Test
  void fallaSiMedicoNoAtiendeEseDia() throws Exception {
    CitaRepository citaRepo = Mockito.mock(CitaRepository.class);
    ConfiguracionCitasService configuracionCitasService = Mockito.mock(ConfiguracionCitasService.class);
    MedicoService medicoService = Mockito.mock(MedicoService.class);
    PacienteService pacienteService = Mockito.mock(PacienteService.class);
    PacienteRepository pacienteRepository = Mockito.mock(PacienteRepository.class);
    SlotService slotService = new SlotService();
    ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

    CitaService service = new CitaService(
      citaRepo,
      configuracionCitasService,
      medicoService,
      pacienteService,
      pacienteRepository,
      slotService,
      eventPublisher
    );

    Medico medico = new Medico();
    setField(medico, "id", 1L);
    setField(medico, "nombres", "Dr X");
    setField(medico, "intervaloMin", 20);
    setField(medico, "activo", true);

    when(configuracionCitasService.getVentanaSemanas()).thenReturn(52);
    when(medicoService.obtenerActivoOFallar(1L)).thenReturn(medico);
    when(medicoService.getDisponibilidadParaFecha(1L, LocalDate.of(2026, 3, 29))).thenReturn(null);

    CrearCitaRequest req = new CrearCitaRequest("1", "A", "B", "300", "HOMBRE", null, null, 1L,
      LocalDate.of(2026, 3, 29), LocalTime.of(8, 0));

    assertThrows(AppException.class, () -> service.crearManual(req, 2L, "MANUAL"));
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field f = target.getClass().getDeclaredField(fieldName);
    f.setAccessible(true);
    f.set(target, value);
  }
}
