import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { App } from './app';
import { routes } from './app.routes';

describe('App', () => {
  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideRouter(routes)]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('redirects visitors to the login page', async () => {
    const fixture = TestBed.createComponent(App);
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/');
    await fixture.whenStable();
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(router.url).toBe('/login');
    expect(compiled.querySelector('h1')?.textContent).toContain('Inicia sesión');
  });

  it('blocks the organization panel without an organization session', async () => {
    TestBed.createComponent(App);
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/organizacion');
    expect(router.url).toBe('/login');
  });
});
