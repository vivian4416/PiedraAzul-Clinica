package co.piedrazul.api.modules.auth;

import co.piedrazul.api.core.AppException;
import co.piedrazul.api.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public AuthResponse login(LoginRequest request) {
    Usuario user = usuarioRepository.findByLogin(request.login())
      .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Credenciales invalidas"));

    if (!user.isActivo() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Credenciales invalidas");
    }

    String token = jwtService.generate(user.getId(), user.getLogin(), user.getRol());
    return new AuthResponse(true, token, user.getRol());
  }
}
