package co.piedrazul.api.modules.auth;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record RegisterPacienteRequest(
  @NotBlank(message = "documento es obligatorio") String documento,
  @NotBlank(message = "password es obligatorio") String password,
  @NotBlank(message = "nombres es obligatorio") String nombres,
  @NotBlank(message = "apellidos es obligatorio") String apellidos,
  @NotBlank(message = "celular es obligatorio") String celular,
  @NotBlank(message = "genero es obligatorio") String genero,
  LocalDate fechaNacimiento,
  String email
) {}
