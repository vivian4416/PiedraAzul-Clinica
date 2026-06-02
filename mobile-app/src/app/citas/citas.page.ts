import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  IonBadge,
  IonButton,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardSubtitle,
  IonCardTitle,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonSelect,
  IonSelectOption,
  IonSpinner,
  IonText,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

import { AuthService } from '../services/auth.service';
import { CitasService } from '../services/citas.service';
import { CitaItem, FiltroCitas, MedicoOption, UsuarioMeResponse } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  templateUrl: './citas.page.html',
  styleUrls: ['./citas.page.scss'],
  imports: [
    CommonModule,
    FormsModule,
    IonBadge,
    IonButton,
    IonCard,
    IonCardContent,
    IonCardHeader,
    IonCardSubtitle,
    IonCardTitle,
    IonContent,
    IonHeader,
    IonInput,
    IonItem,
    IonLabel,
    IonList,
    IonSelect,
    IonSelectOption,
    IonSpinner,
    IonText,
    IonTitle,
    IonToolbar,
  ],
})
export class CitasPage implements OnInit {
  readonly pageSizeOptions = [10, 25, 50];
  readonly stateOptions = ['CONFIRMADA', 'ATENDIDA', 'CANCELADA', 'PENDIENTE'];
  readonly originOptions = ['MANUAL', 'AUTONOMA'];

  loading = true;
  errorMessage = '';
  usuario: UsuarioMeResponse | null = null;
  medicos: MedicoOption[] = [];
  citas: CitaItem[] = [];
  totalRegistros = 0;
  totalPaginas = 0;
  paginaActual = 0;
  totalFiltrado = 0;
  filtros: FiltroCitas = this.createInitialFilters();

  constructor(
    private readonly auth: AuthService,
    private readonly citasService: CitasService,
    private readonly router: Router,
  ) {}

  async ngOnInit(): Promise<void> {
    await this.cargarInicial();
  }

  async recargar(): Promise<void> {
    await this.cargarCitas();
  }

  async buscar(): Promise<void> {
    this.filtros.page = 0;
    await this.cargarCitas();
  }

  async paginaAnterior(): Promise<void> {
    if (this.paginaActual <= 0) {
      return;
    }

    this.filtros.page -= 1;
    await this.cargarCitas();
  }

  async paginaSiguiente(): Promise<void> {
    if (this.paginaActual + 1 >= this.totalPaginas) {
      return;
    }

    this.filtros.page += 1;
    await this.cargarCitas();
  }

  async logout(): Promise<void> {
    await this.auth.logout();
    await this.router.navigateByUrl('/login', { replaceUrl: true });
  }

  formatHora(value: string): string {
    return value?.slice(0, 5) ?? '';
  }

  formatFecha(value: string): string {
    if (!value) {
      return '';
    }

    const [year, month, day] = value.split('-');
    return `${day}/${month}/${year}`;
  }

  get medicoSeleccionado(): MedicoOption | undefined {
    return this.medicos.find((medico) => medico.id === this.filtros.medicoId);
  }

  private async cargarInicial(): Promise<void> {
    this.loading = true;
    this.errorMessage = '';

    try {
      await this.auth.init();
      this.usuario = await this.citasService.obtenerMiPerfil();
      this.medicos = await this.citasService.cargarMedicos();

      if (this.usuario.rol?.toUpperCase() === 'MEDICO') {
        this.filtros.medicoId = this.usuario.id;
      } else {
        this.filtros.medicoId = this.medicos[0]?.id ?? '';
      }

      if (!this.filtros.medicoId) {
        throw new Error('No hay medicos activos para consultar');
      }

      await this.cargarCitas();
    } catch {
      this.errorMessage = 'No se pudo cargar la información. Verifica que el backend y Keycloak estén activos.';
      this.citas = [];
      this.totalRegistros = 0;
      this.totalPaginas = 0;
      this.totalFiltrado = 0;
    } finally {
      this.loading = false;
    }
  }

  private async cargarCitas(): Promise<void> {
    if (!this.filtros.medicoId) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    try {
      const respuesta = await this.citasService.cargarCitas(this.filtros);
      this.citas = respuesta.citas;
      this.totalRegistros = respuesta.total;
      this.totalPaginas = respuesta.totalPaginas;
      this.paginaActual = respuesta.pagina;
      this.totalFiltrado = respuesta.citas.length;
    } catch {
      this.errorMessage = 'No fue posible consultar las citas con los filtros elegidos.';
      this.citas = [];
      this.totalRegistros = 0;
      this.totalPaginas = 0;
      this.totalFiltrado = 0;
    } finally {
      this.loading = false;
    }
  }

  private createInitialFilters(): FiltroCitas {
    return {
      medicoId: '',
      fecha: new Date().toISOString().slice(0, 10),
      page: 0,
      size: 25,
      estado: '',
      origen: '',
    };
  }
}