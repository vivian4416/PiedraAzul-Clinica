package co.piedrazul.api.modules.pacientes;

import java.time.LocalDate;

public record PacienteInput(
  String numDocumento,
  String nombres,
  String apellidos,
  String celular,
  String genero,
  LocalDate fechaNacimiento,
  String email
) {}
