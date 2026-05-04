package co.piedrazul.api.integrations.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Component
@Validated
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakAdminProperties {

  /** Base URL del servidor Keycloak, ej: http://localhost:8080 */
  @NotBlank
  private String baseUrl;

  /** Realm a administrar, ej: PiedraAzul_Realm */
  @NotBlank
  private String realm;

  /** Client confidential con Service Account habilitada */
  @NotBlank
  private String clientId;

  /** Secret del client confidential */
  @NotBlank
  private String clientSecret;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }
}
