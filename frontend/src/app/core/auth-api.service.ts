import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { AdoptanteRegistration, LoginResponse, OrganizacionRegistration, RegistrationResponse } from './models';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly base = 'https://spring.itechk.us/api/v1/auth';

  login(email: string, pass: string) {
    return this.http.post<LoginResponse>(`${this.base}/login/browser`, { email, pass });
  }

  session() {
    return this.http.get<LoginResponse>(`${this.base}/session`, { timeout: 5000 });
  }

  logout() {
    return this.http.post<void>(`${this.base}/logout`, null, { timeout: 5000 });
  }

  registerAdopter(data: AdoptanteRegistration) {
    return this.http.post<RegistrationResponse>(`${this.base}/register`, data);
  }

  registerOrganization(data: OrganizacionRegistration) {
    return this.http.post<RegistrationResponse>(`${this.base}/register/organizacion`, data);
  }
}
