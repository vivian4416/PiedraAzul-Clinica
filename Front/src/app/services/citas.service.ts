import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BehaviorSubject, firstValueFrom, timeout } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { Cita, Paciente, Medico } from '../models/cita.model';

interface ApiResponse<T> {
  ok: boolean;
  data: T;
  message?: string;
}

interface AuthResponse {
  ok: boolean;
  token: string;
  role: string;
}

interface BackendMedico {
  id: number;
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
  id: number;
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
  medicoId: number;
  medicoNombre: string;
  fecha: string;
  total: number;
  disponibles: number;
  citas: BackendCitaItem[];
  slots: BackendSlot[];
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
  id: number;
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
export class CitasService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private readonly apiBase = 'http://localhost:8090/api/v1';
  private readonly defaultLogin = { login: 'agendadora', password: atob('QWRtaW4xMjMh') };
  private readonly requestTimeoutMs = 12000;

  private token: string | null = null;
  private authPromise: Promise<void> | null = null;
  private initialized = false;
  private currentMedicoId = 1;
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
    if (this.initialized) {
      return;
    }

    if (!this.isBrowser) {
      this.initialized = true;
      return;
    }

    await this.obtenerConfiguracionAgendamiento(true);
    await this.cargarMedicos();
    const medicos = this.medicosSubject.value;
    const medicoBase = medicos.length > 0 ? medicos[0].id : 1;
    this.currentMedicoId = medicoBase;
    this.currentFecha = this.getTodayIso();
    await this.cargarCitasPorFiltro(medicoBase, this.currentFecha);
    await this.cargarSlots(medicoBase, this.currentFecha);
    await this.cargarDiasDisponibles(medicoBase, 0);
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

  async obtenerConfiguracionAgendamiento(forceRefresh = false): Promise<ConfiguracionAgendamiento> {
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

  async cargarCitasPorFiltro(medicoId: number, fecha: string): Promise<void> {
    if (!this.isBrowser) {
      return;
    }

    this.currentFecha = fecha;

    if (medicoId === 0) {
      const medicos = this.medicosSubject.value;
      if (medicos.length === 0) {
        await this.cargarMedicos();
      }

      const ids = this.medicosSubject.value.map(m => m.id);
      const listados = await Promise.all(ids.map(id => this.fetchCitas(id, fecha)));
      const merged = listados
        .flat()
        .sort((a, b) => a.hora.localeCompare(b.hora));
      this.citasSubject.next(merged);
      return;
    }

    this.currentMedicoId = medicoId;
    const citas = await this.fetchCitas(medicoId, fecha);
    this.citasSubject.next(citas);
  }

  async cargarSlots(medicoId: number, fecha: string): Promise<void> {
    if (!this.isBrowser) {
      return;
    }

    const params = new HttpParams()
      .set('medicoId', String(medicoId))
      .set('fecha', fecha);

    const data = await this.getApi<BackendSlotsResponse>('/citas/slots', params);
    const slots = data.slots ?? [];
    this.slotsBaseSubject.next(slots.map(s => s.hora));
    this.ocupadosSubject.next(slots.filter(s => !s.disponible).map(s => s.hora));
  }

  async cargarDiasDisponibles(medicoId: number, semanaOffset: number): Promise<void> {
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
        .set('medicoId', String(medicoId))
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

  async crearCitaManual(payload: {
    numDocumento: string;
    nombres: string;
    apellidos: string;
    celular: string;
    genero: string;
    fechaNacimiento?: string;
    email?: string;
    medicoId: number;
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
    medicoId: number;
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

  getCitasPorMedico(medicoId: number): Cita[] {
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

  private async fetchCitas(medicoId: number, fecha: string): Promise<Cita[]> {
    const params = new HttpParams()
      .set('medicoId', String(medicoId))
      .set('fecha', fecha);

    const data = await this.getApi<BackendCitasPorFecha>('/citas', params);

    return (data.citas ?? []).map(c => ({
      id: c.id,
      medico: medicoId,
      hora: c.hora,
      paciente: c.pacienteNombre,
      doc: c.pacienteDocumento,
      cel: c.pacienteCelular,
      estado: c.estado,
      origen: c.origen,
      fecha,
    }));
  }

  private async ensureAuthenticated(): Promise<void> {
    if (!this.isBrowser) {
      return;
    }

    if (this.token) {
      return;
    }

    this.authPromise ??= (async () => {
      console.debug('[CitasService] login tecnico iniciado');
      const auth = await firstValueFrom(this.http.post<AuthResponse>(
        `${this.apiBase}/auth/login`,
        this.defaultLogin
      ).pipe(timeout(this.requestTimeoutMs)));

      this.token = auth.token;
      console.debug('[CitasService] login tecnico completado');
    })().finally(() => {
      this.authPromise = null;
    });

    await this.authPromise;
  }

  private async getApi<T>(path: string, params?: HttpParams): Promise<T> {
    await this.ensureAuthenticated();
    const response = await firstValueFrom(this.http.get<ApiResponse<T>>(`${this.apiBase}${path}`, {
      headers: this.buildHeaders(),
      params,
    }).pipe(timeout(this.requestTimeoutMs)));
    return response.data;
  }

  private async postApi<T = unknown>(path: string, body: unknown): Promise<T> {
    await this.ensureAuthenticated();
    const startedAt = Date.now();
    const response = await firstValueFrom(this.http.post<ApiResponse<T>>(`${this.apiBase}${path}`, body, {
      headers: this.buildHeaders(),
    }).pipe(timeout(this.requestTimeoutMs)));
    const elapsed = Date.now() - startedAt;
    if (path === '/citas/autonoma') {
      console.debug(`[CitasService] POST ${path} completado en ${elapsed}ms`);
    }
    return response.data;
  }

  private async putApi<T = unknown>(path: string, body: unknown): Promise<T> {
    await this.ensureAuthenticated();
    const response = await firstValueFrom(this.http.put<ApiResponse<T>>(`${this.apiBase}${path}`, body, {
      headers: this.buildHeaders(),
    }).pipe(timeout(this.requestTimeoutMs)));
    return response.data;
  }

  private buildHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.token ?? ''}`,
      'Content-Type': 'application/json',
    });
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
