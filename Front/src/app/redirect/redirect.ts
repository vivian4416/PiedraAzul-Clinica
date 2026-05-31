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
    
    if (!this.auth.isLoggedIn()) {
      void this.auth.login(`${window.location.origin}/`);
      return;
    }
    if (this.auth.isAdmin()) {
      void this.router.navigate(['/configuracion'], { replaceUrl: true });
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
