package co.piedrazul.api.modules.citas;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "citas")
public class Cita {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "medico_id", nullable = false)
  private String medicoId;

  @Column(name = "paciente_id", nullable = false)
  private Long pacienteId;

  @Column(name = "creado_por", nullable = false)
  private String creadoPor;

  @Column(name = "fecha_hora", nullable = false)
  private LocalDateTime fechaHora;

  @Column(nullable = false)
  private String estado;

  @Column(nullable = false)
  private String origen;

  public Long getId() { return id; }
  public String getMedicoId() { return medicoId; }
  public Long getPacienteId() { return pacienteId; }
  public String getCreadoPor() { return creadoPor; }
  public LocalDateTime getFechaHora() { return fechaHora; }
  public String getEstado() { return estado; }
  public String getOrigen() { return origen; }

  public void setMedicoId(String medicoId) { this.medicoId = medicoId; }
  public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
  public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }
  public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
  public void setEstado(String estado) { this.estado = estado; }
  public void setOrigen(String origen) { this.origen = origen; }
}
