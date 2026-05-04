package co.piedrazul.api.modules.citas;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auditoria")
public class Auditoria {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "usuario_id")
  private String usuarioId;

  @Column(nullable = false)
  private String accion;

  private String entidad;

  @Column(name = "entidad_id")
  private Long entidadId;

  private String detalle;

  @Column(name = "fecha_hora")
  private LocalDateTime fechaHora;

  public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
  public void setAccion(String accion) { this.accion = accion; }
  public void setEntidad(String entidad) { this.entidad = entidad; }
  public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }
  public void setDetalle(String detalle) { this.detalle = detalle; }
  public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
