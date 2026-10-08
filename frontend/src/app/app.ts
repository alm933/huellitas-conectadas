import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

interface LoginResponse {
  token: string;
  email: string;
  role: 'ADMIN' | 'ORGANIZACION' | 'ADOPTANTE';
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private readonly http = inject(HttpClient);

  protected readonly showPassword = signal(false);
  protected readonly message = signal('');
  protected readonly loading = signal(false);
  protected readonly session = signal<LoginResponse | null>(null);

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

  protected submitLogin(): void {
    this.message.set('');

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { email, password } = this.loginForm.getRawValue();
    this.loading.set(true);

    this.http.post<LoginResponse>(
      'https://spring.itechk.us/api/v1/auth/login',
      { email, pass: password }
    ).subscribe({
      next: response => {
        this.session.set(response);
        this.message.set('');
        this.loading.set(false);
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

  protected logout(): void {
    this.session.set(null);
    this.loginForm.reset();
  }
}