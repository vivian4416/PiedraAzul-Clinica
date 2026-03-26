package co.piedrazul.api.modules.pacientes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PacienteServiceTest {
  private final PacienteRepository repo = Mockito.mock(PacienteRepository.class);
  private final PacienteService service = new PacienteService(repo);

  @Test
  void retornaExistenteSinCrear() {
    Paciente p = new Paciente();
    p.setNumDocumento("79453201");
    when(repo.findByNumDocumento("79453201")).thenReturn(Optional.of(p));

    FindOrCreateResult result = service.findOrCreate(new PacienteInput(
      "79453201", "Carlos", "Rios", "311", "HOMBRE", null, null
    ));

    assertFalse(result.esNuevo());
    verify(repo, never()).save(Mockito.<Paciente>any());
  }

  @Test
  void creaPacienteCuandoNoExiste() {
    when(repo.findByNumDocumento("123")).thenReturn(Optional.empty());
    when(repo.save(Mockito.any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

    FindOrCreateResult result = service.findOrCreate(new PacienteInput(
      "123", "Ana", "Torres", "300", "MUJER", null, null
    ));

    assertTrue(result.esNuevo());
    verify(repo).save(Mockito.any(Paciente.class));
  }
}
