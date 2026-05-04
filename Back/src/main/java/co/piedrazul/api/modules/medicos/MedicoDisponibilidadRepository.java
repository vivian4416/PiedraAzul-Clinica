package co.piedrazul.api.modules.medicos;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicoDisponibilidadRepository extends JpaRepository<MedicoDisponibilidad, Long> {
  Optional<MedicoDisponibilidad> findByMedicoIdAndDiaSemana(String medicoId, Integer diaSemana);
  List<MedicoDisponibilidad> findByMedicoIdInOrderByMedicoIdAscDiaSemanaAsc(List<String> medicoIds);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("delete from MedicoDisponibilidad d where d.medicoId = :medicoId")
  void deleteByMedicoId(@Param("medicoId") String medicoId);
}
