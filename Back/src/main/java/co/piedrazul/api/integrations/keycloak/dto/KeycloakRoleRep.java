package co.piedrazul.api.integrations.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakRoleRep(
  String id,
  String name,
  Boolean composite,
  Boolean clientRole,
  String containerId
) {}
