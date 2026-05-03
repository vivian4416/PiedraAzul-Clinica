import { ApplicationConfig, provideBrowserGlobalErrorListeners, PLATFORM_ID, APP_INITIALIZER } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { AuthInterceptor } from './services/auth.interceptor';
import { AuthService } from './services/auth.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { 
      provide: APP_INITIALIZER, 
      useFactory: (auth: AuthService, platformId: object) => () => {
        if (isPlatformBrowser(platformId)) {
          return auth.init();
        }
        return Promise.resolve();
      },
      deps: [AuthService, PLATFORM_ID], 
      multi: true 
    }
  ]
};
