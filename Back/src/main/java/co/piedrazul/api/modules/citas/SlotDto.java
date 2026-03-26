package co.piedrazul.api.modules.citas;

import java.time.LocalTime;

public record SlotDto(LocalTime hora, boolean disponible) {}
