package co.piedrazul.api.modules.citas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;

public record CrearCitaRequest(
  @NotBlank @Pattern(regexp = "^\\d+$", message = "numDocumento debe contener solo numeros") String numDocumento,
  @NotBlank String nombres,
  @NotBlank String apellidos,
  @NotBlank @Pattern(regexp = "^\\d{10}$", message = "celular debe tener exactamente 10 digitos") String celular,
  @NotBlank @Pattern(regexp = "^(HOMBRE|MUJER|OTRO)$", message = "genero invalido") String genero,
  LocalDate fechaNacimiento,
  String email,
  @NotBlank String medicoId,
  @NotNull LocalDate fecha,
  @NotNull LocalTime hora
) {}
