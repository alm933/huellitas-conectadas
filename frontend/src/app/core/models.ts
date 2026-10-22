export type UserRole = 'ADMIN' | 'ORGANIZACION' | 'ADOPTANTE';
export type RegistrationType = 'ADOPTANTE' | 'ORGANIZACION';

export interface LoginResponse {
  token: string;
  email: string;
  role: UserRole;
}

export interface RegistrationResponse {
  id: number;
  name: string;
  lastName: string;
  email: string;
  role: UserRole;
}

export interface AdoptanteRegistration {
  name: string;
  lastName: string;
  email: string;
  pass: string;
}

export interface OrganizacionRegistration extends AdoptanteRegistration {
  nombre: string;
  tipo: string;
  correo: string;
  telefono: string;
  direccion: string;
  distrito: string;
  descripcion: string;
}

export interface OrganizationSummary {
  id: number;
  nombre: string;
  tipo: string;
  correo: string;
  telefono: string;
  direccion: string;
  distrito: string;
  descripcion: string | null;
  fotoUrl: string | null;
  activo: boolean;
  fechaRegistro: string | null;
}

export type AnimalStatus = 'DISPONIBLE' | 'EN_PROCESO' | 'ADOPTADO' | 'NO_DISPONIBLE';

export interface Animal {
  id: number;
  nombre: string;
  especie: string;
  raza: string | null;
  edadMeses: number;
  sexo: string;
  tamanio: string;
  descripcion: string | null;
  fotoUrl: string | null;
  estado: AnimalStatus;
  organizacionId: number;
  organizacionNombre: string;
}

export interface AnimalRequest {
  nombre: string;
  especie: string;
  raza: string;
  edadMeses: number;
  sexo: string;
  tamanio: string;
  descripcion: string;
  estado: AnimalStatus;
}

export interface AnimalPublicationResponse {
  animal: Animal;
  fotos: Array<{ id: number; url: string; contentType: string }>;
}
