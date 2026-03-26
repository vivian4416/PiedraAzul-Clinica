export interface Cita {
  id: number;
  medico: number;
  hora: string;
  paciente: string;
  doc: string;
  cel: string;
  estado: 'ATENDIDA' | 'CONFIRMADA' | 'CANCELADA' | 'PENDIENTE';
  origen: 'MANUAL' | 'AUTONOMA';
  fecha?: string;
}

export interface Paciente {
  doc: string;
  nombres: string;
  apellidos: string;
  cel: string;
  genero: string;
  fnac?: string;
  email?: string;
}

export interface Medico {
  id: number;
  nombre: string;
  apellido: string;
  especialidad: string;
  descripcion: string;
  foto?: string;
}

export interface Slot {
  hora: string;
  disponible: boolean;
}

export interface SlotPaciente {
  dia: string;
  hora: string;
  medico: string;
}
