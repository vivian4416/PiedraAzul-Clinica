import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header implements OnInit {
  public nombreClinica = 'Piedra Azul';
  public subtitulo = 'Sistema de citas';
  public nombreUsuario = '';
  public rolLabel = '';

  constructor(private readonly auth: AuthService) {}

  ngOnInit(): void {
    this.nombreUsuario = this.auth.getFullName() || this.auth.getUsername();
    this.rolLabel = this.auth.getRolLabel();
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

  logout(): void {
    this.auth.logout();
  }
}
