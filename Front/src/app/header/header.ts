import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  public nombreClinica: string = 'Piedra Azul';
  public subtitulo: string = 'Sistema de citas';
  public usuario: string = 'Maria Aguilar';
  public rol: string = 'Agendador';
}