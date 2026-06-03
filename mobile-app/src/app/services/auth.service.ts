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
    if (this.initPromise) return this.initPromise;
    if (typeof window === 'undefined') return false;

    this.keycloak = new Keycloak({
      url: environment.keycloak.url,
      realm: environment.keycloak.realm,
      clientId: environment.keycloak.clientId,
    });

    this.initPromise = (async () => {
      const stored = this.readStoredSession();

      if (stored?.accessToken) {
        try {
          this.keycloak.token = stored.accessToken;
          this.keycloak.refreshToken = stored.refreshToken;
          this.keycloak.idToken = stored.idToken;
          this.keycloak.tokenParsed = this.decodeJwt(stored.accessToken) as any;
          this.keycloak.authenticated = true;
          return true;
        } catch {
          this.clearStoredSession();
        }
      }

      if (Capacitor.isNativePlatform()) {
        return false;
      }

      try {
        await this.keycloak.init({
          onLoad: 'check-sso',
          pkceMethod: 'S256',
          checkLoginIframe: false,
          responseMode: 'query',
          redirectUri: this.getRedirectUri(),
        });
        return this.keycloak.authenticated ?? false;
      } catch (err) {
        console.error('Keycloak init failed', err);
        return false;
      }
    })();

    return this.initPromise;
  }

  async login(): Promise<void> {
    await this.ensureInitialized();

    if (!this.keycloak) {
      throw new Error('AuthService not initialized');
    }

    if (Capacitor.isNativePlatform() && !this.keycloak.endpoints) {
      await this.keycloak.init({
        pkceMethod: 'S256',
        checkLoginIframe: false,
        responseMode: 'query',
        redirectUri: this.getRedirectUri(),
      });
    }

    const loginUrl = await this.keycloak.createLoginUrl({
      redirectUri: this.getRedirectUri(),
    });

    if (Capacitor.isNativePlatform()) {
      await Browser.open({ url: loginUrl });
      return;
    }

    await this.keycloak.login({
      redirectUri: this.getRedirectUri(),
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

    if (!this.keycloak) return null;

    // Si keycloak no está autenticado, usar el token guardado
    if (!this.keycloak.authenticated) {
      return this.readStoredSession()?.accessToken ?? null;
    }

    try {
      await this.keycloak.updateToken(30);
      this.persistSession();
      return this.keycloak.token ?? null;
    } catch (error) {
      console.error('Token refresh failed', error);
      return this.readStoredSession()?.accessToken ?? null;
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

    const fullName = [
      claims.name,
      [claims.given_name, claims.family_name].filter(Boolean).join(' ').trim(),
    ]
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

    if (
      roles.some((role) =>
        ['administrador', 'admin'].includes(role.toLowerCase()),
      )
    ) {
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
    return this.getRealmRoles().some(
      (currentRole) => currentRole.trim().toLowerCase() === normalized,
    );
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
    // Si estamos en Android/iOS, window.location.origin es 'https://localhost'
    // En web sería 'http://localhost:4200' o similar
    if (window.location.origin === 'https://localhost' ||
      window.location.origin === 'http://localhost') {
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

  async handleRedirectCallback(url: string): Promise<void> {
    const urlParams = new URL(
      url.replace('co.piedrazul.clinica://', 'https://dummy/'),
    );
    const code = urlParams.searchParams.get('code');

    if (!code) return;

    this.initPromise = null;
    this.keycloak = new Keycloak({
      url: environment.keycloak.url,
      realm: environment.keycloak.realm,
      clientId: environment.keycloak.clientId,
    });

    const fakeUrl = `${window.location.origin}/login${urlParams.search}`;
    window.history.replaceState({}, '', fakeUrl);

    await this.keycloak.init({
      pkceMethod: 'S256',
      checkLoginIframe: false,
      responseMode: 'query',
      redirectUri: this.getRedirectUri(),
    });

    if (this.keycloak.authenticated) {
      this.persistSession();
    }

    // Limpiar la URL
    window.history.replaceState({}, '', '/');
  }
}
