package co.piedrazul.api.modules.citas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
public class Auditoria {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "usuario_id")
  private Long usuarioId;

  @Column(nullable = false)
  private String accion;

  private String entidad;

  @Column(name = "entidad_id")
  private Long entidadId;

  private String detalle;

  @Column(name = "fecha_hora")
  private LocalDateTime fechaHora;

  public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
  public void setAccion(String accion) { this.accion = accion; }
  public void setEntidad(String entidad) { this.entidad = entidad; }
  public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }
  public void setDetalle(String detalle) { this.detalle = detalle; }
  public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
