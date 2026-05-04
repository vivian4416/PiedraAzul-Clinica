package co.piedrazul.api.modules.usuarios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioCreateRequest(
  @NotBlank String login,
  @NotBlank String password,
  @NotBlank String nombreCompleto,
  @NotBlank String apellido,
  @Email @NotBlank String email,
  @NotBlank String rol,
  @NotNull Boolean activo,
  String documento,
  String celular
) {}