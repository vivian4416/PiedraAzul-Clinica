package co.piedrazul.api.integrations.keycloak;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import co.piedrazul.api.core.AppException;

@Component
public class KeycloakTokenProvider {
  private final RestClient restClient;
  private final KeycloakAdminProperties props;

  private volatile String cachedToken;
  private volatile Instant cachedTokenExpiresAt;

  public KeycloakTokenProvider(RestClient keycloakRestClient, KeycloakAdminProperties props) {
    this.restClient = keycloakRestClient;
    this.props = props;
  }

  public String getAccessToken() {
    Instant now = Instant.now();
    if (cachedToken != null && cachedTokenExpiresAt != null && now.isBefore(cachedTokenExpiresAt.minusSeconds(15))) {
      return cachedToken;
    }

    synchronized (this) {
      now = Instant.now();
      if (cachedToken != null && cachedTokenExpiresAt != null && now.isBefore(cachedTokenExpiresAt.minusSeconds(15))) {
        return cachedToken;
      }

      MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
      form.add("grant_type", "client_credentials");
      form.add("client_id", props.getClientId());
      form.add("client_secret", props.getClientSecret());

      Map<?, ?> tokenResp;
      try {
        tokenResp = restClient.post()
          .uri("/realms/{realm}/protocol/openid-connect/token", props.getRealm())
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(form)
          .retrieve()
          .body(Map.class);
      } catch (Exception ex) {
        throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_TOKEN_ERROR", "No fue posible obtener token de servicio en Keycloak");
      }

      if (tokenResp == null || tokenResp.get("access_token") == null) {
        throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_TOKEN_ERROR", "Respuesta invalida del token endpoint de Keycloak");
      }

      String token = String.valueOf(tokenResp.get("access_token"));
      long expiresIn = 60;
      Object exp = tokenResp.get("expires_in");
      if (exp instanceof Number n) {
        expiresIn = n.longValue();
      }

      cachedToken = token;
      cachedTokenExpiresAt = Instant.now().plusSeconds(Math.max(10, expiresIn));
      return token;
    }
  }
}
