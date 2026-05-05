package co.piedrazul.api.modules.medicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.piedrazul.api.core.AppException;
import co.piedrazul.api.modules.citas.ConfiguracionCitasService;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class MedicoServiceTest {
  @Test
  void guardarConfiguracionAgendamientoRechazaDiasRepetidos() {
    MedicoRepository medicoRepository = mock(MedicoRepository.class);
    MedicoDisponibilidadRepository disponibilidadRepository = mock(MedicoDisponibilidadRepository.class);
    ConfiguracionCitasService configuracionCitasService = mock(ConfiguracionCitasService.class);
    MedicoService service = new MedicoService(medicoRepository, disponibilidadRepository, configuracionCitasService);

    Medico medico = medico("m1", "Dr. Uno");
    when(medicoRepository.findAllById(List.of("m1"))).thenReturn(List.of(medico));

    GuardarConfiguracionAgendamientoRequest request = new GuardarConfiguracionAgendamientoRequest(
      6,
      List.of(
        new MedicoConfiguracionUpdateRequest(
          "m1",
          30,
          true,
          List.of(
            new DisponibilidadDiaUpdateRequest(1, LocalTime.of(8, 0), LocalTime.of(12, 0)),
            new DisponibilidadDiaUpdateRequest(1, LocalTime.of(13, 0), LocalTime.of(17, 0))
          )
        )
      )
    );

    AppException ex = assertThrows(AppException.class, () -> service.guardarConfiguracionAgendamiento(request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    assertEquals("BAD_REQUEST", ex.getCode());
  }

  @Test
  void guardarConfiguracionAgendamientoRechazaHorarioInvalido() {
    MedicoRepository medicoRepository = mock(MedicoRepository.class);
    MedicoDisponibilidadRepository disponibilidadRepository = mock(MedicoDisponibilidadRepository.class);
    ConfiguracionCitasService configuracionCitasService = mock(ConfiguracionCitasService.class);
    MedicoService service = new MedicoService(medicoRepository, disponibilidadRepository, configuracionCitasService);

    Medico medico = medico("m1", "Dr. Uno");
    when(medicoRepository.findAllById(List.of("m1"))).thenReturn(List.of(medico));

    GuardarConfiguracionAgendamientoRequest request = new GuardarConfiguracionAgendamientoRequest(
      6,
      List.of(
        new MedicoConfiguracionUpdateRequest(
          "m1",
          30,
          true,
          List.of(new DisponibilidadDiaUpdateRequest(1, LocalTime.of(12, 0), LocalTime.of(8, 0)))
        )
      )
    );

    AppException ex = assertThrows(AppException.class, () -> service.guardarConfiguracionAgendamiento(request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    assertEquals("BAD_REQUEST", ex.getCode());
  }

  private static Medico medico(String id, String nombres) {
    Medico medico = new Medico();
    medico.setId(id);
    medico.setNombres(nombres);
    medico.setTipo("MEDICO");
    medico.setEspecialidad("GENERAL");
    medico.setIntervaloMin(30);
    medico.setActivo(true);
    return medico;
  }
}

