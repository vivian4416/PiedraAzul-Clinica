import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, firstValueFrom } from 'rxjs';

import { environment } from '../../environments/environment';
import { CitaItem, CitasPorFechaResponse, FiltroCitas, MedicoOption, UsuarioMeResponse } from '../models/cita.model';

interface ApiResponse<T> {
  ok: boolean;
  data: T;
}

interface BackendMedico {
  id: string;
  nombres: string;
  especialidad: string;
  activo: boolean;
}

@Injectable({ providedIn: 'root' })
export class CitasService {
  private readonly http = inject(HttpClient);
  private readonly citasSubject = new BehaviorSubject<CitaItem[]>([]);
  private readonly medicosSubject = new BehaviorSubject<MedicoOption[]>([]);

  citas$ = this.citasSubject.asObservable();
  medicos$ = this.medicosSubject.asObservable();

  async cargarMedicos(): Promise<MedicoOption[]> {
    const response = await this.getApi<ApiResponse<BackendMedico[]>>('/medicos');
    const medicos = (response.data ?? [])
      .filter((medico) => medico.activo)
      .map((medico) => ({
        id: medico.id,
        nombre: medico.nombres.trim(),
        especialidad: medico.especialidad?.trim() || 'Sin especialidad',
      }));

    this.medicosSubject.next(medicos);
    return medicos;
  }

  async obtenerMiPerfil(): Promise<UsuarioMeResponse> {
    const response = await this.getApi<ApiResponse<UsuarioMeResponse>>('/usuarios/me');
    return response.data;
  }

  async cargarCitas(filtro: FiltroCitas): Promise<CitasPorFechaResponse> {
    const params = new HttpParams()
      .set('medicoId', filtro.medicoId)
      .set('fecha', filtro.fecha)
      .set('page', filtro.page)
      .set('size', filtro.size);

    const response = await this.getApi<ApiResponse<CitasPorFechaResponse>>('/citas', params);
    const data = response.data;
    const filtradas = this.filtrarLocalmente(data.citas ?? [], filtro.estado, filtro.origen);

    this.citasSubject.next(filtradas);

    return {
      ...data,
      citas: filtradas,
    };
  }

  getCitas(): CitaItem[] {
    return this.citasSubject.value;
  }

  getMedicos(): MedicoOption[] {
    return this.medicosSubject.value;
  }

  private filtrarLocalmente(citas: CitaItem[], estado: string, origen: string): CitaItem[] {
    return citas.filter((cita) => {
      const estadoMatch = !estado || cita.estado.toUpperCase() === estado.toUpperCase();
      const origenMatch = !origen || cita.origen.toUpperCase() === origen.toUpperCase();
      return estadoMatch && origenMatch;
    });
  }

  private async getApi<T>(path: string, params?: HttpParams): Promise<T> {
    return firstValueFrom(this.http.get<T>(`${environment.apiBaseUrl}${path}`, { params }));
  }
}