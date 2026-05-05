package co.piedrazul.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleAppDevuelveEstadoYPayloadEsperados() {
    AppException ex = new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Dato invalido");

    var response = handler.handleApp(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(false, response.getBody().ok());
    assertEquals("BAD_REQUEST", response.getBody().code());
    assertEquals("Dato invalido", response.getBody().message());
  }

  @Test
  void handleAuthenticationDevuelve401() {
    var response = handler.handleAuthentication(new BadCredentialsException("bad"));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("UNAUTHORIZED", response.getBody().code());
    assertEquals("No autenticado o token invalido", response.getBody().message());
  }

  @Test
  void handleAccessDeniedDevuelve403() {
    var response = handler.handleAccessDenied(new AccessDeniedException("denied"));

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("FORBIDDEN", response.getBody().code());
    assertEquals("No tienes permisos para ejecutar esta operacion", response.getBody().message());
  }
}

