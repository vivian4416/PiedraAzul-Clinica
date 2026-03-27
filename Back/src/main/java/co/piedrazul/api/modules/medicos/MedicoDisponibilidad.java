package co.piedrazul.api.modules.medicos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "medico_disponibilidad")
public class MedicoDisponibilidad {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "medico_id", nullable = false)
  private Long medicoId;

  @Column(name = "dia_semana", nullable = false)
  private Integer diaSemana;

  @Column(name = "hora_inicio", nullable = false)
  private LocalTime horaInicio;

  @Column(name = "hora_fin", nullable = false)
  private LocalTime horaFin;

  public Long getId() { return id; }
  public Long getMedicoId() { return medicoId; }
  public Integer getDiaSemana() { return diaSemana; }
  public LocalTime getHoraInicio() { return horaInicio; }
  public LocalTime getHoraFin() { return horaFin; }

  public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }
  public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }
  public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
  public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}
