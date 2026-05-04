package co.piedrazul.api.integrations.keycloak;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class KeycloakAdminConfig {

  @Bean
  RestClient keycloakRestClient(KeycloakAdminProperties props) {
    return RestClient.builder()
      .baseUrl(props.getBaseUrl())
      .build();
  }
}
