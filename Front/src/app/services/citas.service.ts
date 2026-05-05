import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { BehaviorSubject, firstValueFrom, timeout } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { Cita, Paciente, Medico } from '../models/cita.model';

interface ApiResponse<T> {
  ok: boolean;
  data: T;
  message?: string;
}

interface UsuarioMeResponse {
  id: string;
  login: string;
  nombreCompleto: string;
  apellido: string;
  email: string;
  rol: string;
  activo: boolean;
  documento: string | null;
  celular: string | null;
}

interface BackendMedico {
  id: string;
  nombres: string;
  tipo?: string;
  especialidad: string;
  intervaloMin?: number;
  activo: boolean;
}

interface BackendDisponibilidadConfig {
  diaSemana: number;
  horaInicio: string;
  horaFin: string;
}

interface BackendMedicoConfiguracion {
  id: string;
  nombres: string;
  tipo: string;
  especialidad: string;
  activo: boolean;
  intervaloMin: number;
  disponibilidad: BackendDisponibilidadConfig[];
}

interface BackendConfiguracionAgendamiento {
  ventanaSemanas: number;
  medicos: BackendMedicoConfiguracion[];
}

interface BackendPaciente {
  numDocumento: string;
  nombres: string;
  apellidos: string;
  celular: string;
  genero: string;
  fechaNacimiento?: string;
  email?: string;
}

interface BackendPacienteAutocomplete extends BackendPaciente {}

interface BackendSlot {
  hora: string;
  disponible: boolean;
}

interface BackendCitaItem {
  id: number;
  hora: string;
  estado: 'ATENDIDA' | 'CONFIRMADA' | 'CANCELADA' | 'PENDIENTE';
  origen: 'MANUAL' | 'AUTONOMA';
  pacienteId: number;
  pacienteNombre: string;
  pacienteDocumento: string;
  pacienteCelular: string;
}

interface BackendCitasPorFecha {
  medicoId: string;
  medicoNombre: string;
  fecha: string;
  total: number;
  totalRegistros: number;
  pagina: number;
  tamanio: number;
  totalPaginas: number;
  disponibles: number;
  citas: BackendCitaItem[];
  slots: BackendSlot[];
}

export interface PaginacionCitas {
  total: number;
  pagina: number;
  tamanio: number;
  totalPaginas: number;
}

interface BackendSlotsResponse {
  slots: BackendSlot[];
  disponibles: number;
  ocupados: number;
}

export interface DiaDisponible {
  fecha: string;
  label: string;
  horas: string[];
}

export interface DisponibilidadConfig {
  diaSemana: number;
  horaInicio: string;
  horaFin: string;
}

export interface MedicoConfiguracion {
  id: string;
  nombres: string;
  tipo: string;
  especialidad: string;
  activo: boolean;
  intervaloMin: number;
  disponibilidad: DisponibilidadConfig[];
}

export interface ConfiguracionAgendamiento {
  ventanaSemanas: number;
  medicos: MedicoConfiguracion[];
}

@Injectable({
  providedIn: 'root'
})
// Orquesta llamadas a la API y estado local de citas, medicos y disponibilidad.
export class CitasService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private readonly apiBase = 'http://localhost:8090/api/v1';
  private readonly requestTimeoutMs = 12000;
  private initialized = false;
  private currentMedicoId = '';
  private currentFecha = this.getTodayIso();

  private readonly citasSubject = new BehaviorSubject<Cita[]>([]);
  private readonly pacientesSubject = new BehaviorSubject<{ [key: string]: Paciente }>({});
  private readonly medicosSubject = new BehaviorSubject<Medico[]>([]);
  private readonly slotsBaseSubject = new BehaviorSubject<string[]>([]);
  private readonly ocupadosSubject = new BehaviorSubject<string[]>([]);
  private readonly diasDisponiblesSubject = new BehaviorSubject<DiaDisponible[]>([]);
  private readonly configuracionSubject = new BehaviorSubject<ConfiguracionAgendamiento | null>(null);

  citas$ = this.citasSubject.asObservable();
  pacientes$ = this.pacientesSubject.asObservable();
  medicos$ = this.medicosSubject.asObservable();
  slotsBase$ = this.slotsBaseSubject.asObservable();
  ocupados$ = this.ocupadosSubject.asObservable();
  diasDisponibles$ = this.diasDisponiblesSubject.asObservable();
  configuracion$ = this.configuracionSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  async inicializar(): Promise<void> {
    if (this.initialized && this.medicosSubject.value.length > 0) {
      return;
    }

    if (!this.isBrowser) {
      this.initialized = true;
      return;
    }

    // Carga medicos: primero intenta via configuracion, luego via listado simple.
    try {
      await this.obtenerConfiguracionAgendamiento(true);
    } catch {
      try {
        await this.cargarMedicos();
      } catch {
        // Sin conexion o sin autenticacion: medicos quedara vacio.
      }
    }

    if (this.medicosSubject.value.length === 0) {
      try {
        await this.cargarMedicos();
      } catch {
        // Intento final fallido: se notificara al componente via medicos vacio.
      }
    }

    const medicos = this.medicosSubject.value;
    const medicoBase = medicos.length > 0 ? medicos[0].id : '';
    this.currentMedicoId = medicoBase;
    this.currentFecha = this.getTodayIso();

    // PACIENTE no puede listar citas (403); se maneja silenciosamente.
    if (medicoBase) {
      try {
        await this.cargarCitasPorFiltro(medicoBase, this.currentFecha);
      } catch {
        this.citasSubject.next([]);
      }

      try {
        await this.cargarSlots(medicoBase, this.currentFecha);
      } catch {
        // Sin slots iniciales; se cargan cuando el usuario selecciona medico/fecha.
      }

      try {
        await this.cargarDiasDisponibles(medicoBase, 0);
      } catch {
        // Sin dias disponibles iniciales.
      }
    }

    this.initialized = true;
  }

  async cargarMedicos(): Promise<void> {
    if (!this.isBrowser) {
      return;
    }

    const data = await this.getApi<BackendMedico[]>('/medicos');
    const medicos = data
      .filter(m => m.activo)
      .map(m => {
        const [nombre, ...resto] = m.nombres.trim().split(' ');
        return {
          id: m.id,
          nombre,
          apellido: resto.join(' ').trim(),
          especialidad: m.especialidad,
          descripcion: `${m.nombres} - ${m.especialidad}`,
        } as Medico;
      });

    this.medicosSubject.next(medicos);
  }

  async obtenerMiPerfil(): Promise<UsuarioMeResponse> {
    if (!this.isBrowser) {
      throw new Error('obtenerMiPerfil called outside browser');
    }
    return this.getApi<UsuarioMeResponse>('/usuarios/me');
  }

  async obtenerConfiguracionAgendamiento(forceRefresh = false): Promise<ConfiguracionAgendamiento> {
    if (!this.isBrowser) {
      throw new Error('obtenerConfiguracionAgendamiento called outside browser');
    }

    const cache = this.configuracionSubject.value;
    if (!forceRefresh && cache) {
      return cache;
    }

    const data = await this.getApi<BackendConfiguracionAgendamiento>('/medicos/configuracion');
    const configuracion = this.mapConfiguracion(data);
    this.configuracionSubject.next(configuracion);

    const medicosActivos = configuracion.medicos
      .filter(m => m.activo)
      .map(m => this.toMedicoListItem(m));
    this.medicosSubject.next(medicosActivos);

    return configuracion;
  }

  async guardarConfiguracionAgendamiento(configuracion: ConfiguracionAgendamiento): Promise<ConfiguracionAgendamiento> {
    const payload = {
      ventanaSemanas: configuracion.ventanaSemanas,
      medicos: configuracion.medicos.map(m => ({
        id: m.id,
        intervaloMin: m.intervaloMin,
        activo: m.activo,
        disponibilidad: m.disponibilidad.map(d => ({
          diaSemana: d.diaSemana,
          horaInicio: d.horaInicio,
          horaFin: d.horaFin,
        })),
      })),
    };

    const data = await this.putApi<BackendConfiguracionAgendamiento>('/medicos/configuracion', payload);
    const nuevaConfiguracion = this.mapConfiguracion(data);
    this.configuracionSubject.next(nuevaConfiguracion);

    const medicosActivos = nuevaConfiguracion.medicos
      .filter(m => m.activo)
      .map(m => this.toMedicoListItem(m));
    this.medicosSubject.next(medicosActivos);

    return nuevaConfiguracion;
  }

  async cargarCitasPorFiltro(
    medicoId: string,
    fecha: string,
    page = 0,
    size = 25
  ): Promise<PaginacionCitas> {
    if (!this.isBrowser) {
      return { total: 0, pagina: 0, tamanio: size, totalPaginas: 0 };
    }

    this.currentFecha = fecha;

    if (!medicoId) {
      const medicos = this.medicosSubject.value;
      if (medicos.length === 0) {
        await this.cargarMedicos();
      }

      const ids = this.medicosSubject.value.map(m => m.id);
      const listados = await Promise.all(ids.map(id => this.fetchCitas(id, fecha, 0, 200)));
      const merged = listados
        .map(r => r.citas)
        .flat()
        .sort((a, b) => a.hora.localeCompare(b.hora));
      this.citasSubject.next(merged);
      return {
        total: merged.length,
        pagina: 0,
        tamanio: merged.length,
        totalPaginas: merged.length > 0 ? 1 : 0,
      };
    }

    this.currentMedicoId = medicoId;
    const resultado = await this.fetchCitas(medicoId, fecha, page, size);
    this.citasSubject.next(resultado.citas);
    return {
      total: resultado.total,
      pagina: resultado.pagina,
      tamanio: resultado.tamanio,
      totalPaginas: resultado.totalPaginas,
    };
  }

  async cargarSlots(medicoId: string, fecha: string): Promise<void> {
    if (!this.isBrowser) {
      return;
    }

    const params = new HttpParams()
      .set('medicoId', medicoId)
      .set('fecha', fecha);

    const data = await this.getApi<BackendSlotsResponse>('/citas/slots', params);
    const slots = data.slots ?? [];
    this.slotsBaseSubject.next(slots.map(s => s.hora));
    this.ocupadosSubject.next(slots.filter(s => !s.disponible).map(s => s.hora));
  }

  async cargarDiasDisponibles(medicoId: string, semanaOffset: number): Promise<void> {
    if (!this.isBrowser) {
      return;
    }

    const configuracion = await this.obtenerConfiguracionAgendamiento();
    const ventanaSemanas = configuracion.ventanaSemanas;
    const dias: DiaDisponible[] = [];
    const inicio = new Date();
    inicio.setHours(0, 0, 0, 0);
    inicio.setDate(inicio.getDate() + (semanaOffset * 7));

    const maxFecha = new Date();
    maxFecha.setHours(0, 0, 0, 0);
    maxFecha.setDate(maxFecha.getDate() + (ventanaSemanas * 7));

    for (let i = 0; i < 7; i++) {
      const fecha = new Date(inicio);
      fecha.setDate(inicio.getDate() + i);
      if (fecha > maxFecha) {
        break;
      }

      const fechaIso = this.toLocalIsoDate(fecha);

      const params = new HttpParams()
        .set('medicoId', medicoId)
        .set('fecha', fechaIso);

      try {
        const data = await this.getApi<BackendSlotsResponse>('/citas/slots', params);
        const disponibles = (data.slots ?? []).filter(s => s.disponible).map(s => s.hora);
        if (disponibles.length > 0) {
          dias.push({
            fecha: fechaIso,
            label: this.formatDiaLabel(fecha),
            horas: disponibles,
          });
        }
      } catch {
        // Si no hay disponibilidad en un dia, se omite del tablero.
      }
    }

    this.diasDisponiblesSubject.next(dias);
  }

  async buscarPaciente(doc: string): Promise<Paciente | undefined> {
    if (!this.isBrowser) {
      return undefined;
    }

    const trimmed = doc.trim();
    if (!trimmed) {
      return undefined;
    }

    const cache = this.pacientesSubject.value[trimmed];
    if (cache) {
      return cache;
    }

    const params = new HttpParams().set('documento', trimmed);

    try {
      const data = await this.getApi<BackendPaciente | null>('/pacientes', params);
      if (!data) {
        return undefined;
      }

      const mapped: Paciente = {
        doc: data.numDocumento,
        nombres: data.nombres,
        apellidos: data.apellidos,
        cel: data.celular,
        genero: this.fromBackendGenero(data.genero),
        fnac: data.fechaNacimiento,
        email: data.email,
      };

      this.agregarPaciente(mapped.doc, mapped);
      return mapped;
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 404) {
        return undefined;
      }
      throw error;
    }
  }

  async buscarPacientesSugeridos(doc: string): Promise<Paciente[]> {
    if (!this.isBrowser) {
      return [];
    }

    const trimmed = doc.trim();
    if (!trimmed) {
      return [];
    }

    const params = new HttpParams().set('documento', trimmed);
    const data = await this.getApi<BackendPacienteAutocomplete[]>('/pacientes/sugerencias', params);

    return (data ?? []).map(item => ({
      doc: item.numDocumento,
      nombres: item.nombres,
      apellidos: item.apellidos,
      cel: item.celular,
      genero: this.fromBackendGenero(item.genero),
      fnac: item.fechaNacimiento,
      email: item.email,
    }));
  }

  async crearCitaManual(payload: {
    numDocumento: string;
    nombres: string;
    apellidos: string;
    celular: string;
    genero: string;
    fechaNacimiento?: string;
    email?: string;
    password?: string;
    medicoId: string;
    fecha: string;
    hora: string;
  }): Promise<void> {
    if (!this.isBrowser) {
      return;
    }

    await this.postApi('/citas', {
      numDocumento: payload.numDocumento.trim(),
      nombres: payload.nombres.trim(),
      apellidos: payload.apellidos.trim(),
      celular: payload.celular.trim(),
      genero: this.toBackendGenero(payload.genero),
      fechaNacimiento: payload.fechaNacimiento || null,
      email: payload.email || null,
      password: payload.password?.trim() || null,
      medicoId: payload.medicoId,
      fecha: payload.fecha,
      hora: payload.hora,
    });

    // Refresca en segundo plano para no bloquear la confirmacion visual del formulario.
    void (async () => {
      try {
        await this.cargarCitasPorFiltro(payload.medicoId, payload.fecha);
        await this.cargarSlots(payload.medicoId, payload.fecha);
      } catch {
        // Si falla la recarga post-creacion, no invalida la cita ya creada.
      }
    })();
  }

  async crearCitaAutonoma(payload: {
    numDocumento: string;
    nombres: string;
    apellidos: string;
    celular: string;
    genero: string;
    fechaNacimiento?: string;
    email?: string;
    password?: string;
    medicoId: string;
    fecha: string;
    hora: string;
  }): Promise<void> {
    if (!this.isBrowser) {
      return;
    }

    await this.postApi('/citas/autonoma', {
      numDocumento: payload.numDocumento.trim(),
      nombres: payload.nombres.trim(),
      apellidos: payload.apellidos.trim(),
      celular: payload.celular.trim(),
      genero: this.toBackendGenero(payload.genero),
      fechaNacimiento: payload.fechaNacimiento || null,
      email: payload.email || null,
      password: payload.password?.trim() || null,
      medicoId: payload.medicoId,
      fecha: payload.fecha,
      hora: payload.hora,
    });

    void (async () => {
      try {
        await this.cargarCitasPorFiltro(payload.medicoId, payload.fecha);
        await this.cargarSlots(payload.medicoId, payload.fecha);
      } catch {
        // Si falla la recarga post-creacion, no invalida la cita ya creada.
      }
    })();
  }

  getCitas(): Cita[] {
    return this.citasSubject.value;
  }

  getCitasPorMedico(medicoId: string): Cita[] {
    return this.citasSubject.value.filter(c => c.medico === medicoId);
  }

  getCitasPorEstado(estado: string): Cita[] {
    return this.citasSubject.value.filter(c => c.estado === estado);
  }

  getEstadisticas() {
    const citas = this.citasSubject.value;
    return {
      total: citas.length,
      confirmadas: citas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: citas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: citas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  getPaciente(doc: string): Paciente | undefined {
    return this.pacientesSubject.value[doc];
  }

  agregarPaciente(doc: string, paciente: Paciente): void {
    this.pacientesSubject.next({
      ...this.pacientesSubject.value,
      [doc]: paciente,
    });
  }

  crearCita(cita: Cita): void {
    this.citasSubject.next([...this.citasSubject.value, cita]);
  }

  getMedicos(): Medico[] {
    return this.medicosSubject.value;
  }

  getSlots(): string[] {
    return this.slotsBaseSubject.value;
  }

  getSlotOcupados(): string[] {
    return this.ocupadosSubject.value;
  }

  getDiasDisponibles(): DiaDisponible[] {
    return this.diasDisponiblesSubject.value;
  }

  getVentanaSemanas(): number {
    return this.configuracionSubject.value?.ventanaSemanas ?? 4;
  }

  getConfiguracionAgendamiento(): ConfiguracionAgendamiento | null {
    return this.configuracionSubject.value;
  }

  private async fetchCitas(medicoId: string, fecha: string, page: number, size: number): Promise<{
    citas: Cita[];
    total: number;
    pagina: number;
    tamanio: number;
    totalPaginas: number;
  }> {
    const params = new HttpParams()
      .set('medicoId', medicoId)
      .set('fecha', fecha)
      .set('page', page)
      .set('size', size);

    const data = await this.getApi<BackendCitasPorFecha>('/citas', params);

    return {
      citas: (data.citas ?? []).map(c => ({
        id: c.id,
        medico: medicoId,
        hora: c.hora,
        paciente: c.pacienteNombre,
        doc: c.pacienteDocumento,
        cel: c.pacienteCelular,
        estado: c.estado,
        origen: c.origen,
        fecha,
      })),
      total: data.total,
      pagina: data.pagina,
      tamanio: data.tamanio,
      totalPaginas: data.totalPaginas,
    };
  }

  private async getApi<T>(path: string, params?: HttpParams): Promise<T> {
    const response = await firstValueFrom(this.http.get<ApiResponse<T>>(`${this.apiBase}${path}`, {
      params,
    }).pipe(timeout(this.requestTimeoutMs)));
    return response.data;
  }

  private async postApi<T = unknown>(path: string, body: unknown): Promise<T> {
    const startedAt = Date.now();
    const response = await firstValueFrom(this.http.post<ApiResponse<T>>(`${this.apiBase}${path}`, body, {
    }).pipe(timeout(this.requestTimeoutMs)));
    const elapsed = Date.now() - startedAt;
    if (path === '/citas/autonoma') {
      console.debug(`[CitasService] POST ${path} completado en ${elapsed}ms`);
    }
    return response.data;
  }

  private async putApi<T = unknown>(path: string, body: unknown): Promise<T> {
    const response = await firstValueFrom(this.http.put<ApiResponse<T>>(`${this.apiBase}${path}`, body, {
    }).pipe(timeout(this.requestTimeoutMs)));
    return response.data;
  }

  private getTodayIso(): string {
    return this.toLocalIsoDate(new Date());
  }

  private toLocalIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private formatDiaLabel(fecha: Date): string {
    const weekday = new Intl.DateTimeFormat('es-CO', { weekday: 'long' }).format(fecha);
    const month = new Intl.DateTimeFormat('es-CO', { month: 'short' }).format(fecha).replace('.', '');
    const day = fecha.getDate();
    const weekdayCap = weekday.charAt(0).toUpperCase() + weekday.slice(1);
    const monthCap = month.charAt(0).toUpperCase() + month.slice(1);
    return `${weekdayCap} ${day} ${monthCap}`;
  }

  private mapConfiguracion(data: BackendConfiguracionAgendamiento): ConfiguracionAgendamiento {
    return {
      ventanaSemanas: data.ventanaSemanas,
      medicos: (data.medicos ?? []).map(m => ({
        id: m.id,
        nombres: m.nombres,
        tipo: m.tipo,
        especialidad: m.especialidad,
        activo: m.activo,
        intervaloMin: m.intervaloMin,
        disponibilidad: (m.disponibilidad ?? []).map(d => ({
          diaSemana: d.diaSemana,
          horaInicio: d.horaInicio.slice(0, 5),
          horaFin: d.horaFin.slice(0, 5),
        })),
      })),
    };
  }

  private toMedicoListItem(medico: MedicoConfiguracion): Medico {
    const [nombre, ...resto] = medico.nombres.trim().split(' ');
    return {
      id: medico.id,
      nombre,
      apellido: resto.join(' ').trim(),
      especialidad: medico.especialidad,
      descripcion: `${medico.nombres} - ${medico.especialidad}`,
    };
  }

  private toBackendGenero(genero: string): 'HOMBRE' | 'MUJER' | 'OTRO' {
    const normalized = genero.trim().toUpperCase();
    if (normalized === 'HOMBRE') return 'HOMBRE';
    if (normalized === 'MUJER') return 'MUJER';
    return 'OTRO';
  }

  private fromBackendGenero(genero: string): string {
    const normalized = genero.trim().toUpperCase();
    if (normalized === 'HOMBRE') return 'Hombre';
    if (normalized === 'MUJER') return 'Mujer';
    return 'Otro';
  }
}
