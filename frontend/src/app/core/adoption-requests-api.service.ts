import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { AdoptionRequest, AdoptionRequestStatus } from './models';

@Injectable({ providedIn: 'root' })
export class AdoptionRequestsApiService {
  private readonly http = inject(HttpClient);
  private readonly base = 'https://spring.itechk.us/api/v1/solicitudes-adopcion';

  create(animalId: number, mensaje: string, tieneOtrasMascotas: boolean, tipoVivienda: string) {
    return this.http.post<AdoptionRequest>(`${this.base}/animales/${animalId}`, { mensaje, tieneOtrasMascotas, tipoVivienda });
  }

  forOrganization() {
    return this.http.get<AdoptionRequest[]>(`${this.base}/organizacion`);
  }

  mine() {
    return this.http.get<AdoptionRequest[]>(`${this.base}/mias`);
  }

  updateStatus(id: number, estado: AdoptionRequestStatus) {
    return this.http.patch<AdoptionRequest>(`${this.base}/${id}/estado`, { estado });
  }
}
