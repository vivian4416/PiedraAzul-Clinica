package co.piedrazul.api.modules.medicos;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicoDisponibilidadRepository extends JpaRepository<MedicoDisponibilidad, Long> {
  Optional<MedicoDisponibilidad> findByMedicoIdAndDiaSemana(Long medicoId, Integer diaSemana);
}
