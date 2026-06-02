package co.piedrazul.api.modules.citas.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.piedrazul.api.modules.citas.CrearCitaRequest;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
  "spring.datasource.url=jdbc:h2:mem:piedrazul_e2e;MODE=MySQL;DB_CLOSE_DELAY=0;DB_CLOSE_ON_EXIT=FALSE",
  "spring.sql.init.mode=always"
})
@Transactional
class AgendamientoE2ETest {
  private static final String MEDICO_ID = "795ee435-a5d2-4817-87b0-11632b46ff4c";
  private static final String DOCUMENTO = "91000001";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void agendamientoAutonomoGuardaYListaEnAgendaMedico() throws Exception {
    // Setup
    LocalDate fecha = siguienteDiaDisponible();
    LocalTime hora = LocalTime.of(8, 0);
    CrearCitaRequest request = new CrearCitaRequest(
      DOCUMENTO,
      "Sofia",
      "Lopez",
      "3009876543",
      "MUJER",
      LocalDate.of(1995, 5, 12),
      "sofia.lopez@correo.com",
      null,
      MEDICO_ID,
      fecha,
      hora
    );

    // Execute
    mockMvc.perform(post("/api/v1/citas/autonoma")
        .with(user("paciente").roles("PACIENTE"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk());

    MvcResult listResult = mockMvc.perform(get("/api/v1/citas")
        .with(user("agendador").roles("AGENDADOR"))
        .param("medicoId", MEDICO_ID)
        .param("fecha", fecha.toString()))
      .andExpect(status().isOk())
      .andReturn();

    // Verify
    Map<String, Object> payload = objectMapper.readValue(
      listResult.getResponse().getContentAsString(),
      new TypeReference<Map<String, Object>>() {}
    );
    Map<String, Object> data = castMap(payload.get("data"));
    List<Map<String, Object>> citas = castList(data.get("citas"));

    boolean found = citas.stream().anyMatch(cita ->
      "AUTONOMA".equals(cita.get("origen")) && DOCUMENTO.equals(cita.get("pacienteDocumento"))
    );

    assertThat(found).isTrue();

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

  @SuppressWarnings("unchecked")
  private Map<String, Object> castMap(Object value) {
    return (Map<String, Object>) value;
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> castList(Object value) {
    return (List<Map<String, Object>>) value;
  }
}
