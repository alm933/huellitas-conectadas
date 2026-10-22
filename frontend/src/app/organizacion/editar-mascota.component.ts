import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AnimalsApiService } from '../core/animals-api.service';
import { Animal, AnimalStatus } from '../core/models';
import { StatusNoticeComponent } from '../shared/status-notice.component';

@Component({
  selector: 'app-editar-mascota',
  imports: [ReactiveFormsModule, RouterLink, StatusNoticeComponent],
  template: `
    <main class="organization-page">
      <a routerLink="/organizacion">← Mis mascotas</a>
      <header class="organization-header"><div><p class="eyebrow">Huellitas Conectadas</p>
        <h1>Editar mascota</h1><p class="subtitle">Actualiza los datos y el estado de la publicación.</p></div></header>
      @if (loading()) { <p class="feedback">Cargando publicación…</p> }
      <app-status-notice [message]="error()" kind="error" />
      <app-status-notice [message]="saved() ? 'Los cambios se guardaron.' : ''" />
      @if (animal()) {
        <form [formGroup]="form" (ngSubmit)="save()" novalidate>
          <section class="form-card fields-grid">
            <div class="field"><label for="edit-name">Nombre</label><input id="edit-name" formControlName="nombre" maxlength="80" /></div>
            <div class="field"><label for="edit-species">Especie</label><select id="edit-species" formControlName="especie">
              <option value="Perro">Perro</option><option value="Gato">Gato</option><option value="Otro">Otro</option>
            </select></div>
            <div class="field"><label for="edit-breed">Raza</label><input id="edit-breed" formControlName="raza" maxlength="80" /></div>
            <div class="field"><label for="edit-age">Edad en meses</label><input id="edit-age" type="number" min="0" formControlName="edadMeses" /></div>
            <div class="field"><label for="edit-sex">Sexo</label><select id="edit-sex" formControlName="sexo">
              <option value="HEMBRA">Hembra</option><option value="MACHO">Macho</option><option value="DESCONOCIDO">Desconocido</option>
            </select></div>
            <div class="field"><label for="edit-size">Tamaño</label><select id="edit-size" formControlName="tamanio">
              <option value="PEQUENO">Pequeño</option><option value="MEDIANO">Mediano</option><option value="GRANDE">Grande</option>
            </select></div>
            <div class="field"><label for="edit-status">Estado</label><select id="edit-status" formControlName="estado">
              <option value="DISPONIBLE">Disponible</option><option value="EN_PROCESO">En proceso</option>
              <option value="ADOPTADO">Adoptado</option><option value="NO_DISPONIBLE">No disponible</option>
            </select></div>
            <div class="field"><label for="edit-description">Descripción</label><textarea id="edit-description" formControlName="descripcion" rows="5"></textarea></div>
          </section>
          @if (form.invalid && form.touched) { <p class="feedback error" role="alert">Completa los datos obligatorios y revisa la edad.</p> }
          <button class="publish-button" type="submit" [disabled]="saving()">{{ saving() ? 'Guardando…' : 'Guardar cambios' }}</button>
        </form>
      }
    </main>
  `,
  styleUrl: './publicar-mascota.component.css'
})
export class EditarMascotaComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(AnimalsApiService);
  protected readonly animal = signal<Animal | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal('');
  protected readonly saved = signal(false);
  protected readonly form = new FormGroup({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(80)] }),
    especie: new FormControl('Perro', { nonNullable: true, validators: [Validators.required] }),
    raza: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(80)] }),
    edadMeses: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
    sexo: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    tamanio: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    descripcion: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    estado: new FormControl<AnimalStatus>('DISPONIBLE', { nonNullable: true, validators: [Validators.required] })
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isSafeInteger(id) || id <= 0) { this.error.set('ID de mascota no válido.'); return; }
    this.loading.set(true);
    this.api.mine().subscribe({
      next: animals => {
        this.loading.set(false);
        const animal = animals.find(item => item.id === id);
        if (!animal) { this.error.set('No se encontró esta mascota entre tus publicaciones.'); return; }
        this.animal.set(animal);
        this.form.reset({
          nombre: animal.nombre, especie: animal.especie, raza: animal.raza ?? '',
          edadMeses: animal.edadMeses, sexo: animal.sexo, tamanio: animal.tamanio,
          descripcion: animal.descripcion ?? '', estado: animal.estado
        });
      },
      error: () => { this.loading.set(false); this.error.set('No se pudo cargar la mascota.'); }
    });
  }

  protected save(): void {
    const animal = this.animal();
    if (!animal || this.saving()) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const values = this.form.getRawValue();
    this.error.set('');
    this.saved.set(false);
    this.saving.set(true);
    this.api.update(animal.id, { ...values, edadMeses: values.edadMeses! }).subscribe({
      next: updated => { this.animal.set(updated); this.saving.set(false); this.saved.set(true); },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.error.set(error.status === 403 ? 'Solo puedes editar mascotas de tu organización activa.'
          : 'No se pudieron guardar los cambios. Revisa los datos y vuelve a intentar.');
      }
    });
  }
}
