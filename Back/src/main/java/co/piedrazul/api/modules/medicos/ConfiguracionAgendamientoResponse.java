package co.piedrazul.api.modules.medicos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;

public record ConfiguracionAgendamientoResponse(
  Integer ventanaSemanas,
  List<MedicoConfiguracionResponse> medicos
) {}

record MedicoConfiguracionResponse(
  Long id,
  String nombres,
  String tipo,
  String especialidad,
  boolean activo,
  Integer intervaloMin,
  List<DisponibilidadDiaResponse> disponibilidad
) {}

record DisponibilidadDiaResponse(
  Integer diaSemana,
  LocalTime horaInicio,
  LocalTime horaFin
) {}

record GuardarConfiguracionAgendamientoRequest(
  @NotNull @Min(1) @Max(12) Integer ventanaSemanas,
  @NotNull @Size(min = 1) List<@Valid MedicoConfiguracionUpdateRequest> medicos
) {}

record MedicoConfiguracionUpdateRequest(
  @NotNull Long id,
  @NotNull @Min(5) @Max(120) Integer intervaloMin,
  @NotNull Boolean activo,
  @NotNull List<@Valid DisponibilidadDiaUpdateRequest> disponibilidad
) {}

record DisponibilidadDiaUpdateRequest(
  @NotNull @Min(1) @Max(7) Integer diaSemana,
  @NotNull LocalTime horaInicio,
  @NotNull LocalTime horaFin
) {}
