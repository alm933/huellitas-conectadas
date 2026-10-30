import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Animal, AnimalPhoto, AnimalPublicationResponse, AnimalRequest } from './models';
import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })
export class AnimalsApiService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly base = 'https://spring.itechk.us/api/v1/animales';

  list() { return this.http.get<Animal[]>(this.base, { headers: this.session.headers() }); }
  get(id: number) { return this.http.get<Animal>(`${this.base}/${id}`, { headers: this.session.headers() }); }
  photos(id: number) { return this.http.get<AnimalPhoto[]>(`${this.base}/${id}/fotos`, { headers: this.session.headers() }); }
  mine() { return this.http.get<Animal[]>(`${this.base}/mios`, { headers: this.session.headers() }); }
  update(id: number, animal: AnimalRequest) {
    return this.http.put<Animal>(`${this.base}/${id}`, animal, { headers: this.session.headers() });
  }
  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`, { headers: this.session.headers() });
  }
  publish(animal: AnimalRequest, photos: File[]) {
    const body = new FormData();
    body.append('animal', new Blob([JSON.stringify(animal)], { type: 'application/json' }));
    for (const photo of photos) body.append('fotos', photo, photo.name);
    return this.http.post<AnimalPublicationResponse>(`${this.base}/publicar`, body,
      { headers: this.session.headers() });
  }
}
