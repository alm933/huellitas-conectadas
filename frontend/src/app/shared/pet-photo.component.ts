import { Component, computed, input, signal } from '@angular/core';

@Component({
  selector: 'app-pet-photo',
  template: `
    @if (photoUrl() && failedSource() !== photoUrl()) {
      <img [src]="photoUrl()" [alt]="'Foto de ' + name()" [loading]="loading()"
        (error)="failedSource.set(photoUrl())" />
    } @else {
      <span class="photo-placeholder" role="img" [attr.aria-label]="name() + ': sin foto disponible'">
        <span aria-hidden="true">🐾</span>
      </span>
    }
  `,
  styles: `
    :host { display: block; width: 100%; height: 100%; overflow: hidden; }
    img { display: block; width: 100%; height: 100%; object-fit: cover; object-position: var(--pet-photo-position, center 35%); }
    .photo-placeholder { display: grid; width: 100%; height: 100%; place-items: center; background: #e9efe5; color: #6c8a70; font-size: 60px; }
  `
})
export class PetPhotoComponent {
  readonly src = input<string | null>(null);
  readonly name = input.required<string>();
  readonly loading = input<'lazy' | 'eager'>('lazy');
  protected readonly photoUrl = computed(() => this.src()?.trim() || '');
  protected readonly failedSource = signal<string | null>(null);
}
