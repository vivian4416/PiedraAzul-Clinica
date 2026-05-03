package co.piedrazul.api.modules.usuarios;

import co.piedrazul.api.integrations.keycloak.dto.KeycloakUserRep;

public record UsuarioResponse(
  String id,
  String login,
  String nombreCompleto,
  String apellido,
  String email,
  String rol,
  boolean activo
) {
  static UsuarioResponse from(KeycloakUserRep user, String rol, String nombreCompleto) {
    boolean enabled = user.enabled() == null || user.enabled();
    return new UsuarioResponse(
      user.id(),
      user.username(),
      nombreCompleto,
      user.lastName() == null ? "" : user.lastName(),
      user.email() == null ? "" : user.email(),
      rol,
      enabled
    );
  }
}