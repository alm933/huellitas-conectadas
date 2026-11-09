import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { OrganizationSummary } from './models';

export type OrganizationUpdate = Pick<OrganizationSummary,
  'nombre' | 'tipo' | 'correo' | 'telefono' | 'direccion' | 'distrito' | 'descripcion' | 'fotoUrl'>;

@Injectable({ providedIn: 'root' })
export class OrganizationsApiService {
  private readonly http = inject(HttpClient);
  private readonly base = 'https://spring.itechk.us/api/v1/organizaciones';

  list() {
    return this.http.get<OrganizationSummary[]>(this.base);
  }
  update(id: number, body: OrganizationUpdate) {
    return this.http.put<OrganizationSummary>(`${this.base}/${id}`, body);
  }
  toggle(id: number, activo: boolean) {
    return this.http.patch<OrganizationSummary>(`${this.base}/${id}/activo`, { activo });
  }
}
