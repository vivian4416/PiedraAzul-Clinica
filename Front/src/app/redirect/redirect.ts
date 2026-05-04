import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-redirect',
  template: '',
  standalone: true,
})
export class RedirectComponent implements OnInit {
  constructor(private readonly auth: AuthService, private readonly router: Router) {}

  ngOnInit(): void {
    // Evita disparar login desde esta ruta para no crear bucles de redireccion.
    // Las rutas protegidas se encargan de autenticar via guard.
    if (!this.auth.isLoggedIn()) {
      void this.router.navigate(['/citas'], { replaceUrl: true });
      return;
    }
    const rol = this.auth.getAppRol();
    if (rol === 'PACIENTE') {
      void this.router.navigate(['/agendar-cita'], { replaceUrl: true });
    } else {
      void this.router.navigate(['/citas'], { replaceUrl: true });
    }
  }
}
