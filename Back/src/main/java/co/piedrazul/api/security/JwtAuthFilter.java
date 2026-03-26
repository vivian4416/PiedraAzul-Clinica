package co.piedrazul.api.security;

import co.piedrazul.api.modules.auth.Usuario;
import co.piedrazul.api.modules.auth.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UsuarioRepository usuarioRepository;

  public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
    this.jwtService = jwtService;
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith(SecurityConstants.BEARER)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(SecurityConstants.BEARER.length());
    try {
      Claims claims = jwtService.parse(token);
      Long userId = Long.parseLong(claims.getSubject());
      Usuario user = usuarioRepository.findById(userId).orElse(null);
      if (user != null && user.isActivo()) {
        AuthUser principal = new AuthUser(user.getId(), user.getLogin(), user.getRol());
        var auth = new UsernamePasswordAuthenticationToken(
          principal,
          null,
          List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    } catch (Exception ignored) {
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
