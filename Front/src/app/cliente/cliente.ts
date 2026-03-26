import { Component, OnInit } from '@angular/core';
import { Cliente } from './cliente.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-cliente',
  imports: [CommonModule, FormsModule],
  templateUrl: './cliente.html',
  styleUrl: './cliente.css',
})
export class ClienteComponent implements OnInit {
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

  validarTerminos(): boolean {
    if (!this.formData.terminos) {
      this.errores.terminos = 'Debes aceptar los terminos y condiciones';
      return false;
    }
    this.errores.terminos = '';
    return true;
  }

  onSubmit(): void {
    const ok =
      this.validarNombre()   &&
      this.validarApellido() &&
      this.validarEmail()    &&
      this.validarTelefono() &&
      this.validarTipoDocumento() &&
      this.validarNumeroIdentificacion() &&
      this.validarGenero() &&
      this.validarFechaNacimiento() &&
      this.validarTerminos();

    if (ok) {
      alert('Registro exitoso. Bienvenido a Clinica Piedra Azul.');
      this.formData = { nombre: '', apellido: '', email: '', telefono: '', tipoDocumento: '', numeroIdentificacion: '', genero: '', fechaNacimiento: '', terminos: false };
      this.errores  = { nombre: '', apellido: '', email: '', telefono: '', tipoDocumento: '', numeroIdentificacion: '', genero: '', fechaNacimiento: '', terminos: '' };
    }
  }
}