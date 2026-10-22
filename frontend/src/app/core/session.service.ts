import { Injectable, inject, signal } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { LoginResponse, UserRole } from './models';

const KEY = 'huellitas.session';

function readSession(): LoginResponse | null {
  if (typeof window === 'undefined') return null;
  try {
    const stored = window.sessionStorage.getItem(KEY);
    if (!stored) return null;
    const session: Partial<LoginResponse> = JSON.parse(stored);
    if (typeof session.token === 'string' && session.token.length > 0
      && typeof session.email === 'string'
      && ['ADMIN', 'ORGANIZACION', 'ADOPTANTE'].includes(session.role ?? '')) {
      return session as LoginResponse;
    }
    window.sessionStorage.removeItem(KEY);
  } catch {
    // Session storage can be disabled by the browser.
  }
  return null;
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly router = inject(Router);
  readonly current = signal<LoginResponse | null>(readSession());

  login(session: LoginResponse): void {
    this.current.set(session);
    try { window.sessionStorage.setItem(KEY, JSON.stringify(session)); } catch { /* in-memory session */ }
    void this.router.navigateByUrl(this.homeFor(session.role));
  }

  logout(): void {
    this.current.set(null);
    try { window.sessionStorage.removeItem(KEY); } catch { /* in-memory logout */ }
    void this.router.navigateByUrl('/login');
  }

  homeFor(role: UserRole): string {
    return role === 'ADMIN' ? '/admin' : role === 'ORGANIZACION' ? '/organizacion' : '/adoptante';
  }

  headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.current()?.token ?? ''}` });
  }
}
