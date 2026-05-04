package co.piedrazul.api.integrations.keycloak.dto;

public record KeycloakCredentialRep(
  String type,
  String value,
  Boolean temporary
) {}
