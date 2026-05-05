package co.piedrazul.api.modules.usuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import co.piedrazul.api.core.AppException;
import co.piedrazul.api.integrations.keycloak.KeycloakAdminClient;
import co.piedrazul.api.integrations.keycloak.dto.KeycloakRoleRep;
import co.piedrazul.api.integrations.keycloak.dto.KeycloakUserRep;
import co.piedrazul.api.modules.auth.RegisterPacienteRequest;
import co.piedrazul.api.modules.medicos.MedicoRepository;
import co.piedrazul.api.modules.pacientes.FindOrCreateResult;
import co.piedrazul.api.modules.pacientes.PacienteInput;
import co.piedrazul.api.modules.pacientes.PacienteService;

class UsuarioServiceTest {
  @Test
  void registrarPacienteCreaUsuarioKeycloakYSincronizaPaciente() {
    KeycloakAdminClient keycloak = mock(KeycloakAdminClient.class);
    MedicoRepository medicoRepository = mock(MedicoRepository.class);
    PacienteService pacienteService = mock(PacienteService.class);
    UsuarioService service = new UsuarioService(keycloak, medicoRepository, pacienteService);

    RegisterPacienteRequest request = new RegisterPacienteRequest(
      " 12345678 ",
      "  Secr3t!  ",
      " Ana Maria ",
      " Torres Lopez ",
      " 3001234567 ",
      " Mujer ",
      LocalDate.of(1990, 5, 20),
      " ana@example.com "
    );

    KeycloakUserRep createdUser = new KeycloakUserRep(
      "kc-1",
      "12345678",
      "Ana Maria",
      "Torres Lopez",
      "ana@example.com",
      true,
      Map.of(
        "documento", List.of("12345678"),
        "celular", List.of("3001234567"),
        "genero", List.of("MUJER")
      )
    );

    when(keycloak.findUserByUsernameExact("12345678")).thenReturn(null);
    when(keycloak.createUser(any(KeycloakUserRep.class))).thenReturn("kc-1");
    when(keycloak.getRealmRoleByName("paciente")).thenReturn(new KeycloakRoleRep("role-1", "paciente", false, false, "realm"));
    when(keycloak.getUser("kc-1")).thenReturn(createdUser);
    when(pacienteService.findOrCreate(any(PacienteInput.class))).thenReturn(new FindOrCreateResult(null, true));

    UsuarioResponse response = service.registrarPaciente(request);

    assertEquals("kc-1", response.id());
    assertEquals("12345678", response.login());
    assertEquals("Ana Maria Torres Lopez", response.nombreCompleto());
    assertEquals("PACIENTE", response.rol());
    assertEquals("12345678", response.documento());
    assertEquals("3001234567", response.celular());
    verify(keycloak).resetPassword("kc-1", "Secr3t!", false);
    verify(keycloak).replaceRealmRoleMappings("kc-1", List.of(new KeycloakRoleRep("role-1", "paciente", false, false, "realm")));
    verify(pacienteService).findOrCreate(any(PacienteInput.class));
  }

  @Test
  void registrarPacienteFallaSiYaExisteUsuarioConEseDocumento() {
    KeycloakAdminClient keycloak = mock(KeycloakAdminClient.class);
    MedicoRepository medicoRepository = mock(MedicoRepository.class);
    PacienteService pacienteService = mock(PacienteService.class);
    UsuarioService service = new UsuarioService(keycloak, medicoRepository, pacienteService);

    RegisterPacienteRequest request = new RegisterPacienteRequest(
      "12345678",
      "Secr3t!",
      "Ana",
      "Torres",
      "3001234567",
      "Mujer",
      LocalDate.of(1990, 5, 20),
      "ana@example.com"
    );

    when(keycloak.findUserByUsernameExact("12345678")).thenReturn(new KeycloakUserRep("kc-1", "12345678", null, null, null, true, null));

    AppException ex = assertThrows(AppException.class, () -> service.registrarPaciente(request));

    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    assertEquals("CONFLICT", ex.getCode());
    verify(keycloak, never()).createUser(any());
    verify(pacienteService, never()).findOrCreate(any());
  }
}


