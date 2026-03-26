import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../services/citas.service';
import { Cita } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css',
})
export class CitasComponent implements OnInit {
  citas: Cita[] = [];
  citasFiltradas: Cita[] = [];

  medicosOptions: Array<{ id: number; nombre: string }> = [{ id: 0, nombre: 'Todos' }];

  filtroMedico: number = 0;
  filtroFecha: string = new Date().toISOString().slice(0, 10);
  cargando = false;
  errorCarga = '';
  
  estadisticas = {
    total: 0,
    confirmadas: 0,
    atendidas: 0,
    canceladas: 0,
  };

  constructor(private readonly citasService: CitasService) {}

  ngOnInit(): void {
    void this.cargarInicial();
  }

  private async cargarInicial(): Promise<void> {
    this.cargando = true;
    this.errorCarga = '';
    try {
      await this.citasService.inicializar();
      this.medicosOptions = [
        { id: 0, nombre: 'Todos' },
        ...this.citasService.getMedicos().map(m => ({
          id: m.id,
          nombre: `${m.nombre} ${m.apellido}`.trim(),
        })),
      ];

      const primerMedico = this.citasService.getMedicos()[0];
      this.filtroMedico = primerMedico ? primerMedico.id : 0;
      await this.filtrar();
    } catch {
      this.errorCarga = 'No se pudo conectar con el backend. Verifica que springboot-api esté encendido en el puerto 8090.';
      this.citas = [];
      this.citasFiltradas = [];
      this.actualizarEstadisticas();
    } finally {
      this.cargando = false;
    }
  }

  async filtrar(): Promise<void> {
    const fecha = this.filtroFecha || new Date().toISOString().slice(0, 10);
    this.filtroFecha = fecha;

    await this.citasService.cargarCitasPorFiltro(this.filtroMedico, fecha);
    this.citas = this.citasService.getCitas();
    this.citasFiltradas = [...this.citas];
    this.actualizarEstadisticas();
  }

  actualizarEstadisticas(): void {
    this.estadisticas = {
      total: this.citasFiltradas.length,
      confirmadas: this.citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: this.citasFiltradas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: this.citasFiltradas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${Number.parseInt(day, 10)} ${meses[Number.parseInt(m, 10)]} ${y}`;
  }

  exportarCSV(): void {
    const rows = [
      ['Hora', 'Paciente', 'Documento', 'Celular', 'Origen', 'Estado'],
      ...this.citasFiltradas.map(c => [c.hora, c.paciente, c.doc, c.cel, c.origen, c.estado])
    ];
    const csv = rows.map(r => r.join(',')).join('\n');
    const a = document.createElement('a');
    a.href = 'data:text/csv;charset=utf-8,\uFEFF' + encodeURIComponent(csv);
    a.download = 'citas_' + (this.filtroFecha || 'hoy') + '.csv';
    a.click();
  }

  getTituloTabla(): string {
    const medico = this.medicosOptions.find(m => m.id === this.filtroMedico);
    return `${medico?.nombre ?? 'Todos'} — ${this.formatFecha(this.filtroFecha) || 'Hoy'}`;
  }

  getBadgeClass(estado: string): string {
    switch (estado) {
      case 'CONFIRMADA': return 'badge-success';
      case 'ATENDIDA': return 'badge-info';
      case 'CANCELADA': return 'badge-danger';
      default: return 'badge-secondary';
    }
  }
}
