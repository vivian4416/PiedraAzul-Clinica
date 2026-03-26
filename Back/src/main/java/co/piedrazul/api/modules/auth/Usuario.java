package co.piedrazul.api.modules.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String login;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "nombre_completo", nullable = false)
  private String nombreCompleto;

  @Column(nullable = false)
  private String rol;

  @Column(nullable = false)
  private boolean activo;

  public Long getId() { return id; }
  public String getLogin() { return login; }
  public String getPasswordHash() { return passwordHash; }
  public String getNombreCompleto() { return nombreCompleto; }
  public String getRol() { return rol; }
  public boolean isActivo() { return activo; }
}
