import { Routes } from '@angular/router';
import { ClienteComponent } from './cliente/cliente';
import { CitasComponent } from './citas/citas';
import { CrearCitaComponent } from './citas/crear-cita/crear-cita';
import { AgendarCitaComponent } from './citas/agendar-cita/agendar-cita';
import { ConfiguracionComponent } from './citas/configuracion/configuracion';

export const routes: Routes = [
  { path: '', redirectTo: '/citas', pathMatch: 'full' },
  { path: 'clientes', component: ClienteComponent },
  { path: 'citas', component: CitasComponent },
  { path: 'crear-cita', component: CrearCitaComponent },
  { path: 'agendar-cita', component: AgendarCitaComponent },
  { path: 'configuracion', component: ConfiguracionComponent }
];