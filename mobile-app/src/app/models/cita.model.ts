export interface MedicoOption {
  id: string;
  nombre: string;
  especialidad: string;
}

export interface CitaItem {
  id: number;
  hora: string;
  estado: string;
  origen: string;
  createdAt?: string;
  pacienteId?: number;
  pacienteNombre: string;
  pacienteDocumento: string;
  pacienteCelular: string;
}

export interface CitasPorFechaResponse {
  medicoId: string;
  medicoNombre: string;
  fecha: string;
  total: number;
  totalRegistros: number;
  pagina: number;
  tamanio: number;
  totalPaginas: number;
  disponibles: number;
  citas: CitaItem[];
  slots: string[];
}

export interface UsuarioMeResponse {
  id: string;
  login: string;
  nombreCompleto: string;
  apellido: string;
  email: string;
  rol: string;
  activo: boolean;
  documento?: string | null;
  celular?: string | null;
}

export interface FiltroCitas {
  medicoId: string;
  fecha: string;
  page: number;
  size: number;
  estado: string;
  origen: string;
}