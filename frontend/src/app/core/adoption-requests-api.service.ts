import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { AdoptionRequest, AdoptionRequestStatus } from './models';
import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })
export class AdoptionRequestsApiService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly base = 'https://spring.itechk.us/api/v1/solicitudes-adopcion';

  create(animalId: number, mensaje: string) {
    return this.http.post<AdoptionRequest>(`${this.base}/animales/${animalId}`, { mensaje },
      { headers: this.session.headers() });
  }

  forOrganization() {
    return this.http.get<AdoptionRequest[]>(`${this.base}/organizacion`,
      { headers: this.session.headers() });
  }

  updateStatus(id: number, estado: AdoptionRequestStatus) {
    return this.http.patch<AdoptionRequest>(`${this.base}/${id}/estado`, { estado },
      { headers: this.session.headers() });
  }
}
