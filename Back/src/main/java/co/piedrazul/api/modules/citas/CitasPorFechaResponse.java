package co.piedrazul.api.modules.citas;

import java.time.LocalDate;
import java.util.List;

public record CitasPorFechaResponse(
  Long medicoId,
  String medicoNombre,
  LocalDate fecha,
  int total,
  int disponibles,
  List<CitaListItemDto> citas,
  List<SlotDto> slots
) {}
