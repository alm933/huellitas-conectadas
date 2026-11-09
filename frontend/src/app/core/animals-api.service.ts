import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Animal, AnimalPhoto, AnimalPublicationResponse, AnimalRequest } from './models';

export type CatalogSpecies = 'TODAS' | 'PERRO' | 'GATO' | 'OTRO';

export interface CatalogPage {
  content: Animal[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

@Injectable({ providedIn: 'root' })
export class AnimalsApiService {
  private readonly http = inject(HttpClient);
  private readonly base = 'https://spring.itechk.us/api/v1/animales';

  list() { return this.http.get<Animal[]>(this.base); }
  catalog(page: number, especie: CatalogSpecies) {
    return this.http.get<CatalogPage>(`${this.base}/catalogo`, {
      params: { page, size: 12, especie }
    });
  }
  get(id: number) { return this.http.get<Animal>(`${this.base}/${id}`); }
  photos(id: number) { return this.http.get<AnimalPhoto[]>(`${this.base}/${id}/fotos`); }
  mine() { return this.http.get<Animal[]>(`${this.base}/mios`); }
  update(id: number, animal: AnimalRequest) {
    return this.http.put<Animal>(`${this.base}/${id}`, animal);
  }
  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
  publish(animal: AnimalRequest, photos: File[]) {
    const body = new FormData();
    body.append('animal', new Blob([JSON.stringify(animal)], { type: 'application/json' }));
    for (const photo of photos) body.append('fotos', photo, photo.name);
    return this.http.post<AnimalPublicationResponse>(`${this.base}/publicar`, body);
  }
}
