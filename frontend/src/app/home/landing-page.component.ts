import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AnimalsApiService, CatalogSpecies } from '../core/animals-api.service';
import { Animal } from '../core/models';
import { SessionService } from '../core/session.service';
import { PetPhotoComponent } from '../shared/pet-photo.component';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [RouterLink, PetPhotoComponent],
  templateUrl: './landing-page.component.html',
  styleUrl: './landing-page.component.css'
})
export class LandingPageComponent implements OnInit {
  private readonly animalsApi = inject(AnimalsApiService);
  protected readonly session = inject(SessionService);
  protected readonly animals = signal<Animal[]>([]);
  protected readonly selectedSpecies = signal<CatalogSpecies>('TODAS');
  protected readonly query = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly speciesFilters = [
    { value: 'TODAS', label: 'Todas' },
    { value: 'PERRO', label: '🐶 Perros' },
    { value: 'GATO', label: '🐱 Gatos' },
    { value: 'OTRO', label: 'Otros' }
  ] as const;
  protected readonly accountLink = computed(() => {
    const current = this.session.current();
    return current ? this.session.homeFor(current.role) : '/login';
  });
  protected readonly accountLabel = computed(() => this.session.current() ? 'Mi panel' : 'Iniciar sesión');
  protected readonly visibleAnimals = computed(() => {
    const query = this.query().trim().toLocaleLowerCase();
    if (!query) return this.animals();
    return this.animals().filter(animal =>
      [animal.nombre, animal.raza ?? '', animal.especie, animal.organizacionNombre]
        .some(value => value.toLocaleLowerCase().includes(query))
    );
  });

  ngOnInit(): void {
    this.loadAnimals();
  }

  protected selectSpecies(species: CatalogSpecies): void {
    if (this.loading() || species === this.selectedSpecies()) return;
    this.selectedSpecies.set(species);
    this.loadAnimals();
  }

  protected loadAnimals(): void {
    this.loading.set(true);
    this.error.set('');
    this.animalsApi.publicCatalog(this.selectedSpecies()).subscribe({
      next: result => {
        this.animals.set(result.content);
        this.loading.set(false);
      },
      error: () => {
        this.animals.set([]);
        this.loading.set(false);
        this.error.set('No se pudieron cargar las mascotas. Revisa tu conexión e inténtalo de nuevo.');
      }
    });
  }

  protected scrollToPets(): void {
    document.getElementById('adoptar')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
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
    if (value === 'HEMBRA') return 'Hembra';
    if (value === 'MACHO') return 'Macho';
    return 'Sexo no indicado';
  }
}
