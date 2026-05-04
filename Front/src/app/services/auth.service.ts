import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

export type AppRol = 'ADMIN' | 'MEDICO' | 'PACIENTE' | '';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private keycloak: Keycloak | null = null;
  private loginInFlight = false;

  private readonly url = 'http://localhost:8080';
  private readonly realm = 'PiedraAzul_Realm';
  private readonly clientId = 'FrontAngular';

  async init(): Promise<boolean> {
    if (typeof window === 'undefined') return false;
    this.keycloak = new Keycloak({ url: this.url, realm: this.realm, clientId: this.clientId });
    try {
      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        checkLoginIframe: false,
      });
      return authenticated as boolean;
    } catch (err) {
      console.error('Keycloak init failed', err);
      return false;
    }
  }

  async login(redirectUri?: string): Promise<void> {
    if (!this.keycloak) throw new Error('AuthService not initialized');
    if (this.loginInFlight) return;
    this.loginInFlight = true;
    try {
      await this.keycloak.login({ redirectUri: redirectUri ?? window.location.href });
    } finally {
      this.loginInFlight = false;
    }
  }

  logout() {
    if (!this.keycloak) throw new Error('AuthService not initialized');
    this.keycloak.logout({ redirectUri: window.location.origin });
  }

  async getToken(): Promise<string | null> {
    if (!this.keycloak) return null;
    try {
      await this.keycloak.updateToken(30);
      return this.keycloak.token ?? null;
    } catch (err) {
      console.error('Failed to refresh token', err);
      return this.keycloak.token ?? null;
    }
  }

  isLoggedIn(): boolean {
    return !!(this.keycloak && this.keycloak.authenticated);
  }

  hasRole(role: string): boolean {
    if (!this.keycloak || !this.keycloak.tokenParsed) return false;
    const parsed: any = this.keycloak.tokenParsed;
    const realmRoles: string[] = parsed?.realm_access?.roles ?? [];
    return realmRoles.includes(role);
  }

  getAppRol(): AppRol {
    if (this.hasRole('administrador') || this.hasRole('agendador')) return 'ADMIN';
    if (this.hasRole('medico')) return 'MEDICO';
    if (this.hasRole('paciente')) return 'PACIENTE';
    return '';
  }

  getFullName(): string {
    if (!this.keycloak || !this.keycloak.tokenParsed) return '';
    const parsed: any = this.keycloak.tokenParsed;
    const name = parsed?.name || '';
    if (name) return name;
    const given = parsed?.given_name || '';
    const family = parsed?.family_name || '';
    return `${given} ${family}`.trim() || parsed?.preferred_username || '';
  }

  getUsername(): string {
    if (!this.keycloak || !this.keycloak.tokenParsed) return '';
    const parsed: any = this.keycloak.tokenParsed;
    return parsed?.preferred_username || '';
  }

  getRolLabel(): string {
    const rol = this.getAppRol();
    switch (rol) {
      case 'ADMIN': return 'Administrador';
      case 'MEDICO': return 'Médico';
      case 'PACIENTE': return 'Paciente';
      default: return '';
    }
  }
}
