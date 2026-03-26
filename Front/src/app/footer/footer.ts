import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  imports: [],
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class Footer {
  public nombreClinica: string = 'Clinica Piedra Azul';
  public anio: string = '2026';
  public horario: string = 'Jornada continua';
}