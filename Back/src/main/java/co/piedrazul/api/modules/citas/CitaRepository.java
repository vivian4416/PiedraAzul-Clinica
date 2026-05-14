package co.piedrazul.api.modules.citas;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaRepository extends JpaRepository<Cita, Long> {
  List<Cita> findByMedicoIdAndFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(
    String medicoId,
    LocalDateTime start,
    LocalDateTime end,
    String estado
  );

  Page<Cita> findByMedicoIdAndFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(
    String medicoId,
    LocalDateTime start,
    LocalDateTime end,
    String estado,
    Pageable pageable
  );

  boolean existsByMedicoIdAndFechaHoraAndEstadoNot(String medicoId, LocalDateTime fechaHora, String estado);

boolean existsByMedicoIdAndFechaHoraAndEstadoNotAndIdNot(
  String medicoId,
  LocalDateTime fechaHora,
  String estado,
  Long id
);
}
