import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AnimalsApiService } from '../core/animals-api.service';
import { Animal } from '../core/models';
import { SessionService } from '../core/session.service';
import { StatusNoticeComponent } from '../shared/status-notice.component';
import { PetPhotoComponent } from '../shared/pet-photo.component';

@Component({
  selector: 'app-organizacion-dashboard',
  imports: [RouterLink, StatusNoticeComponent, PetPhotoComponent],
  template: `
    <main class="organization-page">
      <header class="organization-header">
        <div><p class="eyebrow">Huellitas Conectadas</p><h1>Mis mascotas</h1>
          <p class="subtitle">Gestiona las publicaciones de tu organización.</p></div>
        <button class="logout-button" type="button" (click)="session.logout()">Cerrar sesión</button>
      </header>
      <a class="btn btn-success mb-3" routerLink="/organizacion/publicar">+ Publicar nueva mascota</a>
      <app-status-notice [message]="notice()" />
      <app-status-notice [message]="error()" kind="error" />
      @if (loading()) {
        <p class="feedback">Cargando mascotas…</p>
      } @else if (!error() && animals().length === 0) {
        <p class="feedback">Todavía no hay mascotas publicadas. Usa «Publicar nueva mascota» para empezar.</p>
      } @else {
        <div class="pet-management-grid">
          @for (animal of animals(); track animal.id) {
            <article class="pet-management-card card">
              <app-pet-photo [src]="animal.fotoUrl" [name]="animal.nombre" />
              <div class="pet-management-copy">
                <h2>{{ animal.nombre }}</h2>
                <p>{{ animal.especie }} · {{ animal.edadMeses }} meses · {{ animal.estado }}</p>
                <div class="pet-management-actions">
                  <a class="btn btn-outline-success" [routerLink]="['/organizacion/mascotas', animal.id, 'editar']">Editar</a>
                  <button class="btn btn-outline-danger" type="button" [disabled]="deleting() === animal.id" (click)="deleteAnimal(animal)">
                    {{ deleting() === animal.id ? 'Eliminando…' : 'Eliminar' }}
                  </button>
                </div>
              </div>
            </article>
          }
        </div>
      }
      <button class="refresh-list" type="button" (click)="loadAnimals()" [disabled]="loading()">Actualizar lista</button>
    </main>
  `,
  styleUrl: './publicar-mascota.component.css'
})
export class OrganizacionDashboardComponent implements OnInit {
  protected readonly session = inject(SessionService);
  private readonly api = inject(AnimalsApiService);
  protected readonly animals = signal<Animal[]>([]);
  protected readonly loading = signal(false);
  protected readonly deleting = signal<number | null>(null);
  protected readonly notice = signal('');
  protected readonly error = signal('');

  ngOnInit(): void { this.loadAnimals(); }

  protected loadAnimals(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.mine().subscribe({
      next: animals => { this.animals.set(animals); this.loading.set(false); },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(error.status === 403 ? 'Tu cuenta no está vinculada a una organización.'
          : 'No se pudieron cargar las mascotas. Comprueba la conexión e intenta de nuevo.');
      }
    });
  }

  protected deleteAnimal(animal: Animal): void {
    if (!window.confirm(`¿Eliminar definitivamente a ${animal.nombre} y sus fotos?`)) return;
    this.deleting.set(animal.id);
    this.error.set('');
    this.notice.set('');
    this.api.delete(animal.id).subscribe({
      next: () => {
        this.animals.update(items => items.filter(item => item.id !== animal.id));
        this.notice.set(`${animal.nombre} fue eliminada.`);
        this.deleting.set(null);
      },
      error: () => {
        this.error.set('No se pudo eliminar la publicación. Intenta otra vez.');
        this.deleting.set(null);
      }
    });
  }
}
