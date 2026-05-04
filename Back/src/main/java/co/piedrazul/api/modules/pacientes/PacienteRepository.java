package co.piedrazul.api.modules.pacientes;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
  Optional<Paciente> findByNumDocumento(String numDocumento);

  List<Paciente> findTop5ByNumDocumentoStartingWithOrderByNumDocumentoAsc(String numDocumento);
}
