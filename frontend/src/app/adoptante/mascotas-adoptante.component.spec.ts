import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, convertToParamMap } from '@angular/router';
import { MascotasAdoptanteComponent } from './mascotas-adoptante.component';

describe('Solicitud de adopción', () => {
  const base = 'https://spring.itechk.us/api/v1';
  const pet = { id: 7, nombre: 'Luna', especie: 'Gato', edadMeses: 12, sexo: 'HEMBRA', tamanio: 'MEDIANO', estado: 'DISPONIBLE' };
  const request = { id: 1, animalId: 7, animalNombre: 'Luna', estado: 'PENDIENTE' };
  let http: HttpTestingController;

  function setup(pending = false) {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [MascotasAdoptanteComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '7' }) } } }]
    });
    const fixture = TestBed.createComponent(MascotasAdoptanteComponent);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(`${base}/solicitudes-adopcion/mias`).flush(pending ? [request] : []);
    http.expectOne(`${base}/animales/7`).flush(pet);
    http.expectOne(`${base}/animales/7/fotos`).flush([]);
    fixture.detectChanges();
    return fixture;
  }

  afterEach(() => http.verify());

  it('envía una vez y deshabilita el botón después del éxito', () => {
    const fixture = setup();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.request-button');
    button.click(); fixture.detectChanges();
    expect(button.disabled).toBe(true);
    const post = http.expectOne(`${base}/solicitudes-adopcion/animales/7`);
    expect(post.request.method).toBe('POST');
    post.flush(request); fixture.detectChanges();
    expect(button.textContent).toContain('Solicitud enviada');
    expect(button.disabled).toBe(true);
  });

  it('recupera la solicitud pendiente al recargar', () => {
    const fixture = setup(true);
    expect(fixture.nativeElement.querySelector('.request-button').disabled).toBe(true);
  });

  it('muestra el motivo del servidor y permite reintentar', () => {
    const fixture = setup();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.request-button');
    button.click(); fixture.detectChanges();
    http.expectOne(`${base}/solicitudes-adopcion/animales/7`).flush(
      { detail: 'Esta mascota ya no está disponible.' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('ya no está disponible');
    expect(button.disabled).toBe(false);
  });
});
