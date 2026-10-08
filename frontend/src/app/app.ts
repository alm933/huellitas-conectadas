import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {
  FormField,
  email,
  form,
  maxLength,
  minLength,
  required
} from '@angular/forms/signals';

type UserRole = 'ADMIN' | 'ORGANIZACION' | 'ADOPTANTE';
type RegistrationType = 'ADOPTANTE' | 'ORGANIZACION';

interface LoginResponse {
  token: string;
  email: string;
  role: UserRole;
}

interface RegistrationResponse {
  id: number;
  name: string;
  lastName: string;
  email: string;
  role: UserRole;
}

interface OrganizationSummary {
  id: number;
  nombre: string;
  tipo: string;
  correo: string;
  telefono: string;
  direccion: string;
  distrito: string;
  descripcion: string | null;
  activo: boolean;
  fechaRegistro: string | null;
}

interface AdoptanteRegistration {
  name: string;
  lastName: string;
  email: string;
  pass: string;
}

interface OrganizacionRegistration {
  name: string;
  lastName: string;
  email: string;
  pass: string;
  nombre: string;
  tipo: string;
  correo: string;
  telefono: string;
  direccion: string;
  distrito: string;
  descripcion: string;
}

const API_URL = 'https://spring.itechk.us/api/v1';

@Component({
  selector: 'app-root',
  imports: [ReactiveFormsModule, FormField],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private readonly http = inject(HttpClient);

  protected readonly showPassword = signal(false);
  protected readonly message = signal('');
  protected readonly loading = signal(false);
  protected readonly registering = signal(false);
  protected readonly registrationAttempted = signal(false);
  protected readonly view = signal<'login' | 'register'>('login');
  protected readonly registrationType = signal<RegistrationType>('ADOPTANTE');
  protected readonly session = signal<LoginResponse | null>(null);
  protected readonly organizations = signal<OrganizationSummary[]>([]);
  protected readonly loadingOrganizations = signal(false);
  protected readonly organizationError = signal('');
  protected readonly organizationNotice = signal('');
  protected readonly updatingOrganizationId = signal<number | null>(null);
  protected readonly pendingOrganizations = computed(() =>
    this.organizations().filter(organization => !organization.activo)
  );
  protected readonly activeOrganizations = computed(() =>
    this.organizations().filter(organization => organization.activo)
  );

  protected readonly loginForm = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email]
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    })
  });

  protected readonly adoptanteModel = signal<AdoptanteRegistration>({
    name: '',
    lastName: '',
    email: '',
    pass: ''
  });

  protected readonly adoptanteForm = form(this.adoptanteModel, (fields) => {
    required(fields.name, { message: 'Ingresa tu nombre.' });
    maxLength(fields.name, 50, { message: 'El nombre admite hasta 50 caracteres.' });
    required(fields.lastName, { message: 'Ingresa tus apellidos.' });
    maxLength(fields.lastName, 100, { message: 'Los apellidos admiten hasta 100 caracteres.' });
    required(fields.email, { message: 'Ingresa tu correo electrónico.' });
    email(fields.email, { message: 'Escribe un correo válido.' });
    maxLength(fields.email, 150, { message: 'El correo admite hasta 150 caracteres.' });
    required(fields.pass, { message: 'Crea una contraseña.' });
    minLength(fields.pass, 8, { message: 'La contraseña debe tener al menos 8 caracteres.' });
    maxLength(fields.pass, 100, { message: 'La contraseña admite hasta 100 caracteres.' });
  });

  protected readonly organizacionModel = signal<OrganizacionRegistration>({
    name: '',
    lastName: '',
    email: '',
    pass: '',
    nombre: '',
    tipo: 'REFUGIO',
    correo: '',
    telefono: '',
    direccion: '',
    distrito: '',
    descripcion: ''
  });

  protected readonly organizacionForm = form(this.organizacionModel, (fields) => {
    required(fields.name, { message: 'Ingresa el nombre de la persona responsable.' });
    maxLength(fields.name, 50, { message: 'El nombre admite hasta 50 caracteres.' });
    required(fields.lastName, { message: 'Ingresa los apellidos de la persona responsable.' });
    maxLength(fields.lastName, 100, { message: 'Los apellidos admiten hasta 100 caracteres.' });
    required(fields.email, { message: 'Ingresa el correo para iniciar sesión.' });
    email(fields.email, { message: 'Escribe un correo válido.' });
    maxLength(fields.email, 150, { message: 'El correo admite hasta 150 caracteres.' });
    required(fields.pass, { message: 'Crea una contraseña.' });
    minLength(fields.pass, 8, { message: 'La contraseña debe tener al menos 8 caracteres.' });
    maxLength(fields.pass, 100, { message: 'La contraseña admite hasta 100 caracteres.' });
    required(fields.nombre, { message: 'Ingresa el nombre de la organización.' });
    maxLength(fields.nombre, 120, { message: 'El nombre admite hasta 120 caracteres.' });
    required(fields.tipo, { message: 'Selecciona el tipo de organización.' });
    required(fields.correo, { message: 'Ingresa el correo de contacto.' });
    email(fields.correo, { message: 'Escribe un correo de contacto válido.' });
    maxLength(fields.correo, 150, { message: 'El correo admite hasta 150 caracteres.' });
    required(fields.telefono, { message: 'Ingresa un teléfono de contacto.' });
    required(fields.direccion, { message: 'Ingresa la dirección.' });
    required(fields.distrito, { message: 'Ingresa el distrito.' });
    maxLength(fields.descripcion, 500, { message: 'La descripción admite hasta 500 caracteres.' });
  });

  protected submitLogin(): void {
    this.message.set('');

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { email, password } = this.loginForm.getRawValue();
    this.loading.set(true);

    this.http.post<LoginResponse>(`${API_URL}/auth/login`, { email, pass: password }).subscribe({
      next: response => {
        this.session.set(response);
        this.message.set('');
        this.loading.set(false);
        if (response.role === 'ADMIN') {
          this.loadOrganizations(response.token);
        }
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);

        if (error.status === 0) {
          this.message.set(
            'No se pudo conectar con Spring. Revisa que el servicio esté iniciado y que CORS permita localhost:4200.'
          );
        } else if (error.status === 401 || error.status === 403) {
          this.message.set('Correo o contraseña incorrectos.');
        } else {
          this.message.set(`Error al iniciar sesión (${error.status}).`);
        }
      }
    });
  }

  protected openRegistration(type: RegistrationType): void {
    this.registrationType.set(type);
    this.registrationAttempted.set(false);
    this.message.set('');
    this.view.set('register');
  }

  protected showLogin(): void {
    this.message.set('');
    this.registrationAttempted.set(false);
    this.view.set('login');
  }

  protected submitAdoptante(event: SubmitEvent): void {
    event.preventDefault();
    this.message.set('');
    this.registrationAttempted.set(true);

    if (this.adoptanteForm().invalid()) {
      return;
    }

    this.registering.set(true);
    this.http.post<RegistrationResponse>(`${API_URL}/auth/register`, this.adoptanteModel()).subscribe({
      next: response => {
        this.registrationSucceeded(response.email, false);
      },
      error: error => this.handleRegistrationError(error)
    });
  }

  protected submitOrganizacion(event: SubmitEvent): void {
    event.preventDefault();
    this.message.set('');
    this.registrationAttempted.set(true);

    if (this.organizacionForm().invalid()) {
      return;
    }

    this.registering.set(true);
    this.http.post<RegistrationResponse>(
      `${API_URL}/auth/register/organizacion`,
      this.organizacionModel()
    ).subscribe({
      next: response => {
        this.registrationSucceeded(response.email, true);
      },
      error: error => this.handleRegistrationError(error)
    });
  }

  private registrationSucceeded(email: string, isOrganizacion: boolean): void {
    this.registering.set(false);
    this.registrationAttempted.set(false);
    this.adoptanteModel.set({ name: '', lastName: '', email: '', pass: '' });
    this.organizacionModel.set({
      name: '',
      lastName: '',
      email: '',
      pass: '',
      nombre: '',
      tipo: 'REFUGIO',
      correo: '',
      telefono: '',
      direccion: '',
      distrito: '',
      descripcion: ''
    });
    this.loginForm.controls.email.setValue(email);
    this.loginForm.controls.password.setValue('');
    this.view.set('login');
    this.message.set(
      isOrganizacion
        ? 'Registro recibido. Tu cuenta fue creada y el perfil de la organización queda pendiente de activación.'
        : 'Tu cuenta fue creada. Ya puedes iniciar sesión.'
    );
  }

  private handleRegistrationError(error: HttpErrorResponse): void {
    this.registering.set(false);

    if (error.status === 0) {
      this.message.set(
        'No se pudo conectar con Spring. Revisa que el servicio esté iniciado y que CORS permita localhost:4200.'
      );
    } else if (error.status === 409) {
      this.message.set(
        'El correo de acceso o el correo de contacto ya está registrado. Revisa los datos e intenta de nuevo.'
      );
    } else if (error.status === 400) {
      this.message.set('Algunos datos no son válidos. Revísalos e intenta de nuevo.');
    } else {
      this.message.set(`No se pudo crear la cuenta (${error.status}).`);
    }
  }

  protected refreshOrganizations(): void {
    const currentSession = this.session();
    if (currentSession?.role === 'ADMIN') {
      this.loadOrganizations(currentSession.token);
    }
  }

  private loadOrganizations(token: string): void {
    this.loadingOrganizations.set(true);
    this.organizationError.set('');
    this.organizationNotice.set('');

    this.http.get<OrganizationSummary[]>(`${API_URL}/organizaciones`, {
      headers: new HttpHeaders({ Authorization: `Bearer ${token}` })
    }).subscribe({
      next: organizations => {
        this.organizations.set(organizations);
        this.loadingOrganizations.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loadingOrganizations.set(false);
        this.organizationError.set(
          error.status === 401 || error.status === 403
            ? 'Tu sesión no tiene permiso para consultar las organizaciones.'
            : 'No se pudieron cargar las organizaciones. Intenta actualizar la lista.'
        );
      }
    });
  }

  protected toggleOrganization(organization: OrganizationSummary): void {
    const currentSession = this.session();
    if (!currentSession || currentSession.role !== 'ADMIN') {
      return;
    }

    this.updatingOrganizationId.set(organization.id);
    this.organizationError.set('');
    this.organizationNotice.set('');

    this.http.patch<OrganizationSummary>(
      `${API_URL}/organizaciones/${organization.id}/activo`,
      { activo: !organization.activo },
      { headers: new HttpHeaders({ Authorization: `Bearer ${currentSession.token}` }) }
    ).subscribe({
      next: updatedOrganization => {
        this.organizations.update(items =>
          items.map(item => item.id === updatedOrganization.id ? updatedOrganization : item)
        );
        this.updatingOrganizationId.set(null);
        this.organizationNotice.set(
          updatedOrganization.activo
            ? `${updatedOrganization.nombre} fue activada.`
            : `${updatedOrganization.nombre} fue desactivada.`
        );
      },
      error: (error: HttpErrorResponse) => {
        this.updatingOrganizationId.set(null);
        this.organizationError.set(
          error.status === 401 || error.status === 403
            ? 'Spring rechazó la acción. Verifica que la cuenta tenga rol ADMIN y que el token siga vigente.'
            : 'No se pudo cambiar el estado de la organización. Intenta de nuevo.'
        );
      }
    });
  }

  protected logout(): void {
    this.session.set(null);
    this.organizations.set([]);
    this.organizationError.set('');
    this.organizationNotice.set('');
    this.loginForm.reset();
    this.message.set('');
  }
}
