package co.piedrazul.api.modules.citas;

import co.piedrazul.api.core.AppException;
import co.piedrazul.api.modules.medicos.Medico;
import co.piedrazul.api.modules.medicos.MedicoDisponibilidad;
import co.piedrazul.api.modules.medicos.MedicoService;
import co.piedrazul.api.modules.pacientes.FindOrCreateResult;
import co.piedrazul.api.modules.pacientes.Paciente;
import co.piedrazul.api.modules.pacientes.PacienteInput;
import co.piedrazul.api.modules.pacientes.PacienteRepository;
import co.piedrazul.api.modules.pacientes.PacienteService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CitaService {
  private final CitaRepository citaRepository;
  private final MedicoService medicoService;
  private final PacienteService pacienteService;
  private final PacienteRepository pacienteRepository;
  private final SlotService slotService;
  private final ApplicationEventPublisher eventPublisher;

  public CitaService(CitaRepository citaRepository,
                     MedicoService medicoService,
                     PacienteService pacienteService,
                     PacienteRepository pacienteRepository,
                     SlotService slotService,
                     ApplicationEventPublisher eventPublisher) {
    this.citaRepository = citaRepository;
    this.medicoService = medicoService;
    this.pacienteService = pacienteService;
    this.pacienteRepository = pacienteRepository;
    this.slotService = slotService;
    this.eventPublisher = eventPublisher;
  }

  public CitasPorFechaResponse listarPorMedicoYFecha(Long medicoId, LocalDate fecha) {
    Medico medico = medicoService.obtenerActivoOFallar(medicoId);
    LocalDateTime start = fecha.atStartOfDay();
    LocalDateTime end = fecha.plusDays(1).atStartOfDay();

    List<Cita> citas = citaRepository.findByMedicoIdAndFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(
      medicoId, start, end, "CANCELADA"
    );

    MedicoDisponibilidad disp = medicoService.getDisponibilidadParaFecha(medicoId, fecha);
    List<SlotDto> slots = List.of();
    if (disp != null) {
      List<LocalTime> ocupados = citas.stream().map(c -> c.getFechaHora().toLocalTime()).toList();
      slots = slotService.calcularSlotsDisponibles(disp.getHoraInicio(), disp.getHoraFin(), medico.getIntervaloMin(), ocupados);
    }

    Set<Long> pacienteIds = citas.stream().map(Cita::getPacienteId).collect(Collectors.toSet());
    Map<Long, Paciente> pacientes = pacienteRepository.findAllById(pacienteIds)
      .stream()
      .collect(Collectors.toMap(Paciente::getId, p -> p));

    List<CitaListItemDto> items = citas.stream()
      .map(c -> {
        Paciente paciente = pacientes.get(c.getPacienteId());
        String pacienteNombre = paciente == null
          ? "Paciente #" + c.getPacienteId()
          : (paciente.getNombres() + " " + paciente.getApellidos()).trim();

        return new CitaListItemDto(
          c.getId(),
          c.getFechaHora().toLocalTime(),
          c.getEstado(),
          c.getOrigen(),
          null,
          c.getPacienteId(),
          pacienteNombre,
          paciente == null ? "" : paciente.getNumDocumento(),
          paciente == null ? "" : paciente.getCelular()
        );
      })
      .toList();

    int disponibles = (int) slots.stream().filter(SlotDto::disponible).count();

    return new CitasPorFechaResponse(medicoId, medico.getNombres(), fecha, items.size(), disponibles, items, slots);
  }

  @Transactional
  public CitaCreadaResponse crearManual(CrearCitaRequest req, Long creadoPor, String origen) {
    Medico medico = medicoService.obtenerActivoOFallar(req.medicoId());
    MedicoDisponibilidad disp = medicoService.getDisponibilidadParaFecha(req.medicoId(), req.fecha());

    if (disp == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "El medico no atiende en la fecha seleccionada");
    }

    LocalDateTime fechaHora = LocalDateTime.of(req.fecha(), req.hora());
    LocalDateTime start = req.fecha().atStartOfDay();
    LocalDateTime end = req.fecha().plusDays(1).atStartOfDay();
    List<Cita> citasDia = citaRepository.findByMedicoIdAndFechaHoraBetweenAndEstadoNotOrderByFechaHoraAsc(
      req.medicoId(), start, end, "CANCELADA"
    );
    List<LocalTime> ocupados = citasDia.stream().map(c -> c.getFechaHora().toLocalTime()).toList();

    SlotService.ValidationResult valid = slotService.validarSlot(req.hora(), disp.getHoraInicio(), disp.getHoraFin(), medico.getIntervaloMin(), ocupados);
    if (!valid.valido()) {
      throw new AppException(HttpStatus.CONFLICT, "CONFLICT", valid.razon());
    }

    FindOrCreateResult paciente = pacienteService.findOrCreate(new PacienteInput(
      req.numDocumento(), req.nombres(), req.apellidos(), req.celular(), req.genero(), req.fechaNacimiento(), req.email()
    ));

    Cita cita = new Cita();
    cita.setMedicoId(req.medicoId());
    cita.setPacienteId(paciente.paciente().getId());
    cita.setCreadoPor(creadoPor);
    cita.setFechaHora(fechaHora);
    cita.setEstado("CONFIRMADA");
    cita.setOrigen(origen == null ? "MANUAL" : origen);

    try {
      cita = citaRepository.save(cita);
    } catch (DataIntegrityViolationException ex) {
      throw new AppException(HttpStatus.CONFLICT, "CONFLICT", "El slot fue tomado en este momento, intenta otro");
    }

    eventPublisher.publishEvent(new CitaCreadaEvent(cita.getId(), cita.getMedicoId(), creadoPor, paciente.esNuevo()));
    return new CitaCreadaResponse(cita.getId(), paciente.esNuevo());
  }
}
