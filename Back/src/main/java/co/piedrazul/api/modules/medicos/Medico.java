package co.piedrazul.api.modules.medicos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicos")
public class Medico {
  @Id
  private String id;

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

  public String getId() { return id; }
  public String getNombres() { return nombres; }
  public String getTipo() { return tipo; }
  public String getEspecialidad() { return especialidad; }
  public Integer getIntervaloMin() { return intervaloMin; }
  public boolean isActivo() { return activo; }

  public void setId(String id) { this.id = id; }
  public void setNombres(String nombres) { this.nombres = nombres; }
  public void setTipo(String tipo) { this.tipo = tipo; }
  public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
  public void setIntervaloMin(Integer intervaloMin) { this.intervaloMin = intervaloMin; }
  public void setActivo(boolean activo) { this.activo = activo; }
}
