import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, convertToParamMap } from '@angular/router';
import { MascotasAdoptanteComponent } from './mascotas-adoptante.component';

describe('Solicitud de adopción', async () => {
  const base = 'https://spring.itechk.us/api/v1';
  const pet = { id: 7, nombre: 'Luna', especie: 'Gato', edadMeses: 12, sexo: 'HEMBRA', tamanio: 'MEDIANO', estado: 'DISPONIBLE' };
  const request = { id: 1, animalId: 7, animalNombre: 'Luna', estado: 'PENDIENTE' };
  let http: HttpTestingController;

  async function setup(pending = false, complete = true, open = true) {
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
    await fixture.whenStable();
    fixture.detectChanges();
    // jsdom does not implement the browser's native dialog methods.
    const dialog: HTMLDialogElement = fixture.nativeElement.querySelector('dialog');
    dialog.showModal = () => { dialog.open = true; };
    dialog.close = () => { dialog.open = false; };
    if (!pending && open) {
      fixture.nativeElement.querySelector('.title-line .request-button').click();
      fixture.detectChanges();
    }
    if (!pending && complete) {
      const selects = fixture.nativeElement.querySelectorAll('.adoption-form select') as NodeListOf<HTMLSelectElement>;
      selects[0].selectedIndex = 1;
      selects[0].dispatchEvent(new Event('change'));
      selects[1].selectedIndex = 2;
      selects[1].dispatchEvent(new Event('change'));
      fixture.detectChanges();
    }
    return fixture;
  }

  afterEach(() => http.verify());

  it('abre el formulario solo al solicitar y permite cancelarlo sin enviar', async () => {
    const fixture = await setup(false, false, false);
    const dialog: HTMLDialogElement = fixture.nativeElement.querySelector('dialog');
    expect(dialog.open).toBe(false);
    fixture.nativeElement.querySelector('.title-line .request-button').click();
    expect(dialog.open).toBe(true);
    fixture.nativeElement.querySelector('.dialog-actions button[type="button"]').click();
    expect(dialog.open).toBe(false);
    http.expectNone(`${base}/solicitudes-adopcion/animales/7`);
  });

  it('envía una vez y deshabilita el botón después del éxito', async () => {
    const fixture = await setup();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.send-request-button');
    button.click(); fixture.detectChanges();
    expect(button.disabled).toBe(true);
    const post = http.expectOne(`${base}/solicitudes-adopcion/animales/7`);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual({ mensaje: '', tieneOtrasMascotas: false, tipoVivienda: 'CASA' });
    post.flush(request); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.title-line .request-button').textContent).toContain('Solicitud enviada');
    expect(fixture.nativeElement.querySelector('dialog').open).toBe(false);
    expect(button.disabled).toBe(true);
  });

  it('recupera la solicitud pendiente al recargar', async () => {
    const fixture = await setup(true);
    expect(fixture.nativeElement.querySelector('.request-button').disabled).toBe(true);
  });

  it('exige responder las preguntas antes de enviar', async () => {
    const fixture = await setup(false, false);
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.send-request-button');
    expect(button.disabled).toBe(true);
    button.click();
    http.expectNone(`${base}/solicitudes-adopcion/animales/7`);
  });

  it('muestra el motivo del servidor y permite reintentar', async () => {
    const fixture = await setup();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.send-request-button');
    button.click(); fixture.detectChanges();
    http.expectOne(`${base}/solicitudes-adopcion/animales/7`).flush(
      { detail: 'Esta mascota ya no está disponible.' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('ya no está disponible');
    expect(button.disabled).toBe(false);
  });

  it.each([401, 500])('no atribuye automáticamente el error %i a una sesión expirada', async status => {
    const fixture = await setup();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.send-request-button');
    button.click(); fixture.detectChanges();
    http.expectOne(`${base}/solicitudes-adopcion/animales/7`).flush({}, { status, statusText: 'Error' });
    fixture.detectChanges();
    const message = fixture.nativeElement.querySelector('[role="alert"]').textContent;
    expect(message).not.toContain('Tu sesión expiró');
    expect(button.disabled).toBe(false);
  });
});

describe('Paginación del catálogo del adoptante', () => {
  it('pide la siguiente página y reinicia el índice al cambiar de especie', () => {
    TestBed.configureTestingModule({
      imports: [MascotasAdoptanteComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    const fixture = TestBed.createComponent(MascotasAdoptanteComponent);
    const http = TestBed.inject(HttpTestingController);
    const base = 'https://spring.itechk.us/api/v1';
    const respond = (number: number, especie: string, totalPages: number) => {
      const request = http.expectOne(req => req.url === base + '/animales/catalogo'
        && req.params.get('page') === String(number) && req.params.get('especie') === especie);
      request.flush({ content: [], number, size: 12, totalElements: totalPages * 12, totalPages });
      fixture.detectChanges();
    };

    fixture.detectChanges();
    http.expectOne(base + '/solicitudes-adopcion/mias').flush([]);
    respond(0, 'TODAS', 2);
    const screen: HTMLElement = fixture.nativeElement;
    const pageButtons = () => screen.querySelectorAll<HTMLButtonElement>('.catalog-pages button');
    expect(pageButtons()[0].disabled).toBe(true);
    pageButtons()[1].click();
    respond(1, 'TODAS', 2);
    expect(screen.querySelector('.catalog-pages')?.textContent).toContain('2 de 2');
    expect(pageButtons()[1].disabled).toBe(true);

    const cats = Array.from(screen.querySelectorAll('button')).find(button => button.textContent.trim() === 'Gatos');
    cats!.click();
    respond(0, 'GATO', 0);
    expect(screen.querySelector('.catalog-pages')).toBeNull();
    http.verify();
  });
});
