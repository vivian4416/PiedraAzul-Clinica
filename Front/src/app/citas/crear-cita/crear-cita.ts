import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../../services/citas.service';
import { Medico, Paciente } from '../../models/cita.model';

@Component({
  selector: 'app-crear-cita',
  imports: [CommonModule, FormsModule],
  templateUrl: './crear-cita.html',
  styleUrl: './crear-cita.css',
})
export class CrearCitaComponent implements OnInit {
  medicos: Medico[] = [];
  slots: string[] = [];
  slotOcupados: string[] = [];
  slotSeleccionado: string | null = null;
  mostrarSlots = false;
  mensajeDisponibilidad = '';
  submitted = false;

  readonly hoyIso = this.toIsoDate(new Date());
  readonly maxFechaNacimientoIso = this.getYesterdayIso();

  touched: Record<string, boolean> = {
    doc: false,
    nombres: false,
    apellidos: false,
    cel: false,
    genero: false,
    fnac: false,
    email: false,
    medico: false,
    fecha: false,
    slot: false,
  };

  formPaciente = {
    doc: '',
    nombres: '',
    apellidos: '',
    cel: '',
    fnac: '',
    email: '',
    genero: '',
  };

  formCita = {
    medico: '',
    fecha: '',
    hora: '',
  };

  pacienteEncontrado: Paciente | null = null;
  mostrarToast = false;
  tipoToast: 'success' | 'error' = 'success';
  mensajeToast = '';
  private slotsRequestSeq = 0;
  guardando = false;
  private toastTimeoutId: ReturnType<typeof setTimeout> | null = null;

  constructor(private readonly citasService: CitasService) {}

  ngOnInit(): void {
    void this.cargarInicial();
  }

  private async cargarInicial(): Promise<void> {
    await this.citasService.inicializar();
    this.medicos = this.citasService.getMedicos();
    this.slots = this.citasService.getSlots();
    this.slotOcupados = this.citasService.getSlotOcupados();
  }

  async buscarPaciente(): Promise<void> {
    const doc = this.formPaciente.doc.trim();
    if (this.validarDocumento(doc)) {
      this.pacienteEncontrado = null;
      return;
    }
    const paciente = await this.citasService.buscarPaciente(doc);
    
    if (paciente) {
      this.pacienteEncontrado = paciente;
      this.formPaciente.nombres = paciente.nombres;
      this.formPaciente.apellidos = paciente.apellidos;
      this.formPaciente.cel = paciente.cel;
      this.formPaciente.genero = paciente.genero;
    } else {
      this.pacienteEncontrado = null;
    }
  }

  async cargarSlots(): Promise<void> {
    const requestSeq = ++this.slotsRequestSeq;
    const medico = this.formCita.medico;
    const fecha = this.formCita.fecha;

    if (!medico || !fecha) {
      this.slots = [];
      this.slotOcupados = [];
      this.mensajeDisponibilidad = '';
      this.mostrarSlots = false;
      return;
    }

    const slotPrevio = this.slotSeleccionado;
    this.mostrarSlots = true;

    await this.citasService.cargarSlots(Number.parseInt(medico, 10), fecha);
    if (requestSeq !== this.slotsRequestSeq) {
      return;
    }

    this.slots = this.citasService.getSlots();
    this.slotOcupados = this.citasService.getSlotOcupados();

    const slotsDisponibles = this.slots.filter(slot => !this.slotOcupados.includes(slot));
    this.mensajeDisponibilidad = slotsDisponibles.length === 0
      ? 'No hay horarios disponibles para la fecha seleccionada. Prueba con otro día hábil.'
      : '';

    if (slotPrevio && slotsDisponibles.includes(slotPrevio)) {
      this.slotSeleccionado = slotPrevio;
      this.formCita.hora = slotPrevio;
    } else {
      this.slotSeleccionado = null;
      this.formCita.hora = '';
    }
  }

  async onFechaCitaChange(fecha: string): Promise<void> {
    this.formCita.fecha = fecha;
    this.touched['fecha'] = true;
    await this.cargarSlots();
  }

  async onFechaCitaInput(event: Event): Promise<void> {
    const target = event.target as HTMLInputElement | null;
    const fecha = target?.value ?? '';
    await this.onFechaCitaChange(fecha);
  }

  async onMedicoChange(medico: string): Promise<void> {
    this.formCita.medico = medico;
    this.touched['medico'] = true;
    await this.cargarSlots();
  }

  onDocumentoChange(value: string): void {
    this.formPaciente.doc = value.trim();
    this.touched['doc'] = true;
  }

  onCelularChange(value: string): void {
    this.formPaciente.cel = value.trim();
    this.touched['cel'] = true;
  }

  onTextoChange(field: 'nombres' | 'apellidos', value: string): void {
    this.formPaciente[field] = value;
    this.touched[field] = true;
  }

  onBlur(field: string): void {
    this.touched[field] = true;
  }

  seleccionarSlot(hora: string): void {
    if (!this.slotOcupados.includes(hora)) {
      this.slotSeleccionado = hora;
      this.formCita.hora = hora;
      this.actualizarResumen();
    }
  }

  esSlotOcupado(hora: string): boolean {
    return this.slotOcupados.includes(hora);
  }

  actualizarResumen(): void {
    // Se actualiza automáticamente
  }

  async crearCita(): Promise<void> {
    if (this.guardando) {
      return;
    }

    this.guardando = true;
    this.submitted = true;
    if (!this.formularioValido()) {
      this.mostrarNotificacion('error', 'Corrige los campos marcados en rojo para continuar.');
      this.guardando = false;
      return;
    }

    try {
      const horaSeleccionada = this.slotSeleccionado;
      if (!horaSeleccionada) {
        this.mostrarNotificacion('error', 'Debes seleccionar un horario disponible.');
        this.guardando = false;
        return;
      }

      await this.citasService.crearCitaManual({
        numDocumento: this.formPaciente.doc,
        nombres: this.formPaciente.nombres,
        apellidos: this.formPaciente.apellidos,
        celular: this.formPaciente.cel,
        genero: this.formPaciente.genero,
        fechaNacimiento: this.formPaciente.fnac,
        email: this.formPaciente.email,
        medicoId: Number.parseInt(this.formCita.medico, 10),
        fecha: this.formCita.fecha,
        hora: horaSeleccionada,
      });

      this.guardando = false;
      this.mostrarNotificacion('success', '¡Cita creada exitosamente en base de datos!');
      this.resetFormulario();
      this.slots = this.citasService.getSlots();
      this.slotOcupados = this.citasService.getSlotOcupados();
    } catch {
      this.mostrarNotificacion('error', 'No se pudo crear la cita. Verifica disponibilidad o conexión con backend.');
    } finally {
      this.guardando = false;
    }
  }

  resetFormulario(): void {
    this.formPaciente = {
      doc: '',
      nombres: '',
      apellidos: '',
      cel: '',
      fnac: '',
      email: '',
      genero: '',
    };
    this.formCita = {
      medico: '',
      fecha: '',
      hora: '',
    };
    this.pacienteEncontrado = null;
    this.mostrarSlots = false;
    this.slotSeleccionado = null;
    this.mensajeDisponibilidad = '';
    this.submitted = false;
    this.touched = {
      doc: false,
      nombres: false,
      apellidos: false,
      cel: false,
      genero: false,
      fnac: false,
      email: false,
      medico: false,
      fecha: false,
      slot: false,
    };
  }

  mostrarNotificacion(tipo: 'success' | 'error', mensaje: string): void {
    if (this.toastTimeoutId) {
      clearTimeout(this.toastTimeoutId);
      this.toastTimeoutId = null;
    }

    this.tipoToast = tipo;
    this.mensajeToast = mensaje;
    this.mostrarToast = true;

    this.toastTimeoutId = setTimeout(() => {
      this.mostrarToast = false;
      this.toastTimeoutId = null;
    }, 3000);
  }

  getNombreMedico(): string {
    const medico = this.medicos.find(m => m.id === Number.parseInt(this.formCita.medico, 10));
    return medico ? `${medico.nombre} ${medico.apellido}` : '';
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${Number.parseInt(day, 10)} ${meses[Number.parseInt(m, 10)]} ${y}`;
  }

  mostrarError(campo: string): boolean {
    return !!this.mensajeError(campo);
  }

  mensajeError(campo: string): string {
    const visible = this.submitted || this.touched[campo];
    if (!visible) {
      return '';
    }

    if (campo === 'doc') return this.validarDocumento(this.formPaciente.doc);
    if (campo === 'nombres') return this.validarNombre(this.formPaciente.nombres, 'El nombre');
    if (campo === 'apellidos') return this.validarNombre(this.formPaciente.apellidos, 'El apellido');
    if (campo === 'cel') return this.validarCelular(this.formPaciente.cel);
    if (campo === 'genero') return this.validarGenero(this.formPaciente.genero);
    if (campo === 'fnac') return this.validarFechaNacimiento(this.formPaciente.fnac);
    if (campo === 'email') return this.validarEmail(this.formPaciente.email);
    if (campo === 'medico') return this.validarMedico(this.formCita.medico);
    if (campo === 'fecha') return this.validarFechaCita(this.formCita.fecha);
    if (campo === 'slot') return this.validarSlot();
    return '';
  }

  puedeCrearCita(): boolean {
    return this.formularioValido();
  }

  private formularioValido(): boolean {
    return !this.validarDocumento(this.formPaciente.doc)
      && !this.validarNombre(this.formPaciente.nombres, 'El nombre')
      && !this.validarNombre(this.formPaciente.apellidos, 'El apellido')
      && !this.validarCelular(this.formPaciente.cel)
      && !this.validarGenero(this.formPaciente.genero)
      && !this.validarFechaNacimiento(this.formPaciente.fnac)
      && !this.validarEmail(this.formPaciente.email)
      && !this.validarMedico(this.formCita.medico)
      && !this.validarFechaCita(this.formCita.fecha)
      && !this.validarSlot();
  }

  private validarDocumento(value: string): string {
    const doc = value.trim();
    if (!doc) return 'El documento de identidad es obligatorio.';
    if (!/^\d+$/.test(doc)) return 'El documento solo puede contener números.';
    if (Number(doc) <= 0) return 'El documento debe ser un número positivo.';
    return '';
  }

  private validarNombre(value: string, etiqueta: string): string {
    const text = value.trim();
    if (!text) return `${etiqueta} es obligatorio.`;
    if (!/^[A-Za-zÁÉÍÓÚÜÑÇáéíóúüñç\s]+$/.test(text)) {
      return `${etiqueta} solo puede contener letras (A-Z), incluyendo ñ y ç.`;
    }
    return '';
  }

  private validarCelular(value: string): string {
    const cel = value.trim();
    if (!cel) return 'El celular es obligatorio.';
    if (!/^\d+$/.test(cel)) return 'El celular solo puede contener números.';
    if (cel.length !== 10) return 'El celular debe tener exactamente 10 dígitos.';
    if (Number(cel) <= 0) return 'El celular debe ser un número positivo.';
    return '';
  }

  private validarGenero(value: string): string {
    return value ? '' : 'Debes seleccionar un género.';
  }

  private validarFechaNacimiento(value: string): string {
    if (!value) return '';
    return value < this.hoyIso
      ? ''
      : 'La fecha de nacimiento debe ser menor a la fecha actual.';
  }

  private validarEmail(value: string): string {
    const email = value.trim();
    if (!email) return '';
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
    return regex.test(email)
      ? ''
      : 'El correo electrónico no es válido. Ejemplo: nombre@dominio.com';
  }

  private validarMedico(value: string): string {
    return value ? '' : 'Debes seleccionar un médico.';
  }

  private validarFechaCita(value: string): string {
    if (!value) return 'La fecha de la cita es obligatoria.';
    return value >= this.hoyIso
      ? ''
      : 'La fecha de la cita debe ser igual o mayor a la fecha actual.';
  }

  private validarSlot(): string {
    if (!this.formCita.medico || !this.formCita.fecha) {
      return '';
    }

    const slotsDisponibles = this.slots.filter(slot => !this.slotOcupados.includes(slot));
    if (slotsDisponibles.length === 0) {
      return 'No hay horarios disponibles para la fecha seleccionada.';
    }

    return this.slotSeleccionado
      ? ''
      : 'Debes seleccionar un horario disponible.';
  }

  private toIsoDate(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  private getYesterdayIso(): string {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    return this.toIsoDate(yesterday);
  }
}
