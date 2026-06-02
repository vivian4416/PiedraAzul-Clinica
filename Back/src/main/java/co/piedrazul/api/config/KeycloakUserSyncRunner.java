package co.piedrazul.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import co.piedrazul.api.modules.usuarios.UsuarioService;

@Component
public class KeycloakUserSyncRunner implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(KeycloakUserSyncRunner.class);
  private final UsuarioService usuarioService;
  private final boolean enabled;

  public KeycloakUserSyncRunner(UsuarioService usuarioService,
                                @Value("${app.sync.keycloak.enabled:true}") boolean enabled) {
    this.usuarioService = usuarioService;
    this.enabled = enabled;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!enabled) {
      log.info("[SYNC-KC] Sincronizacion deshabilitada por config");
      return;
    }

    try {
      usuarioService.sincronizarUsuariosKeycloak();
    } catch (Exception ex) {
      log.warn("[SYNC-KC] Error al sincronizar usuarios de Keycloak: {}", ex.getMessage());
    }
  }
}
