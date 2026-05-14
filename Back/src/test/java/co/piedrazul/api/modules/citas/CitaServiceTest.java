package co.piedrazul.api.modules.citas;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.context.ApplicationEventPublisher;

import co.piedrazul.api.core.AppException;
import co.piedrazul.api.modules.medicos.Medico;
import co.piedrazul.api.modules.medicos.MedicoService;
import co.piedrazul.api.modules.pacientes.PacienteRepository;
import co.piedrazul.api.modules.pacientes.PacienteService;
import co.piedrazul.api.modules.usuarios.UsuarioService;

class CitaServiceTest {

  private static final String MEDICO_ID = "795ee435-a5d2-4817-87b0-11632b46ff4c";

  @Test
  void fallaSiMedicoNoAtiendeEseDia() throws Exception {
    CitaRepository citaRepo = Mockito.mock(CitaRepository.class);
    ConfiguracionCitasService configuracionCitasService = Mockito.mock(ConfiguracionCitasService.class);
    MedicoService medicoService = Mockito.mock(MedicoService.class);
    PacienteService pacienteService = Mockito.mock(PacienteService.class);
    PacienteRepository pacienteRepository = Mockito.mock(PacienteRepository.class);
    UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
    SlotService slotService = new SlotService();
    ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
    AuditoriaService auditoriaService = mock(AuditoriaService.class);
  
    CitaService service = new CitaService(
      citaRepo,
      configuracionCitasService,
      medicoService,
      pacienteService,
      pacienteRepository,
      usuarioService,
      slotService,
      eventPublisher,
      auditoriaService
    );

    Medico medico = new Medico();
    setField(medico, "id", MEDICO_ID);
    setField(medico, "nombres", "Dr X");
    setField(medico, "intervaloMin", 20);
    setField(medico, "activo", true);

    when(configuracionCitasService.getVentanaSemanas()).thenReturn(52);
    when(medicoService.obtenerActivoOFallar(MEDICO_ID)).thenReturn(medico);
    when(medicoService.getDisponibilidadParaFecha(MEDICO_ID, LocalDate.of(2026, 3, 29))).thenReturn(null);

    CrearCitaRequest req = new CrearCitaRequest("1", "A", "B", "300", "HOMBRE", null, null, null, MEDICO_ID,
      LocalDate.of(2026, 3, 29), LocalTime.of(8, 0));

    assertThrows(AppException.class, () -> service.crearManual(req, "user-sub-2", "MANUAL"));
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field f = target.getClass().getDeclaredField(fieldName);
    f.setAccessible(true);
    f.set(target, value);
  }
}
