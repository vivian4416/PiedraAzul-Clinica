package co.piedrazul.api.modules.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SlotServiceTest {
  private final SlotService slotService = new SlotService();

  @Test
  void generaSlotsConIntervaloCorrecto() {
    List<SlotDto> slots = slotService.calcularSlotsDisponibles(
      LocalTime.of(8, 0),
      LocalTime.of(9, 0),
      20,
      List.of()
    );

    assertEquals(3, slots.size());
    assertEquals(LocalTime.of(8, 0), slots.get(0).hora());
    assertEquals(LocalTime.of(8, 40), slots.get(2).hora());
  }

  @Test
  void marcaSlotsOcupados() {
    List<SlotDto> slots = slotService.calcularSlotsDisponibles(
      LocalTime.of(8, 0),
      LocalTime.of(9, 0),
      20,
      List.of(LocalTime.of(8, 20))
    );

    assertTrue(slots.get(0).disponible());
    assertFalse(slots.get(1).disponible());
  }

  @Test
  void validaSlotLibre() {
    SlotService.ValidationResult result = slotService.validarSlot(
      LocalTime.of(8, 0),
      LocalTime.of(8, 0),
      LocalTime.of(9, 0),
      20,
      List.of(LocalTime.of(8, 20))
    );

    assertTrue(result.valido());
  }
}
