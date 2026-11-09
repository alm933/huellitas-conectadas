import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, firstValueFrom, of } from 'rxjs';
import { AuthApiService } from './auth-api.service';
import { LoginResponse, UserRole } from './models';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly router = inject(Router);
  private readonly authApi = inject(AuthApiService);
  readonly current = signal<LoginResponse | null>(null);
  readonly logoutError = signal('');
  private loggingOut = false;

  async restore(): Promise<void> {
    try {
      window.sessionStorage.removeItem('huellitas.session');
    } catch {
      // Storage may be disabled in the browser.
    }
    const session = await firstValueFrom(this.authApi.session().pipe(catchError(() => of(null))));
    this.current.set(session);
  }

  login(session: LoginResponse): void {
    this.current.set(session);
    this.logoutError.set('');
    void this.router.navigateByUrl(this.homeFor(session.role));
  }

  logout(): void {
    if (this.loggingOut) return;
    this.loggingOut = true;
    this.logoutError.set('');
    this.authApi.logout().subscribe({
      next: () => {
        this.loggingOut = false;
        this.current.set(null);
        void this.router.navigateByUrl('/login');
      },
      error: () => {
        this.loggingOut = false;
        this.logoutError.set('No se pudo cerrar la sesión. Revisa tu conexión e inténtalo de nuevo.');
      }
    });
  }

  homeFor(role: UserRole): string {
    return role === 'ADMIN' ? '/admin' : role === 'ORGANIZACION' ? '/organizacion' : '/adoptante';
  }
}
