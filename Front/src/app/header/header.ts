import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-header',
  imports: [RouterModule],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  public nombreClinica: string = 'Piedra Azul';
  public subtitulo: string = 'Sistema de citas';
  public usuario: string = 'Maria Aguilar';
  public rol: string = 'Agendador';
}