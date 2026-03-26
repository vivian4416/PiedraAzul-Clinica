package co.piedrazul.api.modules.citas;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record CitaListItemDto(
  Long id,
  LocalTime hora,
  String estado,
  String origen,
  LocalDateTime createdAt,
  Long pacienteId,
  String pacienteNombre,
  String pacienteDocumento,
  String pacienteCelular
) {}
