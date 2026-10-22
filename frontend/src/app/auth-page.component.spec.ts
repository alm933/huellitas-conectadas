import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthPageComponent } from './auth-page.component';

describe('Login', () => {
  let fixture: ComponentFixture<AuthPageComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AuthPageComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([
        { path: 'login', component: AuthPageComponent },
        { path: 'registro', component: AuthPageComponent }
      ])]
    }).compileComponents();
    fixture = TestBed.createComponent(AuthPageComponent);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  function fill(id: string, value: string): void {
    const input = fixture.nativeElement.querySelector(id) as HTMLInputElement;
    input.value = value;
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
  }

  function submitButton(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('button[type="submit"]');
  }

  it('only enables login with a valid email and a nonempty password', () => {
    expect(submitButton().disabled).toBe(true);
    fill('#login-email', 'ana@example.com');
    expect(submitButton().disabled).toBe(true);
    fill('#login-password', 'clave-de-prueba');
    expect(submitButton().disabled).toBe(false);
    fill('#login-email', 'correo-invalido');
    expect(submitButton().disabled).toBe(true);
    fill('#login-email', 'ana@example.com');
    fill('#login-password', '');
    expect(submitButton().disabled).toBe(true);
    http.expectNone('https://spring.itechk.us/api/v1/auth/login');
  });

  it.each([401, 403])('shows a contrasting alert for rejected credentials (HTTP %i)', status => {
    fill('#login-email', 'ana@example.com');
    fill('#login-password', 'clave-de-prueba');
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
    expect(submitButton().disabled).toBe(true);
    expect(submitButton().getAttribute('aria-busy')).toBe('true');

    http.expectOne('https://spring.itechk.us/api/v1/auth/login')
      .flush({}, { status, statusText: 'Unauthorized' });
    fixture.detectChanges();

    const alert = fixture.nativeElement.querySelector('[role="alert"]') as HTMLElement;
    expect(alert.textContent).toContain('Correo o contraseña incorrectos.');
    expect(alert.classList.contains('form-message-error')).toBe(true);
    expect(submitButton().disabled).toBe(false);
    expect(submitButton().getAttribute('aria-busy')).toBe('false');
  });

  it('keeps a successful registration distinct from an earlier login error', async () => {
    fill('#login-email', 'ana@example.com');
    fill('#login-password', 'clave-de-prueba');
    fixture.nativeElement.querySelector('form').dispatchEvent(
      new Event('submit', { bubbles: true, cancelable: true }));
    http.expectOne('https://spring.itechk.us/api/v1/auth/login')
      .flush({}, { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();

    fixture.nativeElement.querySelector('.switch-prompt .text-button').click();
    await fixture.whenStable();
    fixture.detectChanges();
    fill('#adopter-name', 'Ana');
    fill('#adopter-last-name', 'Prueba');
    fill('#adopter-email', 'ana@example.com');
    fill('#adopter-password', 'clave-de-prueba');
    fixture.nativeElement.querySelector('form').dispatchEvent(
      new Event('submit', { bubbles: true, cancelable: true }));
    http.expectOne('https://spring.itechk.us/api/v1/auth/register').flush({
      id: 1, name: 'Ana', lastName: 'Prueba', email: 'ana@example.com', role: 'ADOPTANTE'
    });
    await fixture.whenStable();
    fixture.detectChanges();

    const notice = fixture.nativeElement.querySelector('.form-message') as HTMLElement;
    expect(notice.textContent).toContain('Tu cuenta fue creada.');
    expect(notice.getAttribute('role')).toBe('status');
    expect(notice.classList.contains('form-message-error')).toBe(false);
    expect(submitButton().disabled).toBe(true);
  });
});
