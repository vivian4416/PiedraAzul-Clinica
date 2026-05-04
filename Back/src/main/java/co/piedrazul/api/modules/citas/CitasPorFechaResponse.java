package co.piedrazul.api.modules.citas;

import java.time.LocalDate;
import java.util.List;

public record CitasPorFechaResponse(
  String medicoId,
  String medicoNombre,
  LocalDate fecha,
  int total,
  long totalRegistros,
  int pagina,
  int tamanio,
  int totalPaginas,
  int disponibles,
  List<CitaListItemDto> citas,
  List<SlotDto> slots
) {}
