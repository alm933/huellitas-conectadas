import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { App } from './app';
import { routes } from './app.routes';

describe('App', () => {
  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter(routes)]
    }).compileComponents();
  });

  it('shows the public welcome page to visitors at the root route', async () => {
    const fixture = TestBed.createComponent(App);
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/');
    const request = TestBed.inject(HttpTestingController)
      .expectOne(req => req.url.endsWith('/api/v1/animales/publicos/catalogo'));
    request.flush({ content: [], number: 0, size: 6, totalElements: 0, totalPages: 0 });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(router.url).toBe('/');
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Un hogar cambia');
    expect(compiled.querySelector('a[href="#adoptar"]')?.textContent).toContain('Explorar mascotas');
  });

  it('blocks the organization panel without an organization session', async () => {
    TestBed.createComponent(App);
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/organizacion');
    expect(router.url).toBe('/login');
  });
});
