import { Injectable } from '@angular/core';
import { Capacitor } from '@capacitor/core';
import { Browser } from '@capacitor/browser';
import Keycloak from 'keycloak-js';

import { environment } from '../../environments/environment';

interface TokenClaims {
  sub?: string;
  name?: string;
  given_name?: string;
  family_name?: string;
  preferred_username?: string;
  realm_access?: {
    roles?: string[];
  };
}

interface SessionSnapshot {
  accessToken: string;
  refreshToken: string;
  idToken: string;
  storedAt: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private keycloak: any = null;
  private initPromise: Promise<boolean> | null = null;
  private readonly storageKey = 'piedrazul.mobile.session';

  async init(): Promise<boolean> {
    if (this.initPromise) {
      return this.initPromise;
    }

    if (typeof window === 'undefined') {
      return false;
    }

    this.keycloak = new Keycloak({
      url: environment.keycloak.url,
      realm: environment.keycloak.realm,
      clientId: environment.keycloak.clientId,
    });

    this.initPromise = this.keycloak.init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      checkLoginIframe: false,
      responseMode: 'query',
      redirectUri: this.getRedirectUri(),
    }).then(async (authenticated: boolean) => {
      this.persistSession();
      return authenticated;
    }).catch((error: unknown) => {
      console.error('Keycloak init failed', error);
      return false;
    });

    return this.initPromise ?? Promise.resolve(false);
  }

  async login(): Promise<void> {
    await this.ensureInitialized();

    if (!this.keycloak) {
      throw new Error('AuthService not initialized');
    }

    const loginUrl = await this.keycloak.createLoginUrl({
      redirectUri: this.getRedirectUri(),
      pkceMethod: 'S256',
    });

    if (Capacitor.isNativePlatform()) {
      await Browser.open({ url: loginUrl });
      return;
    }

    await this.keycloak.login({
      redirectUri: this.getRedirectUri(),
      pkceMethod: 'S256',
    });
  }

  async logout(): Promise<void> {
    await this.ensureInitialized();

    if (!this.keycloak) {
      throw new Error('AuthService not initialized');
    }

    this.clearStoredSession();
    await this.keycloak.logout({ redirectUri: this.getRedirectUri() });
  }

  async getToken(): Promise<string | null> {
    await this.ensureInitialized();

    if (!this.keycloak) {
      return this.readStoredSession()?.accessToken ?? null;
    }

    try {
      await this.keycloak.updateToken(30);
      this.persistSession();
      return this.keycloak.token ?? this.readStoredSession()?.accessToken ?? null;
    } catch (error) {
      console.error('Token refresh failed', error);
      return this.keycloak.token ?? this.readStoredSession()?.accessToken ?? null;
    }
  }

  isLoggedIn(): boolean {
    if (this.keycloak?.authenticated) {
      return true;
    }

    return Boolean(this.readStoredSession()?.accessToken);
  }

  getDisplayName(): string {
    const claims = this.getClaims();
    if (!claims) {
      return '';
    }

    const fullName = [claims.name, [claims.given_name, claims.family_name].filter(Boolean).join(' ').trim()]
      .find((value) => Boolean(value?.trim()))
      ?.trim();

    return fullName || claims.preferred_username || '';
  }

  getUsername(): string {
    return this.getClaims()?.preferred_username ?? '';
  }

  getRoleLabel(): string {
    const role = this.getPrimaryRole();

    switch (role) {
      case 'ADMIN':
        return 'Administrador';
      case 'AGENDADOR':
        return 'Agendador';
      case 'MEDICO':
        return 'Médico';
      case 'PACIENTE':
        return 'Paciente';
      default:
        return '';
    }
  }

  getPrimaryRole(): string {
    const roles = this.getRealmRoles();

    if (roles.some((role) => ['administrador', 'admin'].includes(role.toLowerCase()))) {
      return 'ADMIN';
    }

    if (roles.some((role) => role.toLowerCase() === 'agendador')) {
      return 'AGENDADOR';
    }

    if (roles.some((role) => role.toLowerCase() === 'medico')) {
      return 'MEDICO';
    }

    if (roles.some((role) => role.toLowerCase() === 'paciente')) {
      return 'PACIENTE';
    }

    return '';
  }

  hasRole(role: string): boolean {
    const normalized = role.trim().toLowerCase();
    return this.getRealmRoles().some((currentRole) => currentRole.trim().toLowerCase() === normalized);
  }

  async getUserId(): Promise<string> {
    await this.ensureInitialized();
    return this.getClaims()?.sub ?? '';
  }

  private async ensureInitialized(): Promise<void> {
    if (!this.initPromise) {
      await this.init();
      return;
    }

    await this.initPromise;
  }

  private getRedirectUri(): string {
    if (Capacitor.isNativePlatform()) {
      return `${environment.mobileAppScheme}://login`;
    }

    return `${window.location.origin}/login`;
  }

  private getRealmRoles(): string[] {
    const claims = this.getClaims();
    return claims?.realm_access?.roles ?? [];
  }

  private getClaims(): TokenClaims | null {
    const parsed = this.keycloak?.tokenParsed as TokenClaims | undefined;
    if (parsed) {
      return parsed;
    }

    const stored = this.readStoredSession();
    if (!stored?.accessToken) {
      return null;
    }

    return this.decodeJwt(stored.accessToken);
  }

  private persistSession(): void {
    if (!this.keycloak) {
      return;
    }

    const snapshot: SessionSnapshot = {
      accessToken: this.keycloak.token ?? '',
      refreshToken: this.keycloak.refreshToken ?? '',
      idToken: this.keycloak.idToken ?? '',
      storedAt: Date.now(),
    };

    if (snapshot.accessToken) {
      localStorage.setItem(this.storageKey, JSON.stringify(snapshot));
    } else {
      this.clearStoredSession();
    }
  }

  private readStoredSession(): SessionSnapshot | null {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as SessionSnapshot;
    } catch {
      return null;
    }
  }

  private clearStoredSession(): void {
    localStorage.removeItem(this.storageKey);
  }

  private decodeJwt(token: string): TokenClaims | null {
    const segments = token.split('.');
    if (segments.length < 2) {
      return null;
    }

    try {
      const payload = atob(segments[1].replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(payload) as TokenClaims;
    } catch {
      return null;
    }
  }
}