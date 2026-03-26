package co.piedrazul.api.modules.pacientes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "pacientes")
public class Paciente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "num_documento", nullable = false, unique = true)
  private String numDocumento;

  @Column(nullable = false)
  private String nombres;

  @Column(nullable = false)
  private String apellidos;

  @Column(nullable = false)
  private String celular;

  @Column(nullable = false)
  private String genero;

  @Column(name = "fecha_nacimiento")
  private LocalDate fechaNacimiento;

  private String email;

  public Long getId() { return id; }
  public String getNumDocumento() { return numDocumento; }
  public String getNombres() { return nombres; }
  public String getApellidos() { return apellidos; }
  public String getCelular() { return celular; }
  public String getGenero() { return genero; }
  public LocalDate getFechaNacimiento() { return fechaNacimiento; }
  public String getEmail() { return email; }

  public void setNumDocumento(String numDocumento) { this.numDocumento = numDocumento; }
  public void setNombres(String nombres) { this.nombres = nombres; }
  public void setApellidos(String apellidos) { this.apellidos = apellidos; }
  public void setCelular(String celular) { this.celular = celular; }
  public void setGenero(String genero) { this.genero = genero; }
  public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
  public void setEmail(String email) { this.email = email; }
}
