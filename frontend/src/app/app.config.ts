import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { HttpInterceptorFn, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { SessionService } from './core/session.service';

const apiSession: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith('https://spring.itechk.us/api/v1/')) return next(request);

  const mutation = !['GET', 'HEAD', 'OPTIONS'].includes(request.method);
  return next(request.clone({
    withCredentials: true,
    headers: mutation ? request.headers.set('X-Huellitas-Request', '1') : request.headers
  }));
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([apiSession])),
    provideAppInitializer(() => inject(SessionService).restore())
  ]
};