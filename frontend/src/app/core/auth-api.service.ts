import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { AdoptanteRegistration, LoginResponse, OrganizacionRegistration, RegistrationResponse } from './models';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly base = 'https://spring.itechk.us/api/v1/auth';

  login(email: string, pass: string) {
    return this.http.post<LoginResponse>(`${this.base}/login`, { email, pass });
  }

  registerAdopter(data: AdoptanteRegistration) {
    return this.http.post<RegistrationResponse>(`${this.base}/register`, data);
  }

  registerOrganization(data: OrganizacionRegistration) {
    return this.http.post<RegistrationResponse>(`${this.base}/register/organizacion`, data);
  }
}
