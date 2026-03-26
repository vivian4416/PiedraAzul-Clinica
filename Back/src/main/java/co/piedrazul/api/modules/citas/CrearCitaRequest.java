package co.piedrazul.api.modules.citas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CrearCitaRequest(
  @NotBlank String numDocumento,
  @NotBlank String nombres,
  @NotBlank String apellidos,
  @NotBlank String celular,
  @NotBlank String genero,
  LocalDate fechaNacimiento,
  String email,
  @NotNull Long medicoId,
  @NotNull LocalDate fecha,
  @NotNull LocalTime hora
) {}
