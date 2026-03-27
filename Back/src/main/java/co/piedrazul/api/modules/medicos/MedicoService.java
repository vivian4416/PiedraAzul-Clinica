package co.piedrazul.api.modules.medicos;

import co.piedrazul.api.modules.citas.ConfiguracionCitasService;
import co.piedrazul.api.core.AppException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicoService {
  private final MedicoRepository medicoRepository;
  private final MedicoDisponibilidadRepository disponibilidadRepository;
  private final ConfiguracionCitasService configuracionCitasService;

  public MedicoService(MedicoRepository medicoRepository,
                       MedicoDisponibilidadRepository disponibilidadRepository,
                       ConfiguracionCitasService configuracionCitasService) {
    this.medicoRepository = medicoRepository;
    this.disponibilidadRepository = disponibilidadRepository;
    this.configuracionCitasService = configuracionCitasService;
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

  public ConfiguracionAgendamientoResponse obtenerConfiguracionAgendamiento() {
    int ventanaSemanas = configuracionCitasService.getVentanaSemanas();
    List<Medico> medicos = medicoRepository.findAllByOrderByNombresAsc();
    List<Long> medicoIds = medicos.stream().map(Medico::getId).toList();

    if (medicoIds.isEmpty()) {
      return new ConfiguracionAgendamientoResponse(ventanaSemanas, List.of());
    }

    Map<Long, List<DisponibilidadDiaResponse>> disponibilidadPorMedico = disponibilidadRepository
      .findByMedicoIdInOrderByMedicoIdAscDiaSemanaAsc(medicoIds)
      .stream()
      .collect(Collectors.groupingBy(
        MedicoDisponibilidad::getMedicoId,
        Collectors.mapping(
          d -> new DisponibilidadDiaResponse(d.getDiaSemana(), d.getHoraInicio(), d.getHoraFin()),
          Collectors.toList()
        )
      ));

    List<MedicoConfiguracionResponse> medicosResponse = medicos.stream()
      .map(m -> new MedicoConfiguracionResponse(
        m.getId(),
        m.getNombres(),
        m.getTipo(),
        m.getEspecialidad(),
        m.isActivo(),
        m.getIntervaloMin(),
        disponibilidadPorMedico.getOrDefault(m.getId(), List.of())
      ))
      .toList();

    return new ConfiguracionAgendamientoResponse(ventanaSemanas, medicosResponse);
  }

  @Transactional
  public ConfiguracionAgendamientoResponse guardarConfiguracionAgendamiento(GuardarConfiguracionAgendamientoRequest request) {
    configuracionCitasService.actualizarVentanaSemanas(request.ventanaSemanas());

    List<Long> ids = request.medicos().stream().map(MedicoConfiguracionUpdateRequest::id).toList();
    Map<Long, Medico> medicosDb = medicoRepository.findAllById(ids)
      .stream()
      .collect(Collectors.toMap(Medico::getId, Function.identity()));

    if (medicosDb.size() != ids.size()) {
      throw new AppException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Uno o más médicos no existen");
    }

    for (MedicoConfiguracionUpdateRequest medicoRequest : request.medicos()) {
      Medico medico = medicosDb.get(medicoRequest.id());
      validarDisponibilidad(medicoRequest.disponibilidad());

      medico.setIntervaloMin(medicoRequest.intervaloMin());
      medico.setActivo(Boolean.TRUE.equals(medicoRequest.activo()));
      medicoRepository.save(medico);

      disponibilidadRepository.deleteByMedicoId(medicoRequest.id());
      disponibilidadRepository.flush();

      List<MedicoDisponibilidad> nuevasDisponibilidades = medicoRequest.disponibilidad().stream()
        .map(d -> {
          MedicoDisponibilidad disponibilidad = new MedicoDisponibilidad();
          disponibilidad.setMedicoId(medicoRequest.id());
          disponibilidad.setDiaSemana(d.diaSemana());
          disponibilidad.setHoraInicio(d.horaInicio());
          disponibilidad.setHoraFin(d.horaFin());
          return disponibilidad;
        })
        .toList();

      if (!nuevasDisponibilidades.isEmpty()) {
        disponibilidadRepository.saveAll(nuevasDisponibilidades);
      }
    }

    return obtenerConfiguracionAgendamiento();
  }

  private void validarDisponibilidad(List<DisponibilidadDiaUpdateRequest> disponibilidad) {
    Set<Integer> dias = new HashSet<>();
    for (DisponibilidadDiaUpdateRequest dia : disponibilidad) {
      if (!dias.add(dia.diaSemana())) {
        throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "No se pueden repetir dias en la disponibilidad");
      }

      if (!dia.horaInicio().isBefore(dia.horaFin())) {
        throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "La hora de inicio debe ser menor a la hora fin");
      }
    }
  }
}
