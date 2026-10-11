import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Component, EventEmitter, Input, OnDestroy, Output, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

interface PhotoPreview {
  file: File;
  url: string;
}

interface AnimalPublicationResponse {
  animal: {
    id: number;
    nombre: string;
    fotoUrl: string | null;
  };
  fotos: Array<{ id: number; url: string; contentType: string }>;
}

const API_URL = 'https://spring.itechk.us/api/v1';
const MAX_PHOTO_BYTES = 5 * 1024 * 1024;
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

@Component({
  selector: 'app-publicar-mascota',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './publicar-mascota.component.html',
  styleUrl: './publicar-mascota.component.css'
})
export class PublicarMascotaComponent implements OnDestroy {
  private readonly http = inject(HttpClient);

  @Input({ required: true }) token = '';
  @Output() readonly logoutRequested = new EventEmitter<void>();

  protected readonly form = new FormGroup({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(80)] }),
    especie: new FormControl('Perro', { nonNullable: true, validators: [Validators.required, Validators.maxLength(50)] }),
    raza: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(80)] }),
    edadMeses: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
    sexo: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    tamanio: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    descripcion: new FormControl('', { nonNullable: true, validators: [Validators.required] })
  });

  protected readonly photos = signal<PhotoPreview[]>([]);
  protected readonly errorMessage = signal('');
  protected readonly success = signal<AnimalPublicationResponse | null>(null);
  protected readonly uploading = signal(false);

  protected onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    this.errorMessage.set('');
    this.success.set(null);
    this.releasePhotoUrls();

    if (files.length > 8) {
      this.errorMessage.set('Puedes adjuntar como máximo 8 fotos. Se conservarán las primeras 8 válidas.');
    }

    const accepted: File[] = [];
    let rejected = false;

    for (const file of files.slice(0, 8)) {
      if (!ALLOWED_TYPES.includes(file.type) || file.size === 0 || file.size > MAX_PHOTO_BYTES) {
        rejected = true;
        continue;
      }
      accepted.push(file);
    }

    if (rejected) {
      this.errorMessage.set('Se omitieron archivos no válidos. Usa imágenes JPEG, PNG o WebP de hasta 5 MB cada una.');
    }

    this.photos.set(accepted.map(file => ({ file, url: URL.createObjectURL(file) })));
    input.value = '';
  }

  protected removePhoto(index: number): void {
    const current = this.photos();
    const removed = current[index];
    if (removed) URL.revokeObjectURL(removed.url);
    this.photos.set(current.filter((_, itemIndex) => itemIndex !== index));
    this.success.set(null);
  }

  protected publish(): void {
    this.errorMessage.set('');
    this.success.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.photos().length < 2 || this.photos().length > 8) {
      this.errorMessage.set('Selecciona entre 2 y 8 fotos antes de publicar.');
      return;
    }

    const values = this.form.getRawValue();
    const animal = {
      nombre: values.nombre.trim(),
      especie: values.especie,
      raza: values.raza.trim(),
      edadMeses: values.edadMeses,
      sexo: values.sexo,
      tamanio: values.tamanio,
      descripcion: values.descripcion.trim(),
      estado: 'DISPONIBLE'
    };

    const body = new FormData();
    body.append('animal', new Blob([JSON.stringify(animal)], { type: 'application/json' }));
    for (const photo of this.photos()) {
      body.append('fotos', photo.file, photo.file.name);
    }

    this.uploading.set(true);
    this.http.post<AnimalPublicationResponse>(API_URL + '/animales/publicar', body, {
      headers: new HttpHeaders({ Authorization: 'Bearer ' + this.token })
    }).subscribe({
      next: response => {
        this.uploading.set(false);
        this.success.set(response);
        this.form.reset({
          nombre: '',
          especie: 'Perro',
          raza: '',
          edadMeses: null,
          sexo: '',
          tamanio: '',
          descripcion: ''
        });
        this.releasePhotoUrls();
        this.photos.set([]);
      },
      error: (error: HttpErrorResponse) => {
        this.uploading.set(false);
        if (error.status === 0) {
          this.errorMessage.set('No se pudo conectar con Spring. Revisa la conexión y CORS.');
        } else if (error.status === 401) {
          this.errorMessage.set('La sesión venció. Cierra sesión e inicia nuevamente.');
        } else if (error.status === 403) {
          this.errorMessage.set('La organización aún no está activa o la cuenta no está vinculada a una organización.');
        } else if (error.status === 400) {
          this.errorMessage.set(error.error?.detail || error.error?.message || 'Revisa los datos y que hayas seleccionado al menos dos fotos válidas.');
        } else {
          this.errorMessage.set('No se pudo publicar la mascota (error ' + error.status + ').');
        }
      }
    });
  }

  protected requestLogout(): void {
    this.logoutRequested.emit();
  }

  ngOnDestroy(): void {
    this.releasePhotoUrls();
  }

  private releasePhotoUrls(): void {
    for (const photo of this.photos()) URL.revokeObjectURL(photo.url);
  }
}
