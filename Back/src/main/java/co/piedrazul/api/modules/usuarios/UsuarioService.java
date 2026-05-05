package co.piedrazul.api.modules.usuarios;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import co.piedrazul.api.core.AppException;
import co.piedrazul.api.integrations.keycloak.KeycloakAdminClient;
import co.piedrazul.api.integrations.keycloak.dto.KeycloakRoleRep;
import co.piedrazul.api.integrations.keycloak.dto.KeycloakUserRep;
import co.piedrazul.api.modules.auth.RegisterPacienteRequest;
import co.piedrazul.api.modules.medicos.Medico;
import co.piedrazul.api.modules.medicos.MedicoRepository;
import co.piedrazul.api.modules.pacientes.PacienteInput;
import co.piedrazul.api.modules.pacientes.PacienteService;

@Service
public class UsuarioService {
  private static final List<String> ROLES_VALIDOS = List.of("ADMIN", "AGENDADOR", "MEDICO", "PACIENTE");
  private static final Set<String> ROLES_KC_VALIDOS = Set.of("administrador", "agendador", "medico", "paciente");
  private final KeycloakAdminClient keycloak;
  private final MedicoRepository medicoRepository;
  private final PacienteService pacienteService;

  public UsuarioService(KeycloakAdminClient keycloak, MedicoRepository medicoRepository, PacienteService pacienteService) {
    this.keycloak = keycloak;
    this.medicoRepository = medicoRepository;
    this.pacienteService = pacienteService;
  }

  public List<UsuarioResponse> listar() {
    List<KeycloakUserRep> users = keycloak.listUsers(200);
    return users.stream()
      .map(u -> UsuarioResponse.from(u, primaryAppRole(u.id()), nombreCompleto(u)))
      .toList();
  }

  public UsuarioResponse obtener(String id) {
    KeycloakUserRep user = keycloak.getUser(id);
    return UsuarioResponse.from(user, primaryAppRole(id), nombreCompleto(user));
  }

  public UsuarioResponse crear(UsuarioCreateRequest request) {
    String login = normalizarTexto(request.login());
    if (keycloak.findUserByUsernameExact(login) != null) {
      throw new AppException(HttpStatus.CONFLICT, "CONFLICT", "Ya existe un usuario con ese login");
    }

    String rol = normalizarRol(request.rol());
    boolean enabled = request.activo();
    String firstName = normalizarTexto(request.nombreCompleto());
    String lastName = normalizarTexto(request.apellido());
    String email = normalizarEmail(request.email());

    Map<String, Object> attrs = new LinkedHashMap<>();
    if (request.documento() != null && !request.documento().isBlank()) {
      attrs.put("documento", List.of(request.documento().trim()));
    }
    if (request.celular() != null && !request.celular().isBlank()) {
      attrs.put("celular", List.of(request.celular().trim()));
    }

    KeycloakUserRep userRep = new KeycloakUserRep(
      null,
      login,
      firstName,
      lastName,
      email,
      enabled,
      attrs
    );

    String userId = keycloak.createUser(userRep);
    keycloak.resetPassword(userId, normalizarTexto(request.password()), false);
    asignarRolUnico(userId, rol);

    if ("MEDICO".equals(rol)) {
      sincronizarPerfilMedico(userId, firstName + " " + lastName, enabled);
    } else if ("PACIENTE".equals(rol)) {
      sincronizarPerfilPaciente(request, firstName, lastName);
    }

    KeycloakUserRep created = keycloak.getUser(userId);
    return UsuarioResponse.from(created, rol, nombreCompleto(created));
  }

  public UsuarioResponse actualizar(String id, UsuarioUpdateRequest request) {
    KeycloakUserRep existing = keycloak.getUser(id);
    String login = normalizarTexto(request.login());
    if (existing.username() != null && !existing.username().equals(login)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Keycloak no permite cambiar el login de este usuario desde este endpoint");
    }
    KeycloakUserRep other = keycloak.findUserByUsernameExact(login);
    if (other != null && other.id() != null && !other.id().equals(id)) {
      throw new AppException(HttpStatus.CONFLICT, "CONFLICT", "Ya existe un usuario con ese login");
    }

    String rol = normalizarRol(request.rol());
    boolean enabled = request.activo();
    String firstName = normalizarTexto(request.nombreCompleto());
    String lastName = normalizarTexto(request.apellido());
    String email = normalizarEmail(request.email());

    Map<String, Object> attrs = existing.attributes() != null
      ? new LinkedHashMap<>(existing.attributes())
      : new LinkedHashMap<>();
    if (request.documento() != null && !request.documento().isBlank()) {
      attrs.put("documento", List.of(request.documento().trim()));
    }
    if (request.celular() != null && !request.celular().isBlank()) {
      attrs.put("celular", List.of(request.celular().trim()));
    }

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("firstName", firstName);
    payload.put("lastName", lastName);
    payload.put("email", email);
    payload.put("enabled", enabled);
    if (!attrs.isEmpty()) {
      payload.put("attributes", attrs);
    }

    keycloak.updateUser(id, payload);
    if (request.password() != null && !request.password().isBlank()) {
      keycloak.resetPassword(id, request.password().trim(), false);
    }
    asignarRolUnico(id, rol);

    if ("MEDICO".equals(rol)) {
      sincronizarPerfilMedico(id, firstName + " " + lastName, enabled);
    } else {
      desactivarPerfilMedicoSiExiste(id);
      if ("PACIENTE".equals(rol)) {
        sincronizarPerfilPaciente(request, firstName, lastName);
      }
    }

    KeycloakUserRep updated = keycloak.getUser(id);
    return UsuarioResponse.from(updated, rol, nombreCompleto(updated));
  }

  public UsuarioResponse desactivar(String id) {
    keycloak.setEnabled(id, false);
    desactivarPerfilMedicoSiExiste(id);
    KeycloakUserRep user = keycloak.getUser(id);
    return UsuarioResponse.from(user, primaryAppRole(id), nombreCompleto(user));
  }

  public UsuarioResponse obtenerActual(Authentication authentication) {
    String userId = keycloakUserId(authentication);
    if (userId == null || userId.isBlank()) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Usuario no autenticado");
    }
    KeycloakUserRep user = keycloak.getUser(userId);
    return UsuarioResponse.from(user, primaryAppRole(userId), nombreCompleto(user));
  }

  public UsuarioResponse registrarPaciente(RegisterPacienteRequest request) {
    String documento = normalizarTexto(request.documento());
    if (keycloak.findUserByUsernameExact(documento) != null) {
      throw new AppException(HttpStatus.CONFLICT, "CONFLICT", "Ya existe un usuario con ese documento");
    }

    String firstName = normalizarTexto(request.nombres());
    String lastName = normalizarTexto(request.apellidos());
    String email = normalizarEmailOptional(request.email());
    String celular = normalizarTexto(request.celular());
    String genero = normalizarGenero(request.genero());

    Map<String, Object> attrs = new LinkedHashMap<>();
    attrs.put("documento", List.of(documento));
    attrs.put("celular", List.of(celular));
    attrs.put("genero", List.of(genero));

    KeycloakUserRep userRep = new KeycloakUserRep(
      null,
      documento,
      firstName,
      lastName,
      email,
      true,
      attrs
    );

    String userId = keycloak.createUser(userRep);
    keycloak.resetPassword(userId, normalizarTexto(request.password()), false);
    asignarRolUnico(userId, "PACIENTE");

    pacienteService.findOrCreate(new PacienteInput(
      documento,
      firstName,
      lastName,
      celular,
      genero,
      request.fechaNacimiento(),
      email
    ));

    KeycloakUserRep created = keycloak.getUser(userId);
    return UsuarioResponse.from(created, "PACIENTE", nombreCompleto(created));
  }

  private void sincronizarPerfilMedico(String userId, String nombres, boolean activo) {
    medicoRepository.findById(userId).ifPresentOrElse(
      medico -> {
        medico.setNombres(nombres);
        medico.setActivo(activo);
        medicoRepository.save(medico);
      },
      () -> {
        Medico medico = new Medico();
        medico.setId(userId);
        medico.setNombres(nombres);
        medico.setTipo("MEDICO");
        medico.setEspecialidad("GENERAL");
        medico.setIntervaloMin(30);
        medico.setActivo(activo);
        medicoRepository.save(medico);
      }
    );
  }

  private void desactivarPerfilMedicoSiExiste(String userId) {
    medicoRepository.findById(userId).ifPresent(medico -> {
      medico.setActivo(false);
      medicoRepository.save(medico);
    });
  }

  private void sincronizarPerfilPaciente(UsuarioCreateRequest request, String firstName, String lastName) {
    String documento = normalizarTexto(request.documento());
    String celular = normalizarTexto(request.celular());
    String genero = normalizarGenero(request.genero());
    pacienteService.findOrCreate(new PacienteInput(
      documento,
      firstName,
      lastName,
      celular,
      genero,
      request.fechaNacimiento(),
      request.email()
    ));
  }

  private void sincronizarPerfilPaciente(UsuarioUpdateRequest request, String firstName, String lastName) {
    String documento = normalizarTexto(request.documento());
    String celular = normalizarTexto(request.celular());
    String genero = normalizarGenero(request.genero());
    pacienteService.findOrCreate(new PacienteInput(
      documento,
      firstName,
      lastName,
      celular,
      genero,
      request.fechaNacimiento(),
      request.email()
    ));
  }

  private String normalizarTexto(String value) {
    if (value == null || value.isBlank()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "El campo es obligatorio");
    }
    return value.trim();
  }

  private String normalizarRol(String value) {
    String rol = normalizarTexto(value).toUpperCase();
    if (!ROLES_VALIDOS.contains(rol)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Rol invalido");
    }
    return rol;
  }

  private String normalizarEmail(String value) {
    String email = normalizarTexto(value);
    if (!email.contains("@")) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Email invalido");
    }
    return email;
  }

  private String normalizarEmailOptional(String value) {
    if (value == null || value.isBlank()) return null;
    String email = value.trim();
    if (!email.contains("@")) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Email invalido");
    }
    return email;
  }

  private String normalizarGenero(String value) {
    if (value == null || value.isBlank()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "genero es obligatorio");
    }
    String normalized = value.trim().toLowerCase();
    if ("masculino".equals(normalized) || "hombre".equals(normalized)) return "HOMBRE";
    if ("femenino".equals(normalized) || "mujer".equals(normalized)) return "MUJER";
    if ("otro".equals(normalized)) return "OTRO";
    return normalized.toUpperCase();
  }

  public void crearUsuarioPacienteSiNoExiste(PacienteInput input, String password) {
    String documento = normalizarTexto(input.numDocumento());
    String firstName = normalizarTexto(input.nombres());
    String lastName = normalizarTexto(input.apellidos());
    String email = normalizarEmailOptional(input.email());
    String celular = normalizarTexto(input.celular());
    String genero = normalizarGenero(input.genero());

    KeycloakUserRep existing = keycloak.findUserByUsernameExact(documento);
    Map<String, Object> attrs = existing != null && existing.attributes() != null
      ? new LinkedHashMap<>(existing.attributes())
      : new LinkedHashMap<>();
    attrs.put("documento", List.of(documento));
    attrs.put("celular", List.of(celular));
    attrs.put("genero", List.of(genero));

    if (existing == null) {
      if (password == null || password.isBlank()) {
        throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "password es obligatorio para crear usuario");
      }

      KeycloakUserRep userRep = new KeycloakUserRep(
        null,
        documento,
        firstName,
        lastName,
        email,
        true,
        attrs
      );

      String userId = keycloak.createUser(userRep);
      keycloak.resetPassword(userId, password.trim(), false);
      asignarRolUnico(userId, "PACIENTE");
      return;
    }

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("firstName", firstName);
    payload.put("lastName", lastName);
    payload.put("email", email);
    payload.put("attributes", attrs);
    keycloak.updateUser(existing.id(), payload);
  }

  private String nombreCompleto(KeycloakUserRep user) {
    if (user == null) return "";

    String first = user.firstName() == null ? "" : user.firstName().trim();
    String last = user.lastName() == null ? "" : user.lastName().trim();
    String combined = (first + " " + last).trim();
    return combined.isBlank() ? (user.username() == null ? "" : user.username()) : combined;
  }

  private void asignarRolUnico(String userId, String appRole) {
    String kcRole = toKeycloakRealmRole(appRole);
    KeycloakRoleRep rep = keycloak.getRealmRoleByName(kcRole);
    if (rep == null || rep.name() == null) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ROLE_NOT_FOUND", "Rol no existe en Keycloak: " + kcRole);
    }
    keycloak.replaceRealmRoleMappings(userId, List.of(rep));
  }

  private String primaryAppRole(String userId) {
    List<KeycloakRoleRep> roles = keycloak.getRealmRoleMappings(userId);
    if (roles == null || roles.isEmpty()) return "";

    for (KeycloakRoleRep r : roles) {
      if (r != null && r.name() != null && ROLES_KC_VALIDOS.contains(r.name().toLowerCase())) {
        return toAppRole(r.name());
      }
    }
    return toAppRole(roles.get(0).name());
  }

  private String toKeycloakRealmRole(String appRole) {
    if (appRole == null) return "";
    return switch (appRole.trim().toUpperCase()) {
      case "ADMIN" -> "administrador";
      case "AGENDADOR" -> "agendador";
      case "MEDICO" -> "medico";
      case "PACIENTE" -> "paciente";
      default -> appRole.trim().toLowerCase();
    };
  }

  private String toAppRole(String kcRole) {
    if (kcRole == null) return "";
    return switch (kcRole.trim().toLowerCase()) {
      case "administrador" -> "ADMIN";
      case "agendador" -> "AGENDADOR";
      case "medico" -> "MEDICO";
      case "paciente" -> "PACIENTE";
      default -> kcRole.trim().toUpperCase();
    };
  }

  private String keycloakUserId(Authentication authentication) {
    if (authentication == null) {
      return null;
    }
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
        return jwt.getSubject();
      }
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof Jwt jwt && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
      return jwt.getSubject();
    }
    return authentication.getName();
  }
}
