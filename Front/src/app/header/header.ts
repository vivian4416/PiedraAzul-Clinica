import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  public nombreClinica = 'Piedra Azul';
  public subtitulo = 'Sistema de citas';

  constructor(private readonly auth: AuthService) {}

  get nombreUsuario(): string {
    return this.auth.getFullName() || this.auth.getUsername();
  }

  get rolLabel(): string {
    return this.auth.getRolLabel();
  }

  get initiales(): string {
    const parts = this.nombreUsuario.trim().split(' ').filter(p => p.length > 0);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return 'U';
  }

  get esPaciente(): boolean { return this.auth.getAppRol() === 'PACIENTE'; }
  get esMedico(): boolean { return this.auth.getAppRol() === 'MEDICO'; }
  get esAdmin(): boolean { return this.auth.getAppRol() === 'ADMIN'; }
  get esAgendador(): boolean { return this.auth.getAppRol() === 'AGENDADOR'; }

  logout(): void {
    this.auth.logout();
  }
}
