package co.piedrazul.api.modules.medicos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicos")
public class Medico {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nombres;

  @Column(nullable = false)
  private String tipo;

  @Column(nullable = false)
  private String especialidad;

  @Column(name = "intervalo_min", nullable = false)
  private Integer intervaloMin;

  @Column(nullable = false)
  private boolean activo;

  public Long getId() { return id; }
  public String getNombres() { return nombres; }
  public String getTipo() { return tipo; }
  public String getEspecialidad() { return especialidad; }
  public Integer getIntervaloMin() { return intervaloMin; }
  public boolean isActivo() { return activo; }

  public void setIntervaloMin(Integer intervaloMin) { this.intervaloMin = intervaloMin; }
  public void setActivo(boolean activo) { this.activo = activo; }
}
