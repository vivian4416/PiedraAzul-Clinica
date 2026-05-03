package co.piedrazul.api.modules.citas;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaRepository extends JpaRepository<Cita, Long> {
  List<Cita> findByMedicoIdAndFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(
    String medicoId,
    LocalDateTime start,
    LocalDateTime end,
    String estado
  );

  boolean existsByMedicoIdAndFechaHoraAndEstadoNot(String medicoId, LocalDateTime fechaHora, String estado);
}
