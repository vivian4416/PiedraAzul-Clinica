package co.piedrazul.api.modules.pacientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PacienteServiceTest {
  private final FakeRepositoryState state = new FakeRepositoryState();
  private final PacienteRepository repo = createRepositoryProxy(state);
  private final PacienteService service = new PacienteService(repo);

  @Test
  void retornaExistenteSinCrear() {
    Paciente paciente = new Paciente();
    paciente.setNumDocumento("79453201");
    state.findByNumDocumentoResult = Optional.of(paciente);

    FindOrCreateResult result = service.findOrCreate(new PacienteInput(
      "79453201", "Carlos", "Rios", "3111234567", "HOMBRE", null, null
    ));

    assertFalse(result.esNuevo());
    assertEquals(0, state.saveCount);
  }

  @Test
  void creaPacienteCuandoNoExiste() {
    state.findByNumDocumentoResult = Optional.empty();

    FindOrCreateResult result = service.findOrCreate(new PacienteInput(
      "123", "Ana", "Torres", "3001234567", "MUJER", null, null
    ));

    assertTrue(result.esNuevo());
    assertEquals(1, state.saveCount);
  }

  @Test
  void normalizaDocumentoYEvitaDuplicadosConEspacios() {
    Paciente paciente = new Paciente();
    paciente.setNumDocumento("12345678");
    state.findByNumDocumentoResult = Optional.of(paciente);

    FindOrCreateResult result = service.findOrCreate(new PacienteInput(
      " 12345678 ", "Ana", "Torres", " 3001234567 ", "MUJER", null, ""
    ));

    assertFalse(result.esNuevo());
    assertEquals(0, state.saveCount);
  }

  @Test
  void retornaSugerenciasPorPrefijoDeDocumento() {
    Paciente paciente = new Paciente();
    paciente.setNumDocumento("12345678");
    paciente.setNombres("Ana");
    paciente.setApellidos("Torres");
    paciente.setCelular("3001234567");
    paciente.setGenero("MUJER");
    state.sugerencias.put("123", List.of(paciente));

    List<PacienteAutocompleteDto> sugerencias = service.sugerenciasPorDocumento(" 123 ");

    assertEquals(1, sugerencias.size());
    assertEquals("12345678", sugerencias.get(0).numDocumento());
  }

  private static PacienteRepository createRepositoryProxy(FakeRepositoryState state) {
    InvocationHandler handler = (Object proxy, Method method, Object[] args) -> {
      return switch (method.getName()) {
        case "findByNumDocumento" -> state.findByNumDocumentoResult;
        case "findTop5ByNumDocumentoStartingWithOrderByNumDocumentoAsc" -> state.sugerencias.getOrDefault((String) args[0], List.of());
        case "save" -> {
          state.saveCount++;
          yield args[0];
        }
        case "toString" -> "FakePacienteRepository";
        case "hashCode" -> System.identityHashCode(proxy);
        case "equals" -> proxy == args[0];
        default -> throw new UnsupportedOperationException("Method not needed in test: " + method.getName());
      };
    };

    return (PacienteRepository) Proxy.newProxyInstance(
      PacienteRepository.class.getClassLoader(),
      new Class<?>[] {PacienteRepository.class},
      handler
    );
  }

  private static final class FakeRepositoryState {
    Optional<Paciente> findByNumDocumentoResult = Optional.empty();
    final Map<String, List<Paciente>> sugerencias = new HashMap<>();
    int saveCount = 0;
  }
}
