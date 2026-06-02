package co.piedrazul.api.modules.citas.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import co.piedrazul.api.modules.citas.Cita;
import co.piedrazul.api.modules.citas.CitaRepository;

@DataJpaTest
@Transactional
class CitaRepositoryIntegrationTest {
  private static final String MEDICO_ID = "795ee435-a5d2-4817-87b0-11632b46ff4c";

  @Autowired
  private CitaRepository citaRepository;

  @Test
  void guardaCitaConIdYEstadoConfirmada() {
    // Setup
    Cita cita = new Cita();
    cita.setMedicoId(MEDICO_ID);
    cita.setPacienteId(1L);
    cita.setCreadoPor("test-user");
    cita.setFechaHora(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0));
    cita.setEstado("CONFIRMADA");
    cita.setOrigen("MANUAL");

    // Execute
    Cita saved = citaRepository.save(cita);

    // Verify
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getEstado()).isEqualTo("CONFIRMADA");

    // Teardown (transaccion rollback automatico)
  }
}
