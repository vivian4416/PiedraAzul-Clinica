package co.piedrazul.api.modules.citas.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.piedrazul.api.modules.citas.CrearCitaRequest;
import co.piedrazul.api.modules.pacientes.Paciente;
import co.piedrazul.api.modules.pacientes.PacienteRepository;
import co.piedrazul.api.modules.usuarios.UsuarioService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CitaControllerComponentTest {
  private static final String MEDICO_ID = "795ee435-a5d2-4817-87b0-11632b46ff4c";
  private static final String DOCUMENTO = "90000001";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private PacienteRepository pacienteRepository;

  @MockBean
  private UsuarioService usuarioService;

  @Test
  void creaCitaManualYRetornaPacienteCorrecto() throws Exception {
    // Setup
    LocalDate fecha = siguienteDiaDisponible();
    LocalTime hora = LocalTime.of(8, 0);
    CrearCitaRequest request = new CrearCitaRequest(
      DOCUMENTO,
      "Laura",
      "Perez",
      "3001234567",
      "MUJER",
      LocalDate.of(1990, 1, 10),
      "laura.perez@correo.com",
      "Secret123*",
      MEDICO_ID,
      fecha,
      hora
    );

    // Execute
    mockMvc.perform(post("/api/v1/citas")
        .with(user("agendador").roles("AGENDADOR"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.ok").value(true))
      .andExpect(jsonPath("$.data.citaId").isNumber())
      .andExpect(jsonPath("$.data.esNuevoPaciente").value(true));

    // Verify
    Paciente paciente = pacienteRepository.findByNumDocumento(DOCUMENTO).orElseThrow();
    assertThat(paciente.getNombres()).isEqualTo("Laura");
    assertThat(paciente.getApellidos()).isEqualTo("Perez");
    assertThat(paciente.getCelular()).isEqualTo("3001234567");

    // Teardown (transaccion rollback automatico)
  }

  private LocalDate siguienteDiaDisponible() {
    LocalDate cursor = LocalDate.now().plusDays(1);
    for (int i = 0; i < 28; i++) {
      DayOfWeek day = cursor.getDayOfWeek();
      if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
        return cursor;
      }
      cursor = cursor.plusDays(1);
    }
    return LocalDate.now().plusDays(1);
  }
}
