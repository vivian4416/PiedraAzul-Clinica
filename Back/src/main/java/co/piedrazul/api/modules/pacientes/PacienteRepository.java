package co.piedrazul.api.modules.pacientes;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
  Optional<Paciente> findByNumDocumento(String numDocumento);
}
