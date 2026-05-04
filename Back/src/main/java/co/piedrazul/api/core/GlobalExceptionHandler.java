package co.piedrazul.api.core;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiError> handleApp(AppException ex) {
    HttpStatus status = ex.getStatus();
    return ResponseEntity.status(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.of(ex.getCode(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
      .findFirst()
      .map(err -> err.getField() + ": " + err.getDefaultMessage())
      .orElse("Solicitud invalida");
    return ResponseEntity.badRequest().body(ApiError.of("BAD_REQUEST", message));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .body(ApiError.of("UNAUTHORIZED", "No autenticado o token invalido"));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
      .body(ApiError.of("FORBIDDEN", "No tienes permisos para ejecutar esta operacion"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleOther(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ApiError.of("INTERNAL_ERROR", "Error interno del servidor"));
  }
}
