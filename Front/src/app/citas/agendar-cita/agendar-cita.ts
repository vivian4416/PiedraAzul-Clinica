import { ChangeDetectorRef, Component, NgZone, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { CitasService, DiaDisponible } from '../../services/citas.service';
import { Medico, Paciente } from '../../models/cita.model';

@Component({
  selector: 'app-agendar-cita',
  imports: [CommonModule, FormsModule],
  templateUrl: './agendar-cita.html',
  styleUrl: './agendar-cita.css',
})
export class AgendarCitaComponent implements OnInit {
  modoActual: 'paciente' | 'agendador' = 'paciente';

  medicos: Medico[] = [];
  medicoSeleccionado = '';

  diasDisponibles: DiaDisponible[] = [];
  slotSeleccionado: { fecha: string; label: string; hora: string } | null = null;

  documentoPaciente = '';
  celularPaciente = '';
  pacienteRegistrado: Paciente | null = null;
  errorPaciente = '';

  mostrarConfirmacion = false;
  semanaSeleccionada = 0;
  semanasDisponibles: number[] = [0, 1, 2, 3];

  cargando = false;
  guardando = false;
  solicitudEnCurso = false;
  mostrarToast = false;
  mensajeToast = '';
  private toastTimeoutId: ReturnType<typeof setTimeout> | null = null;
  private guardandoTimeoutId: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private readonly citasService: CitasService,
    private readonly router: Router,
    private readonly ngZone: NgZone,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    void this.cargarInicial();
  }

  private async cargarInicial(): Promise<void> {
    this.cargando = true;
    this.errorPaciente = '';

    try {
      await this.citasService.inicializar();
      const configuracion = await this.citasService.obtenerConfiguracionAgendamiento();
      this.semanasDisponibles = Array.from({ length: configuracion.ventanaSemanas }, (_, idx) => idx);
      this.medicos = this.citasService.getMedicos();

      const primerMedico = this.medicos[0];
      this.medicoSeleccionado = primerMedico ? String(primerMedico.id) : '';
      await this.cargarDisponibilidad();
    } catch {
      this.diasDisponibles = [];
      this.errorPaciente = 'No fue posible cargar la disponibilidad. Verifica la conexión con el backend.';
    } finally {
      this.cargando = false;
      this.syncUi();
    }
  }

  async onMedicoChange(medicoId: string): Promise<void> {
    this.medicoSeleccionado = medicoId;
    this.cancelarSeleccion();
    await this.cargarDisponibilidad();
  }

  async seleccionarSemana(semana: number): Promise<void> {
    this.semanaSeleccionada = semana;
    this.cancelarSeleccion();
    await this.cargarDisponibilidad();
  }

  seleccionarSlot(dia: DiaDisponible, hora: string): void {
    this.slotSeleccionado = {
      fecha: dia.fecha,
      label: dia.label,
      hora,
    };
    this.mostrarConfirmacion = true;
  }

  cancelarSeleccion(): void {
    this.slotSeleccionado = null;
    this.mostrarConfirmacion = false;
  }

  async confirmarCita(): Promise<void> {
    if (!this.slotSeleccionado) {
      return;
    }

    if (!this.pacienteRegistrado) {
      this.errorPaciente = 'Debes validar un paciente registrado antes de confirmar la cita.';
      return;
    }

    if (this.solicitudEnCurso) {
      return;
    }

    this.guardando = true;
    this.solicitudEnCurso = true;
    this.errorPaciente = '';
    const traceId = `web-cita-${Date.now()}`;
    console.debug(`[${traceId}] iniciar confirmarCita`);

    if (this.guardandoTimeoutId) {
      clearTimeout(this.guardandoTimeoutId);
      this.guardandoTimeoutId = null;
    }

    this.guardandoTimeoutId = setTimeout(() => {
      if (!this.solicitudEnCurso) {
        return;
      }
      this.guardando = false;
      this.solicitudEnCurso = false;
      this.errorPaciente = 'La confirmación tardó más de lo esperado. Verifica en la tabla si la cita se creó e intenta recargar.';
      console.warn(`[${traceId}] failsafe: guardando forzado a false por timeout UI`);
      this.guardandoTimeoutId = null;
      this.syncUi();
    }, 15000);

    const medicoId = Number.parseInt(this.medicoSeleccionado, 10);
    const fecha = this.slotSeleccionado.fecha;
    const hora = this.slotSeleccionado.hora;
    const resumen = {
      label: this.slotSeleccionado.label,
      hora: this.slotSeleccionado.hora,
    };

    // Libera el estado visual del boton inmediatamente.
    this.guardando = false;

    this.enviarCitaEnSegundoPlano(traceId, {
      numDocumento: this.pacienteRegistrado.doc,
      nombres: this.pacienteRegistrado.nombres,
      apellidos: this.pacienteRegistrado.apellidos,
      celular: this.pacienteRegistrado.cel,
      genero: this.pacienteRegistrado.genero,
      fechaNacimiento: this.pacienteRegistrado.fnac,
      email: this.pacienteRegistrado.email,
      medicoId,
      fecha,
      hora,
    }, resumen).catch((err: unknown) => {
      console.error(`[${traceId}] error inesperado en enviarCitaEnSegundoPlano`, err);
      if (this.guardandoTimeoutId) {
        clearTimeout(this.guardandoTimeoutId);
        this.guardandoTimeoutId = null;
      }
      this.solicitudEnCurso = false;
      this.guardando = false;
      this.errorPaciente = 'Error inesperado al confirmar. Verifica si la cita fue creada.';
      this.syncUi();
    });
  }

  async validarPacienteRegistrado(): Promise<void> {
    const doc = this.documentoPaciente.trim();
    const cel = this.celularPaciente.trim();

    this.pacienteRegistrado = null;
    this.errorPaciente = '';

    if (!doc || !/^\d+$/.test(doc)) {
      this.errorPaciente = 'Ingresa un documento válido para continuar.';
      return;
    }

    if (!cel || !/^\d{10}$/.test(cel)) {
      this.errorPaciente = 'Ingresa el celular registrado (10 dígitos).';
      return;
    }

    try {
      const paciente = await this.citasService.buscarPaciente(doc);
      if (!paciente) {
        this.errorPaciente = 'No encontramos ese paciente. Debes estar registrado para agendar por la web.';
        return;
      }

      if (paciente.cel !== cel) {
        this.errorPaciente = 'El celular no coincide con el registro del paciente.';
        return;
      }

      this.pacienteRegistrado = paciente;
    } catch {
      this.errorPaciente = 'No fue posible validar el registro del paciente.';
    }
  }

  irModoAgendador(): void {
    this.modoActual = 'agendador';
    void this.router.navigate(['/crear-cita']);
  }

  getSemanaLabel(semanaOffset: number): string {
    const start = this.getDateOffset(semanaOffset * 7);
    const end = this.getDateOffset((semanaOffset * 7) + 6);
    return `${this.formatDiaMes(start)} - ${this.formatDiaMes(end)}`;
  }

  getNombreMedico(): string {
    const id = Number.parseInt(this.medicoSeleccionado, 10);
    const medico = this.medicos.find(m => m.id === id);
    if (!medico) {
      return 'Selecciona un médico';
    }
    return `Dr/Dra. ${medico.nombre} ${medico.apellido}`.trim();
  }

  getSubtituloDisponibilidad(): string {
    return `${this.getNombreMedico()} · Sem. ${this.getSemanaLabel(this.semanaSeleccionada)}`;
  }

  puedeConfirmar(): boolean {
    return !!this.slotSeleccionado && !!this.pacienteRegistrado && !this.solicitudEnCurso;
  }

  private async enviarCitaEnSegundoPlano(
    traceId: string,
    payload: {
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
    },
    resumen: { label: string; hora: string }
  ): Promise<void> {
    try {
      await this.citasService.crearCitaAutonoma(payload);
      console.debug(`[${traceId}] cita guardada en backend`, { medicoId: payload.medicoId, fecha: payload.fecha, hora: payload.hora });

      this.mensajeToast = `Cita web confirmada para ${resumen.label} a las ${resumen.hora}.`;
      this.mostrarToast = true;

      if (this.toastTimeoutId) {
        clearTimeout(this.toastTimeoutId);
        this.toastTimeoutId = null;
      }

      this.toastTimeoutId = setTimeout(() => {
        this.mostrarToast = false;
        this.toastTimeoutId = null;
        this.syncUi();
      }, 3200);

      this.cancelarSeleccion();
      void this.cargarDisponibilidad()
        .then(() => console.debug(`[${traceId}] disponibilidad recargada`))
        .catch(() => console.warn(`[${traceId}] fallo recargando disponibilidad`));
    } catch (error: unknown) {
      console.error(`[${traceId}] error al confirmar cita`, error);
      this.errorPaciente = this.getMensajeErrorGuardado(error);
    } finally {
      if (this.guardandoTimeoutId) {
        clearTimeout(this.guardandoTimeoutId);
        this.guardandoTimeoutId = null;
      }
      this.solicitudEnCurso = false;
      this.guardando = false;
      console.debug(`[${traceId}] fin confirmarCita, solicitudEnCurso=false`);
      this.syncUi();
    }
  }

  private async cargarDisponibilidad(): Promise<void> {
    const medicoId = Number.parseInt(this.medicoSeleccionado, 10);

    this.slotSeleccionado = null;
    this.mostrarConfirmacion = false;
    this.errorPaciente = '';

    if (!medicoId) {
      this.diasDisponibles = [];
      return;
    }

    this.cargando = true;
    try {
      await this.citasService.cargarDiasDisponibles(medicoId, this.semanaSeleccionada);
      this.diasDisponibles = [...this.citasService.getDiasDisponibles()];
    } catch {
      this.diasDisponibles = [];
      this.errorPaciente = 'No se pudo actualizar la disponibilidad para la semana seleccionada.';
    } finally {
      this.cargando = false;
      this.syncUi();
    }
  }

  private syncUi(): void {
    this.ngZone.run(() => {
      try {
        this.cdr.detectChanges();
      } catch {
        // No-op si la vista ya fue destruida.
      }
    });
  }

  private getDateOffset(offsetDays: number): Date {
    const date = new Date();
    date.setDate(date.getDate() + offsetDays);
    return date;
  }

  private formatDiaMes(date: Date): string {
    const day = date.getDate();
    const month = new Intl.DateTimeFormat('es-CO', { month: 'short' })
      .format(date)
      .replace('.', '');
    const monthCap = month.charAt(0).toUpperCase() + month.slice(1);
    return `${day} ${monthCap}`;
  }

  private getMensajeErrorGuardado(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const backendMessage = this.extraerMensajeBackend(error.error);
      if (backendMessage) {
        return backendMessage;
      }

      if (error.status === 0) {
        return 'No fue posible conectar con el backend para guardar la cita.';
      }
      if (error.status === 409) {
        return 'El horario seleccionado ya no está disponible. Elige otro horario.';
      }
      if (error.status === 400) {
        return 'Datos inválidos para crear la cita. Verifica la información del paciente.';
      }
    }

    return 'No se pudo crear la cita web. Intenta nuevamente en unos segundos.';
  }

  private extraerMensajeBackend(payload: unknown): string | null {
    if (typeof payload !== 'object' || payload === null || !('message' in payload)) {
      return null;
    }

    const message = (payload as { message?: unknown }).message;
    return typeof message === 'string' && message.trim() ? message.trim() : null;
  }
}
