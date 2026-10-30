import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AnimalsApiService } from '../core/animals-api.service';
import { Animal, AnimalPhoto } from '../core/models';
import { SessionService } from '../core/session.service';
import { PetPhotoComponent } from '../shared/pet-photo.component';

@Component({
  selector: 'app-mascotas-adoptante',
  standalone: true,
  imports: [PetPhotoComponent],
  template: `
    @if (selectedAnimal(); as animal) {
      <main class="detail-page">
        <div class="detail-hero">
          <app-pet-photo [src]="currentPhoto()" [name]="animal.nombre" loading="eager" />
          <button class="round-button back-button" type="button" aria-label="Volver al catálogo" (click)="backToCatalog()">‹</button>
          <button class="round-button favorite-button" type="button" [attr.aria-pressed]="isFavorite(animal.id)" [attr.aria-label]="isFavorite(animal.id) ? 'Quitar de favoritos' : 'Añadir a favoritos'" (click)="toggleFavorite(animal.id)">
            {{ isFavorite(animal.id) ? '♥' : '♡' }}
          </button>
          @if (photos().length > 1) {
            <button class="round-button photo-previous" type="button" aria-label="Foto anterior" (click)="showPreviousPhoto()">‹</button>
            <button class="round-button photo-next" type="button" aria-label="Foto siguiente" (click)="showNextPhoto()">›</button>
            <div class="photo-pagination" aria-live="polite">{{ currentPhotoIndex() + 1 }} / {{ photos().length }}</div>
          }
        </div>
        <section class="detail-sheet">
          <div class="title-line">
            <div>
              <h1>{{ animal.nombre }}</h1>
              <p class="organization"><span aria-hidden="true">⌖</span> {{ animal.organizacionNombre || 'Organización de adopción' }}</p>
            </div>
            <span class="ready-badge">Busca un hogar</span>
          </div>
          <div class="stats">
            <article class="stat age"><span aria-hidden="true">♙</span><strong>Edad</strong><small>{{ formatAge(animal.edadMeses) }}</small></article>
            <article class="stat sex"><span aria-hidden="true">{{ animal.sexo === 'HEMBRA' ? '♀' : animal.sexo === 'MACHO' ? '♂' : '•' }}</span><strong>Sexo</strong><small>{{ label(animal.sexo) }}</small></article>
            <article class="stat size"><span aria-hidden="true">▤</span><strong>Tamaño</strong><small>{{ label(animal.tamanio) }}</small></article>
          </div>
          <section class="story">
            <h2><span aria-hidden="true">✧</span> Su historia</h2>
            <p>{{ animal.descripcion || 'Esta mascota está esperando conocer a una familia que le dé un hogar lleno de cariño.' }}</p>
            @if (animal.raza) {
              <p class="breed"><strong>Raza:</strong> {{ animal.raza }} · {{ animal.especie }}</p>
            }
          </section>
          <button class="back-link" type="button" (click)="backToCatalog()">← Volver a las mascotas</button>
        </section>
      </main>
    } @else {
      <main class="catalog-page">
        <header class="catalog-header">
          <div>
            <p class="eyebrow">Huellitas Conectadas</p>
            <h1>Mascotas que buscan hogar</h1>
            <p>Conoce a quienes esperan una familia como la tuya.</p>
          </div>
          <button class="logout-button" type="button" (click)="session.logout()">Cerrar sesión</button>
        </header>
        @if (error()) {
          <div class="notice error" role="alert">
            <span>{{ error() }}</span>
            <button type="button" (click)="loadAnimals()">Reintentar</button>
          </div>
        }
        @if (loading() && animals().length === 0) {
          <p class="catalog-state">Cargando mascotas…</p>
        } @else if (error()) {
          <p class="catalog-state">No se pudieron mostrar las mascotas.</p>
        } @else if (animals().length === 0) {
          <section class="catalog-state empty">
            <span aria-hidden="true">🐾</span>
            <h2>Pronto habrá nuevas historias</h2>
            <p>Cuando una organización publique mascotas disponibles, aparecerán aquí.</p>
            <button type="button" (click)="loadAnimals()">Actualizar</button>
          </section>
        } @else {
          <section class="animal-grid" aria-label="Mascotas disponibles">
            @for (item of animals(); track item.id) {
              <button class="animal-card" type="button" (click)="openAnimal(item)">
                <span class="card-image">
                  <app-pet-photo [src]="item.fotoUrl" [name]="item.nombre" />
                  <span class="card-heart" aria-hidden="true">{{ isFavorite(item.id) ? '♥' : '♡' }}</span>
                </span>
                <span class="card-copy">
                  <span class="card-title">{{ item.nombre }} <span class="card-age">{{ formatAge(item.edadMeses) }}</span></span>
                  <span class="card-organization">⌖ {{ item.organizacionNombre || 'Organización de adopción' }}</span>
                  <span class="card-description">{{ item.descripcion || item.especie + ' en adopción' }}</span>
                  <span class="card-cta">Conocer su historia →</span>
                </span>
              </button>
            }
          </section>
        }
        <footer class="catalog-footer">
          <span>{{ animals().length }} {{ animals().length === 1 ? 'mascota disponible' : 'mascotas disponibles' }}</span>
          <button type="button" (click)="loadAnimals()" [disabled]="loading()">{{ loading() ? 'Actualizando…' : 'Actualizar' }}</button>
        </footer>
      </main>
    }
  `,
  styles: [`
    :host{display:block;min-height:100vh;background:#f7f7f2;color:#26352e;font-family:Inter,"Segoe UI",sans-serif}*{box-sizing:border-box}button{font:inherit;cursor:pointer}
    .catalog-page{width:min(calc(100% - 40px),1100px);margin:auto;padding:42px 0 28px}.catalog-header{display:flex;align-items:center;justify-content:space-between;gap:20px;margin-bottom:28px}
    .eyebrow{margin:0 0 9px;color:#568365;font-size:12px;font-weight:750;letter-spacing:1.5px;text-transform:uppercase}h1{margin:0;color:#24332c;font-size:clamp(28px,4vw,40px);letter-spacing:-1.3px}.catalog-header p:last-child{margin:9px 0 0;color:#718078}
    .logout-button,.catalog-footer button,.notice button,.empty button{padding:10px 14px;border:1px solid #d8e2d8;border-radius:10px;background:white;color:#3f7350;font-weight:700}
    .animal-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(245px,1fr));gap:20px}.animal-card{overflow:hidden;padding:0;border:1px solid #e6e9e1;border-radius:18px;background:#fff;color:inherit;text-align:left;box-shadow:0 7px 22px #283c0d;transition:transform .16s,box-shadow .16s}.animal-card:hover{transform:translateY(-3px);box-shadow:0 12px 28px #283c1c}
    .card-image{position:relative;display:grid;aspect-ratio:4/3;place-items:center;overflow:hidden;background:#e9efe5}.card-heart{position:absolute;top:12px;right:12px;display:grid;width:36px;height:36px;place-items:center;border-radius:50%;background:#fff9f5;color:#a75537;font-size:22px}
    .card-copy{display:grid;gap:8px;padding:16px}.card-title{display:flex;justify-content:space-between;gap:12px;font-size:19px;font-weight:750}.card-age,.card-organization{color:#79847c;font-size:13px;font-weight:450}.card-description{display:-webkit-box;overflow:hidden;color:#647168;font-size:14px;line-height:1.5;-webkit-box-orient:vertical;-webkit-line-clamp:2}.card-cta{margin-top:5px;color:#477b59;font-size:14px;font-weight:750}
    .catalog-state{padding:48px 16px;color:#66746a;text-align:center}.empty{max-width:520px;margin:50px auto;padding:34px;border:1px solid #e6e9e1;border-radius:18px;background:white}.empty>span{font-size:40px}.empty h2{margin:12px 0 8px;color:#26352e}.empty p{line-height:1.6}
    .catalog-footer{display:flex;justify-content:space-between;align-items:center;margin-top:24px;color:#778179;font-size:13px}.catalog-footer button:disabled{opacity:.65;cursor:wait}.notice{display:flex;justify-content:space-between;align-items:center;gap:15px;margin-bottom:20px;padding:13px 16px;border-radius:12px}.error{background:#fff0ec;color:#873f32}
    .detail-page{position:relative;isolation:isolate;min-height:100vh;background:#fff9f6}.detail-page::before,.detail-page::after{position:fixed;z-index:-1;top:0;bottom:0;width:max(0px,calc((100vw - 900px)/2));background-color:#e9efe5;background-image:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='86' height='86' viewBox='0 0 86 86'%3E%3Cg fill='%234d385f' fill-opacity='.22'%3E%3Cellipse cx='25' cy='31' rx='5' ry='7' transform='rotate(-24 25 31)'/%3E%3Cellipse cx='36' cy='25' rx='5' ry='7' transform='rotate(-8 36 25)'/%3E%3Cellipse cx='48' cy='27' rx='5' ry='7' transform='rotate(12 48 27)'/%3E%3Cellipse cx='58' cy='36' rx='5' ry='7' transform='rotate(30 58 36)'/%3E%3Cpath d='M26 43c-8 4-12 12-9 19 2 5 8 5 13 3 5-2 8-2 13 1 6 3 12 2 13-4 2-8-4-17-13-21-6-3-11-2-17 2z'/%3E%3C/g%3E%3C/svg%3E");background-size:86px 86px;content:""}.detail-page::before{left:0}.detail-page::after{right:0}.detail-hero{position:relative;width:min(100%,900px);aspect-ratio:1.85;max-height:min(65vh,620px);margin:0 auto;background:#e7eddf}.detail-hero>app-pet-photo{--pet-photo-position:center 38%}
    .round-button{position:absolute;top:16px;display:grid;width:42px;height:42px;place-items:center;border:0;border-radius:50%;background:#fffbf7eb;color:#33423a;font-size:27px;box-shadow:0 2px 8px #0002}.back-button{left:16px}.favorite-button{right:16px;color:#a95539}
    .photo-previous,.photo-next{top:50%;transform:translateY(-50%)}.photo-previous{left:16px}.photo-next{right:16px}.photo-pagination{position:absolute;right:16px;bottom:16px;padding:6px 10px;border-radius:20px;background:#24332ccc;color:white;font-size:12px;font-weight:700}
    .detail-sheet{position:relative;width:min(100%,900px);min-height:430px;margin:-24px auto 0;padding:24px 26px 32px;border-radius:24px 24px 0 0;background:#fff9f6}.title-line{display:flex;align-items:center;justify-content:space-between;gap:12px}.title-line h1{font-size:27px;letter-spacing:-.6px}.organization{margin:5px 0 0;color:#7d827d;font-size:13px}.ready-badge{flex:none;padding:8px 12px;border-radius:20px;background:#a5ece5;color:#28655d;font-size:12px;font-weight:750}
    .stats{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:10px;margin:22px 0 28px}.stat{display:grid;min-height:92px;place-content:center;justify-items:center;gap:3px;border:1px solid transparent;border-radius:12px;text-align:center}.stat>span{font-size:18px;font-weight:750}.stat strong{font-size:12px}.stat small{color:#56645b;font-size:11px}.age{border-color:#f0d2c7;background:#f9e0d8;color:#985a45}.sex{border-color:#d3eeea;background:#e4f4f2;color:#43847b}.size{border-color:#e1dcda;background:#ece7e5;color:#786e68}
    .story h2{margin:0 0 12px;font-size:17px;letter-spacing:-.2px}.story h2 span{color:#d28d5a}.story>p{margin:0;padding:14px;border-radius:12px;background:white;color:#626b64;font-size:13px;line-height:1.65}.story .breed{margin-top:9px;padding:0;background:transparent}.back-link{margin-top:19px;padding:0;border:0;background:none;color:#477b59;font-weight:750}
    @media(max-width:600px){.detail-page::before,.detail-page::after{display:none}.catalog-page{width:calc(100% - 30px);padding:28px 0}.catalog-header{align-items:flex-start}.catalog-header h1{font-size:29px}.logout-button{padding:8px 10px;font-size:12px}.animal-grid{grid-template-columns:repeat(2,minmax(0,1fr));gap:11px}.card-image{aspect-ratio:4/3}.card-copy{gap:6px;padding:12px}.card-title{font-size:16px}.card-age,.card-organization{font-size:11px}.card-description{font-size:12px}.card-cta{font-size:12px}.detail-hero{width:100%;height:clamp(280px,42vh,480px);max-height:none;aspect-ratio:auto}.detail-sheet{padding:19px 14px 28px}.title-line h1{font-size:23px}.ready-badge{padding:7px 9px;font-size:10px}.stats{gap:6px;margin:19px 0 24px}.stat{min-height:82px}.catalog-footer{margin-top:18px}}
    @media(max-width:380px){.animal-grid{grid-template-columns:1fr}.card-image{aspect-ratio:1.4}}
  `],})
export class MascotasAdoptanteComponent implements OnInit {
  private readonly api = inject(AnimalsApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly session = inject(SessionService);
  protected readonly animals = signal<Animal[]>([]);
  protected readonly selectedAnimal = signal<Animal | null>(null);
  protected readonly photos = signal<AnimalPhoto[]>([]);
  protected readonly currentPhotoIndex = signal(0);
  protected readonly currentPhoto = computed(() => this.photos()[this.currentPhotoIndex()]?.url ?? this.selectedAnimal()?.fotoUrl ?? null);
  protected readonly favorites = signal<number[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal('');

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const animalId = Number(id);
      if (!Number.isSafeInteger(animalId) || animalId <= 0) {
        this.error.set('ID de mascota no válido.');
        return;
      }
      this.loading.set(true);
      this.api.get(animalId).subscribe({
        next: animal => {
          if (animal.estado !== 'DISPONIBLE') {
            this.loading.set(false);
            this.error.set('Esta mascota ya no está disponible.');
            return;
          }
          this.selectedAnimal.set(animal);
          this.api.photos(animalId).subscribe({
            next: photos => {
              const availablePhotos = photos.filter(photo => !!photo.url?.trim());
              this.photos.set(availablePhotos.length > 0
                ? availablePhotos
                : animal.fotoUrl ? [{ id: 0, url: animal.fotoUrl, contentType: '' }] : []);
              this.currentPhotoIndex.set(0);
              this.loading.set(false);
            },
            error: () => {
              this.photos.set(animal.fotoUrl ? [{ id: 0, url: animal.fotoUrl, contentType: '' }] : []);
              this.currentPhotoIndex.set(0);
              this.loading.set(false);
            }
          });
        },
        error: () => { this.loading.set(false); this.error.set('No se pudo cargar la mascota.'); }
      });
    } else {
      this.loadAnimals();
    }
  }

  protected openAnimal(animal: Animal): void {
    void this.router.navigate(['/adoptante/mascotas', animal.id]);
  }

  protected backToCatalog(): void {
    void this.router.navigateByUrl('/adoptante');
  }

  protected showPreviousPhoto(): void {
    const count = this.photos().length;
    if (count > 1) this.currentPhotoIndex.update(index => (index - 1 + count) % count);
  }

  protected showNextPhoto(): void {
    const count = this.photos().length;
    if (count > 1) this.currentPhotoIndex.update(index => (index + 1) % count);
  }

  protected loadAnimals(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.list().subscribe({
      next: animals => {
        this.animals.set(animals.filter(animal => animal.estado === 'DISPONIBLE'));
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(error.status === 401 || error.status === 403
          ? 'Tu sesión no tiene permiso para ver las mascotas. Cierra sesión e inicia de nuevo.'
          : 'No se pudieron cargar las mascotas. Revisa tu conexión e intenta de nuevo.');
      }
    });
  }

  protected isFavorite(id: number): boolean {
    return this.favorites().includes(id);
  }

  protected toggleFavorite(id: number): void {
    this.favorites.update(items => items.includes(id)
      ? items.filter(item => item !== id)
      : [...items, id]);
  }

  protected formatAge(months: number): string {
    if (months < 12) return months === 1 ? '1 mes' : `${months} meses`;
    const years = Math.floor(months / 12);
    const remaining = months % 12;
    return remaining === 0
      ? `${years} ${years === 1 ? 'año' : 'años'}`
      : `${years} ${years === 1 ? 'año' : 'años'} y ${remaining} m`;
  }

  protected label(value: string): string {
    const labels: Record<string, string> = {
      HEMBRA: 'Hembra', MACHO: 'Macho', DESCONOCIDO: 'No indicado',
      PEQUENO: 'Pequeño', MEDIANO: 'Mediano', GRANDE: 'Grande'
    };
    return labels[value] ?? value;
  }
}
