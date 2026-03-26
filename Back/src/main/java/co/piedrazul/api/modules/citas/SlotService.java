package co.piedrazul.api.modules.citas;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SlotService {

  public List<SlotDto> calcularSlotsDisponibles(LocalTime horaInicio,
                                                LocalTime horaFin,
                                                int intervaloMin,
                                                List<LocalTime> ocupados) {
    if (intervaloMin < 5 || intervaloMin > 120) {
      throw new IllegalArgumentException("intervaloMin debe estar entre 5 y 120");
    }

    Set<LocalTime> ocupadosSet = new HashSet<>(ocupados);
    List<SlotDto> slots = new ArrayList<>();
    LocalTime cursor = horaInicio;

    while (cursor.isBefore(horaFin)) {
      slots.add(new SlotDto(cursor, !ocupadosSet.contains(cursor)));
      cursor = cursor.plusMinutes(intervaloMin);
    }

    return slots;
  }

  public ValidationResult validarSlot(LocalTime hora,
                                      LocalTime horaInicio,
                                      LocalTime horaFin,
                                      int intervaloMin,
                                      List<LocalTime> ocupados) {
    List<SlotDto> slots = calcularSlotsDisponibles(horaInicio, horaFin, intervaloMin, ocupados);
    SlotDto target = slots.stream().filter(s -> s.hora().equals(hora)).findFirst().orElse(null);

    if (target == null) {
      return new ValidationResult(false, "La hora no corresponde a un slot valido del medico");
    }
    if (!target.disponible()) {
      return new ValidationResult(false, "El slot ya esta ocupado");
    }
    return new ValidationResult(true, null);
  }

  public record ValidationResult(boolean valido, String razon) {}
}
