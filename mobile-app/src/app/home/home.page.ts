import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { IonButton, IonCard, IonCardContent, IonCardHeader, IonCardSubtitle, IonCardTitle, IonContent, IonText } from '@ionic/angular/standalone';

import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
  imports: [CommonModule, IonButton, IonCard, IonCardContent, IonCardHeader, IonCardSubtitle, IonCardTitle, IonContent, IonText],
})
export class HomePage implements OnInit {
  loading = true;
  loggedIn = false;
  displayName = '';
  roleLabel = '';
  errorMessage = '';

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
  ) {}

  async ngOnInit(): Promise<void> {
    try {
      await this.auth.init();
      this.refreshState();
      if (this.loggedIn) {
        await this.router.navigateByUrl('/citas', { replaceUrl: true });
      }
    } catch {
      this.errorMessage = 'No fue posible inicializar Keycloak. Verifica la URL base y el clientId.';
    } finally {
      this.loading = false;
    }
  }

  async login(): Promise<void> {
  this.errorMessage = '';
  this.loading = true;

  try {
    await this.auth.login();
  } catch (err) {
    alert('Error login: ' + JSON.stringify(err) + ' | ' + (err as any)?.message);
    this.errorMessage = 'No se pudo abrir el flujo de login con Keycloak.';
    this.loading = false;
  }
}

  async logout(): Promise<void> {
    this.loading = true;
    try {
      await this.auth.logout();
      this.refreshState();
    } finally {
      this.loading = false;
    }
  }

  private refreshState(): void {
    this.loggedIn = this.auth.isLoggedIn();
    this.displayName = this.auth.getDisplayName();
    this.roleLabel = this.auth.getRoleLabel();
  }

  async goToCitas(): Promise<void> {
    await this.router.navigateByUrl('/citas', { replaceUrl: true });
  }
}
