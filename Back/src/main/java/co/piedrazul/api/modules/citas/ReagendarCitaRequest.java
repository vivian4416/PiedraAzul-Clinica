package co.piedrazul.api.modules.citas;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReagendarCitaRequest(
  @NotBlank String medicoId,
  @NotNull @FutureOrPresent LocalDate fecha,
  @NotNull LocalTime hora
) {}