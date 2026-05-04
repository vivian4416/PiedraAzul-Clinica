package co.piedrazul.api.modules.usuarios;

import java.util.List;
import java.util.Map;

import co.piedrazul.api.integrations.keycloak.dto.KeycloakUserRep;

public record UsuarioResponse(
  String id,
  String login,
  String nombreCompleto,
  String apellido,
  String email,
  String rol,
  boolean activo,
  String documento,
  String celular
) {
  static UsuarioResponse from(KeycloakUserRep user, String rol, String nombreCompleto) {
    boolean enabled = user.enabled() == null || user.enabled();
    Map<String, Object> attrs = user.attributes() == null ? Map.of() : user.attributes();
    return new UsuarioResponse(
      user.id(),
      user.username(),
      nombreCompleto,
      user.lastName() == null ? "" : user.lastName(),
      user.email() == null ? "" : user.email(),
      rol,
      enabled,
      extractAttr(attrs, "documento"),
      extractAttr(attrs, "celular")
    );
  }

  private static String extractAttr(Map<String, Object> attrs, String key) {
    Object val = attrs.get(key);
    if (val instanceof List<?> list && !list.isEmpty()) {
      return String.valueOf(list.get(0));
    }
    if (val instanceof String s) return s;
    return null;
  }
}
