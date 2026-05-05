package co.piedrazul.api.modules.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.piedrazul.api.core.AppException;
import co.piedrazul.api.security.JwtService;

class AuthServiceTest {
  @Test
  void loginExitosoDevuelveTokenYRol() throws Exception {
    UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    JwtService jwtService = mock(JwtService.class);
    AuthService service = new AuthService(usuarioRepository, passwordEncoder, jwtService);

    Usuario user = usuario(42L, "ana", "hash-secreto", "PACIENTE", true);
    when(usuarioRepository.findByLogin("ana")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("clave", "hash-secreto")).thenReturn(true);
    when(jwtService.generate(42L, "ana", "PACIENTE")).thenReturn("jwt-token");

    AuthResponse response = service.login(new LoginRequest("ana", "clave"));

    assertTrue(response.ok());
    assertEquals("jwt-token", response.token());
    assertEquals("PACIENTE", response.role());
    verify(jwtService).generate(42L, "ana", "PACIENTE");
  }

  @Test
  void loginConCredencialesInvalidasLanzaUnauthorized() throws Exception {
    UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    JwtService jwtService = mock(JwtService.class);
    AuthService service = new AuthService(usuarioRepository, passwordEncoder, jwtService);

    Usuario user = usuario(42L, "ana", "hash-secreto", "PACIENTE", true);
    when(usuarioRepository.findByLogin("ana")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("clave", "hash-secreto")).thenReturn(false);

    AppException ex = assertThrows(AppException.class, () -> service.login(new LoginRequest("ana", "clave")));

    assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    assertEquals("UNAUTHORIZED", ex.getCode());
    verify(jwtService, never()).generate(42L, "ana", "PACIENTE");
  }

  private static Usuario usuario(Long id, String login, String passwordHash, String rol, boolean activo) throws Exception {
    Usuario user = new Usuario();
    setField(user, "id", id);
    user.setLogin(login);
    user.setPasswordHash(passwordHash);
    user.setRol(rol);
    user.setActivo(activo);
    return user;
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}



