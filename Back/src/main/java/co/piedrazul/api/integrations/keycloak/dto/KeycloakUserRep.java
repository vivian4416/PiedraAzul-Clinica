package co.piedrazul.api.integrations.keycloak.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakUserRep(
  String id,
  String username,
  String firstName,
  String lastName,
  String email,
  Boolean enabled,
  Map<String, Object> attributes
) {}
