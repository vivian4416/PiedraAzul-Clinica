import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../../services/citas.service';
import { Medico } from '../../models/cita.model';

interface MedicoConfig {
  id: number;
  nombre: string;
  apellido: string;
  especialidad: string;
  activo: boolean;
}

@Component({
  selector: 'app-configuracion',
  imports: [CommonModule, FormsModule],
  templateUrl: './configuracion.html',
  styleUrl: './configuracion.css',
})
export class ConfiguracionComponent implements OnInit {
  diasSemana = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];
  diasActivos: { [key: string]: boolean } = {
    Lunes: true,
    Martes: true,
    Miércoles: true,
    Jueves: true,
    Viernes: true,
    Sábado: false,
    Domingo: false,
  };

  configuracion = {
    horaInicio: '08:00',
    horaFin: '18:00',
    intervaloSlot: 20,
    sitio: 'Popayán',
    ubicacion: 'Calle 5 # 3-25',
  };

  medicos: MedicoConfig[] = [
    { id: 1, nombre: 'Dr. Andrés', apellido: 'Mora', especialidad: 'Quiropraxia', activo: true },
    { id: 2, nombre: 'Dra. Carolina', apellido: 'Ríos', especialidad: 'Fisioterapia', activo: true },
    { id: 3, nombre: 'Dr. Luis', apellido: 'Pérez', especialidad: 'Terapia Neural', activo: true },
  ];

  mostrarFormMedico = false;
  medicoEditando: any = null;
  formularioMedico = {
    nombre: '',
    apellido: '',
    especialidad: '',
  };

  constructor(private readonly citasService: CitasService) {}

  ngOnInit(): void {
    void this.cargarInicial();
  }

  private async cargarInicial(): Promise<void> {
    await this.citasService.inicializar();
    const medicosBackend = this.citasService.getMedicos();
    if (medicosBackend.length > 0) {
      this.medicos = medicosBackend.map((m: Medico) => ({
        id: m.id,
        nombre: m.nombre,
        apellido: m.apellido,
        especialidad: m.especialidad,
        activo: true,
      }));
    }
  }

  toggleDia(dia: string): void {
    this.diasActivos[dia] = !this.diasActivos[dia];
  }

  abrirFormularioMedico(): void {
    this.mostrarFormMedico = true;
    this.medicoEditando = null;
    this.formularioMedico = { nombre: '', apellido: '', especialidad: '' };
  }

  cerrarFormulario(): void {
    this.mostrarFormMedico = false;
    this.medicoEditando = null;
  }

  guardarMedico(): void {
    if (!this.formularioMedico.nombre || !this.formularioMedico.apellido || !this.formularioMedico.especialidad) {
      alert('Por favor completa todos los campos');
      return;
    }

    if (this.medicoEditando) {
      // Actualizar médico existente
      this.medicoEditando.nombre = this.formularioMedico.nombre;
      this.medicoEditando.apellido = this.formularioMedico.apellido;
      this.medicoEditando.especialidad = this.formularioMedico.especialidad;
    } else {
      // Crear nuevo médico
      const nuevoId = Math.max(...this.medicos.map(m => m.id), 0) + 1;
      this.medicos.push({
        id: nuevoId,
        nombre: this.formularioMedico.nombre,
        apellido: this.formularioMedico.apellido,
        especialidad: this.formularioMedico.especialidad,
        activo: true,
      });
    }

    this.cerrarFormulario();
    alert('Médico guardado exitosamente');
  }

  editarMedico(medico: any): void {
    this.medicoEditando = medico;
    this.formularioMedico = {
      nombre: medico.nombre,
      apellido: medico.apellido,
      especialidad: medico.especialidad,
    };
    this.mostrarFormMedico = true;
  }

  eliminarMedico(id: number): void {
    if (confirm('¿Está seguro de que desea eliminar este médico?')) {
      this.medicos = this.medicos.filter(m => m.id !== id);
    }
  }

  toggleActivo(medico: any): void {
    medico.activo = !medico.activo;
  }

  guardarConfiguracion(): void {
    alert('Configuración guardada exitosamente');
  }
}
