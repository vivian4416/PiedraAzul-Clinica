import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  CitasService,
  ConfiguracionAgendamiento,
  MedicoConfiguracion,
} from '../../services/citas.service';

interface DiaSemanaItem {
  id: number;
  nombre: string;
  corto: string;
}

interface DisponibilidadEditor {
  activo: boolean;
  horaInicio: string;
  horaFin: string;
}

@Component({
  selector: 'app-configuracion',
  imports: [CommonModule, FormsModule],
  templateUrl: './configuracion.html',
  styleUrl: './configuracion.css',
})
export class ConfiguracionComponent implements OnInit {
  readonly diasSemana: DiaSemanaItem[] = [
    { id: 1, nombre: 'Lunes', corto: 'Lun' },
    { id: 2, nombre: 'Martes', corto: 'Mar' },
    { id: 3, nombre: 'Miercoles', corto: 'Mie' },
    { id: 4, nombre: 'Jueves', corto: 'Jue' },
    { id: 5, nombre: 'Viernes', corto: 'Vie' },
    { id: 6, nombre: 'Sabado', corto: 'Sab' },
    { id: 7, nombre: 'Domingo', corto: 'Dom' },
  ];

  ventanaSemanas = 4;
  medicos: MedicoConfiguracion[] = [];
  medicoSeleccionadoId: number | null = null;
  medicoActivo = true;
  intervaloSlot = 20;
  disponibilidadEditor: Record<number, DisponibilidadEditor> = {};

  cargando = false;
  guardando = false;
  mensaje = '';
  tipoMensaje: 'ok' | 'error' = 'ok';

  constructor(private readonly citasService: CitasService) {}

  ngOnInit(): void {
    void this.cargarInicial();
  }

  private async cargarInicial(): Promise<void> {
    this.cargando = true;
    this.mensaje = '';

    try {
      await this.citasService.inicializar();
      const configuracion = await this.citasService.obtenerConfiguracionAgendamiento(true);
      this.aplicarConfiguracion(configuracion);
    } catch {
      this.tipoMensaje = 'error';
      this.mensaje = 'No fue posible cargar la configuracion. Verifica conexion con el backend.';
    } finally {
      this.cargando = false;
    }
  }

  seleccionarMedico(medicoId: number): void {
    this.medicoSeleccionadoId = medicoId;
    const medico = this.medicos.find(m => m.id === medicoId);
    if (!medico) {
      return;
    }

    this.medicoActivo = medico.activo;
    this.intervaloSlot = medico.intervaloMin;
    this.disponibilidadEditor = this.crearEditorDisponibilidad(medico);
  }

  toggleDia(diaSemana: number): void {
    const actual = this.disponibilidadEditor[diaSemana];
    this.disponibilidadEditor[diaSemana] = {
      ...actual,
      activo: !actual.activo,
    };
  }

  isDiaActivo(diaSemana: number): boolean {
    return !!this.disponibilidadEditor[diaSemana]?.activo;
  }

  async guardarConfiguracion(): Promise<void> {
    if (this.guardando) {
      return;
    }

    if (this.ventanaSemanas < 1 || this.ventanaSemanas > 12) {
      this.tipoMensaje = 'error';
      this.mensaje = 'La ventana de agendamiento debe estar entre 1 y 12 semanas.';
      return;
    }

    if (this.intervaloSlot < 5 || this.intervaloSlot > 120) {
      this.tipoMensaje = 'error';
      this.mensaje = 'El intervalo por medico debe estar entre 5 y 120 minutos.';
      return;
    }

    const medicoEditando = this.medicos.find(m => m.id === this.medicoSeleccionadoId);
    if (!medicoEditando) {
      this.tipoMensaje = 'error';
      this.mensaje = 'Selecciona un medico o terapista para guardar cambios.';
      return;
    }

    const disponibilidad = this.diasSemana
      .map(dia => ({ dia, data: this.disponibilidadEditor[dia.id] }))
      .filter(item => item.data?.activo)
      .map(item => ({
        diaSemana: item.dia.id,
        horaInicio: item.data.horaInicio,
        horaFin: item.data.horaFin,
      }));

    const rangoInvalido = disponibilidad.some(d => d.horaInicio >= d.horaFin);
    if (rangoInvalido) {
      this.tipoMensaje = 'error';
      this.mensaje = 'La hora de inicio debe ser menor a la hora fin en cada dia activo.';
      return;
    }

    medicoEditando.activo = this.medicoActivo;
    medicoEditando.intervaloMin = this.intervaloSlot;
    medicoEditando.disponibilidad = disponibilidad;

    this.guardando = true;
    this.mensaje = '';

    try {
      const configGuardada = await this.citasService.guardarConfiguracionAgendamiento({
        ventanaSemanas: this.ventanaSemanas,
        medicos: this.medicos,
      });

      this.aplicarConfiguracion(configGuardada, medicoEditando.id);
      this.tipoMensaje = 'ok';
      this.mensaje = 'Configuracion guardada exitosamente.';
    } catch {
      this.tipoMensaje = 'error';
      this.mensaje = 'No fue posible guardar la configuracion en el backend.';
    } finally {
      this.guardando = false;
    }
  }

  getNombreMedicoSeleccionado(): string {
    const medico = this.medicos.find(m => m.id === this.medicoSeleccionadoId);
    return medico ? `${medico.nombres} - ${medico.especialidad}` : 'Selecciona un medico o terapista';
  }

  private aplicarConfiguracion(configuracion: ConfiguracionAgendamiento, medicoPreferidoId?: number): void {
    this.ventanaSemanas = configuracion.ventanaSemanas;
    this.medicos = configuracion.medicos.map(m => ({
      ...m,
      disponibilidad: [...m.disponibilidad],
    }));

    const fallbackId = this.medicos[0]?.id ?? null;
    const selectedId = medicoPreferidoId ?? this.medicoSeleccionadoId ?? fallbackId;
    if (selectedId !== null) {
      this.seleccionarMedico(selectedId);
    }
  }

  private crearEditorDisponibilidad(medico: MedicoConfiguracion): Record<number, DisponibilidadEditor> {
    const base: Record<number, DisponibilidadEditor> = {};
    for (const dia of this.diasSemana) {
      base[dia.id] = { activo: false, horaInicio: '08:00', horaFin: '12:00' };
    }

    for (const disp of medico.disponibilidad) {
      base[disp.diaSemana] = {
        activo: true,
        horaInicio: disp.horaInicio,
        horaFin: disp.horaFin,
      };
    }

    return base;
  }
}
