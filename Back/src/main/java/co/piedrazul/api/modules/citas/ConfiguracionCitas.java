package co.piedrazul.api.modules.citas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracion_citas")
public class ConfiguracionCitas {
  @Id
  private Integer id;

  @Column(name = "ventana_semanas", nullable = false)
  private Integer ventanaSemanas;

  public Integer getId() { return id; }
  public Integer getVentanaSemanas() { return ventanaSemanas; }

  public void setId(Integer id) { this.id = id; }
  public void setVentanaSemanas(Integer ventanaSemanas) { this.ventanaSemanas = ventanaSemanas; }
}
