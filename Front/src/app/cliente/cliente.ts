import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Cliente } from './cliente.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-cliente',
  imports: [CommonModule, FormsModule],
  templateUrl: './cliente.html',
  styleUrl: './cliente.css',
})
export class ClienteComponent implements OnInit {
  private readonly apiBase = 'http://localhost:8090/api/v1';
  clientes: Cliente[] = [];

  descripcionActual: string = 'Selecciona una especialidad para ver su descripcion.';

  descripciones: { [key: string]: string } = {
    fisio:  'La fisioterapia ayuda a recuperar la movilidad y disminuir el dolor mediante ejercicios y técnicas manuales.',
    nutri:  'La nutrición terapéutica diseña planes alimentarios personalizados según tu estado de salud.',
    quiro:  'La quiropraxia se enfoca en el ajuste de la columna y articulaciones para mejorar postura y dolor.',
    neural: 'La terapia neural utiliza inyecciones en puntos específicos para modular el sistema nervioso y el dolor.'
  };

  formData = {
    nombre:   '',
    apellido: '',
    email:    '',
    telefono: '',
    tipoDocumento: '',
    numeroIdentificacion: '',
    genero: '',
    fechaNacimiento: '',
    password: '',
    terminos: false
  };

  errores = {
    nombre:   '',
    apellido: '',
    email:    '',
    telefono: '',
    tipoDocumento: '',
    numeroIdentificacion: '',
    genero: '',
    fechaNacimiento: '',
    password: '',
    terminos: ''
  };

  ngOnInit(): void {
    this.clientes = [
      { id: 1, nombre: 'Juan',   apellido: 'Perez', email: 'juan@email.com',   createAt: new Date() },
      { id: 2, nombre: 'Maria',  apellido: 'Gomez', email: 'maria@email.com',  createAt: new Date() },
      { id: 3, nombre: 'Carlos', apellido: 'Lopez', email: 'carlos@email.com', createAt: new Date() }
    ];
  }

  seleccionarEspecialidad(clave: string, event: Event): void {
    event.preventDefault();
    this.descripcionActual = this.descripciones[clave] || 'Descripcion no disponible.';
  }

  validarNombre(): boolean {
    if (this.formData.nombre.trim().length < 2) {
      this.errores.nombre = 'Nombre obligatorio minimo 2 caracteres';
      return false;
    }
    this.errores.nombre = '';
    return true;
  }

  validarApellido(): boolean {
    if (this.formData.apellido.trim().length < 2) {
      this.errores.apellido = 'Apellido obligatorio minimo 2 caracteres';
      return false;
    }
    this.errores.apellido = '';
    return true;
  }

  validarEmail(): boolean {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!regex.test(this.formData.email)) {
      this.errores.email = 'Correo electronico invalido';
      return false;
    }
    this.errores.email = '';
    return true;
  }

  validarTelefono(): boolean {
    if (this.formData.telefono.trim().length < 7) {
      this.errores.telefono = 'Telefono obligatorio minimo 7 digitos';
      return false;
    }
    this.errores.telefono = '';
    return true;
  }

  validarTipoDocumento(): boolean {
    if (!this.formData.tipoDocumento) {
      this.errores.tipoDocumento = 'Selecciona tu tipo de documento';
      return false;
    }
    this.errores.tipoDocumento = '';
    return true;
  }

  validarNumeroIdentificacion(): boolean {
    if (this.formData.numeroIdentificacion.trim().length < 5) {
      this.errores.numeroIdentificacion = 'Numero de identificacion obligatorio minimo 5 caracteres';
      return false;
    }
    this.errores.numeroIdentificacion = '';
    return true;
  }

  validarGenero(): boolean {
    if (!this.formData.genero) {
      this.errores.genero = 'Selecciona tu genero';
      return false;
    }
    this.errores.genero = '';
    return true;
  }

  validarFechaNacimiento(): boolean {
    if (!this.formData.fechaNacimiento) {
      this.errores.fechaNacimiento = 'Fecha de nacimiento obligatoria';
      return false;
    }
    this.errores.fechaNacimiento = '';
    return true;
  }

  validarPassword(): boolean {
    const value = this.formData.password.trim();
    if (!value) {
      this.errores.password = 'La contraseña es obligatoria';
      return false;
    }
    if (value.length < 6) {
      this.errores.password = 'La contraseña debe tener minimo 6 caracteres';
      return false;
    }
    this.errores.password = '';
    return true;
  }

  validarTerminos(): boolean {
    if (!this.formData.terminos) {
      this.errores.terminos = 'Debes aceptar los terminos y condiciones';
      return false;
    }
    this.errores.terminos = '';
    return true;
  }

  constructor(private readonly http: HttpClient) {}

  async onSubmit(): Promise<void> {
    const ok =
      this.validarNombre()   &&
      this.validarApellido() &&
      this.validarEmail()    &&
      this.validarTelefono() &&
      this.validarTipoDocumento() &&
      this.validarNumeroIdentificacion() &&
      this.validarGenero() &&
      this.validarFechaNacimiento() &&
      this.validarPassword() &&
      this.validarTerminos();

    if (!ok) return;

    const payload = {
      documento: this.formData.numeroIdentificacion.trim(),
      password: this.formData.password.trim(),
      nombres: this.formData.nombre.trim(),
      apellidos: this.formData.apellido.trim(),
      celular: this.formData.telefono.trim(),
      genero: this.toBackendGenero(this.formData.genero),
      fechaNacimiento: this.formData.fechaNacimiento || null,
      email: this.formData.email.trim(),
    };

    try {
      const response = await firstValueFrom(this.http.post<any>(`${this.apiBase}/auth/registro`, payload));
      const mensaje = response?.message || 'Registro exitoso. Bienvenido a Clinica Piedra Azul.';
      alert(mensaje);
      this.formData = { nombre: '', apellido: '', email: '', telefono: '', tipoDocumento: '', numeroIdentificacion: '', genero: '', fechaNacimiento: '', password: '', terminos: false };
      this.errores  = { nombre: '', apellido: '', email: '', telefono: '', tipoDocumento: '', numeroIdentificacion: '', genero: '', fechaNacimiento: '', password: '', terminos: '' };
    } catch (error) {
      const mensaje = this.getMensajeErrorRegistro(error);
      alert(mensaje);
    }
  }

  private toBackendGenero(value: string): string {
    const normalized = value.trim().toLowerCase();
    if (normalized === 'masculino') return 'HOMBRE';
    if (normalized === 'femenino') return 'MUJER';
    return 'OTRO';
  }

  private getMensajeErrorRegistro(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const backendMessage = (error.error && error.error.message) ? error.error.message : '';
      if (backendMessage) return backendMessage;
      if (error.status === 401) return 'No autenticado. Inicia sesion para registrar.';
      if (error.status === 403) return 'No tienes permisos para registrar pacientes.';
    }
    return 'No fue posible completar el registro. Intenta nuevamente.';
  }
}