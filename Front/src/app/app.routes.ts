import { Routes } from '@angular/router';
import { ClienteComponent } from './cliente/cliente';
import { CitasComponent } from './citas/citas';
import { CrearCitaComponent } from './citas/crear-cita/crear-cita';
import { AgendarCitaComponent } from './citas/agendar-cita/agendar-cita';
import { ConfiguracionComponent } from './citas/configuracion/configuracion';
import { RedirectComponent } from './redirect/redirect';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', component: RedirectComponent, pathMatch: 'full' },
  { path: 'clientes', component: ClienteComponent, canActivate: [authGuard] },
  { path: 'citas', component: CitasComponent, canActivate: [authGuard] },
  { path: 'crear-cita', component: CrearCitaComponent, canActivate: [authGuard] },
  { path: 'agendar-cita', component: AgendarCitaComponent, canActivate: [authGuard] },
  { path: 'configuracion', component: ConfiguracionComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];
