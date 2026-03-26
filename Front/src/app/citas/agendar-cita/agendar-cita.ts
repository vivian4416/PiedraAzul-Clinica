import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../../services/citas.service';

@Component({
  selector: 'app-agendar-cita',
  imports: [CommonModule, FormsModule],
  templateUrl: './agendar-cita.html',
  styleUrl: './agendar-cita.css',
})
export class AgendarCitaComponent implements OnInit {
  diasDisponibles: { [key: string]: string[] } = {};
  diasArray: string[] = [];
  slotSeleccionado: { dia: string; hora: string } | null = null;
  mostrarConfirmacion = false;
  semanaSeleccionada = 1;

  constructor(private readonly citasService: CitasService) {}

  ngOnInit(): void {
    void this.cargarInicial();
  }

  private async cargarInicial(): Promise<void> {
    await this.citasService.inicializar();
    await this.citasService.cargarDiasDisponibles(1, this.semanaSeleccionada - 1);
    this.diasDisponibles = this.citasService.getDiasDisponibles();
    this.diasArray = Object.keys(this.diasDisponibles);
  }

  seleccionarSlot(dia: string, hora: string): void {
    this.slotSeleccionado = { dia, hora };
    this.mostrarConfirmacion = true;
  }

  async seleccionarSemana(semana: number): Promise<void> {
    this.semanaSeleccionada = semana;
    await this.citasService.cargarDiasDisponibles(1, semana - 1);
    this.diasDisponibles = this.citasService.getDiasDisponibles();
    this.diasArray = Object.keys(this.diasDisponibles);
    this.slotSeleccionado = null;
    this.mostrarConfirmacion = false;
  }

  confirmarCita(): void {
    if (!this.slotSeleccionado) return;
    
    // Aquí se crearía la cita del paciente
    const mensaje = `¡Cita confirmada! ${this.slotSeleccionado.dia} a las ${this.slotSeleccionado.hora}`;
    alert(mensaje);
    
    // Reset
    this.slotSeleccionado = null;
    this.mostrarConfirmacion = false;
  }

  getNombreMedico(): string {
    return 'Dr. Andrés Mora · Quiropráctica';
  }
}
