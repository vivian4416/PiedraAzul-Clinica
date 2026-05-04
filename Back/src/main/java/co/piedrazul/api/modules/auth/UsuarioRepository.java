package co.piedrazul.api.modules.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByLogin(String login);
  boolean existsByLogin(String login);
  boolean existsByLoginAndIdNot(String login, Long id);
  List<Usuario> findAllByOrderByIdAsc();
}
