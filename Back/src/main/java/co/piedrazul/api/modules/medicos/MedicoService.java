package co.piedrazul.api.modules.medicos;

import co.piedrazul.api.core.AppException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MedicoService {
  private final MedicoRepository medicoRepository;
  private final MedicoDisponibilidadRepository disponibilidadRepository;

  public MedicoService(MedicoRepository medicoRepository, MedicoDisponibilidadRepository disponibilidadRepository) {
    this.medicoRepository = medicoRepository;
    this.disponibilidadRepository = disponibilidadRepository;
  }

  public List<Medico> listarActivos() {
    return medicoRepository.findByActivoTrueOrderByNombresAsc();
  }

  public Medico obtenerActivoOFallar(Long id) {
    Medico medico = medicoRepository.findById(id)
      .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Medico no encontrado"));

    if (!medico.isActivo()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "El medico esta inactivo");
    }
    return medico;
  }

  public MedicoDisponibilidad getDisponibilidadParaFecha(Long medicoId, LocalDate fecha) {
    DayOfWeek day = fecha.getDayOfWeek();
    int dia = day.getValue();
    if (dia > 5) {
      return null;
    }
    return disponibilidadRepository.findByMedicoIdAndDiaSemana(medicoId, dia).orElse(null);
  }
}
