package co.piedrazul.api.integrations.keycloak;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import co.piedrazul.api.core.AppException;
import co.piedrazul.api.integrations.keycloak.dto.KeycloakCredentialRep;
import co.piedrazul.api.integrations.keycloak.dto.KeycloakRoleRep;
import co.piedrazul.api.integrations.keycloak.dto.KeycloakUserRep;

@Component
public class KeycloakAdminClient {
  private static final Logger log = LoggerFactory.getLogger(KeycloakAdminClient.class);
  private final RestClient restClient;
  private final KeycloakAdminProperties props;
  private final KeycloakTokenProvider tokenProvider;

  public KeycloakAdminClient(RestClient keycloakRestClient, KeycloakAdminProperties props, KeycloakTokenProvider tokenProvider) {
    this.restClient = keycloakRestClient;
    this.props = props;
    this.tokenProvider = tokenProvider;
  }

  public List<KeycloakUserRep> listUsers(int max) {
    try {
      KeycloakUserRep[] users = restClient.get()
        .uri(uriBuilder -> uriBuilder
          .path("/admin/realms/{realm}/users")
          .queryParam("max", max)
          .build(props.getRealm()))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getAccessToken())
        .retrieve()
        .body(KeycloakUserRep[].class);

      if (users == null) return List.of();
      return Arrays.asList(users);
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "No fue posible listar usuarios en Keycloak");
    }
  }

  /**
   * Busca usuario por username exacto. Retorna null si no existe.
   */
  public KeycloakUserRep findUserByUsernameExact(String username) {
    if (username == null || username.isBlank()) return null;

    try {
      KeycloakUserRep[] users = restClient.get()
        .uri(uriBuilder -> uriBuilder
          .path("/admin/realms/{realm}/users")
          .queryParam("username", username)
          .queryParam("exact", true)
          .build(props.getRealm()))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getAccessToken())
        .retrieve()
        .body(KeycloakUserRep[].class);

      if (users == null || users.length == 0) return null;
      return users[0];
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "No fue posible buscar usuario en Keycloak");
    }
  }

  public KeycloakUserRep getUser(String userId) {
    try {
      return restClient.get()
        .uri("/admin/realms/{realm}/users/{id}", props.getRealm(), userId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getAccessToken())
        .retrieve()
        .body(KeycloakUserRep.class);
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "No fue posible obtener usuario en Keycloak");
    }
  }

  public String createUser(KeycloakUserRep user) {
    ResponseEntity<Void> resp;
    try {
      resp = restClient.post()
        .uri("/admin/realms/{realm}/users", props.getRealm())
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getAccessToken())
        .body(user)
        .retrieve()
        .toBodilessEntity();
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "No fue posible crear usuario en Keycloak");
    }

    if (resp.getStatusCode().value() != 201) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "Keycloak no confirmo la creacion del usuario");
    }

    URI location = resp.getHeaders().getLocation();
    if (location == null) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "Keycloak no devolvio Location del usuario creado");
    }

    String path = location.getPath();
    String id = path.substring(path.lastIndexOf('/') + 1);
    if (id.isBlank()) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "No se pudo inferir el id del usuario creado");
    }
    return id;
  }

  public void updateUser(String userId, Map<String, Object> user) {
    try {
      restClient.put()
        .uri("/admin/realms/{realm}/users/{id}", props.getRealm(), userId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getAccessToken())
        .body(user)
        .retrieve()
        .toBodilessEntity();
    } catch (Exception ex) {
      log.error("[KEYCLOAK] Error al actualizar usuario {} en Keycloak: {} - {}", userId, ex.getClass().getSimpleName(), ex.getMessage(), ex);
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "No fue posible actualizar usuario en Keycloak: " + ex.getMessage());
    }
  }

  public void setEnabled(String userId, boolean enabled) {
    KeycloakUserRep existing = getUser(userId);
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("firstName", existing.firstName());
    payload.put("lastName", existing.lastName());
    payload.put("email", existing.email());
    payload.put("enabled", enabled);
    if (existing.attributes() != null && !existing.attributes().isEmpty()) {
      payload.put("attributes", existing.attributes());
    }
    updateUser(userId, payload);
  }

  public void resetPassword(String userId, String password, boolean temporary) {
    if (password == null || password.isBlank()) return;

    try {
      restClient.put()
        .uri("/admin/realms/{realm}/users/{id}/reset-password", props.getRealm(), userId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getAccessToken())
        .body(new KeycloakCredentialRep("password", password, temporary))
        .retrieve()
        .toBodilessEntity();
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "No fue posible establecer password en Keycloak");
    }
  }

  public KeycloakRoleRep getRealmRoleByName(String roleName) {
    try {
      return restClient.get()
        .uri("/admin/realms/{realm}/roles/{roleName}", props.getRealm(), roleName)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getAccessToken())
        .retrieve()
        .body(KeycloakRoleRep.class);
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "No fue posible obtener rol en Keycloak");
    }
  }

  public List<KeycloakRoleRep> getRealmRoleMappings(String userId) {
    try {
      KeycloakRoleRep[] roles = restClient.get()
        .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", props.getRealm(), userId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getAccessToken())
        .retrieve()
        .body(KeycloakRoleRep[].class);

      if (roles == null) return List.of();
      return Arrays.asList(roles);
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "No fue posible obtener roles del usuario en Keycloak");
    }
  }

  public void replaceRealmRoleMappings(String userId, List<KeycloakRoleRep> roles) {
    try {
      // 1) delete current
      List<KeycloakRoleRep> existing = getRealmRoleMappings(userId);
      if (!existing.isEmpty()) {
        restClient.method(HttpMethod.DELETE)
          .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", props.getRealm(), userId)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getAccessToken())
          .body(existing)
          .retrieve()
          .toBodilessEntity();
      }

      // 2) add desired
      if (roles != null && !roles.isEmpty()) {
        restClient.post()
          .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", props.getRealm(), userId)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getAccessToken())
          .body(roles)
          .retrieve()
          .toBodilessEntity();
      }
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ADMIN_ERROR", "No fue posible asignar roles al usuario en Keycloak");
    }
  }
}
