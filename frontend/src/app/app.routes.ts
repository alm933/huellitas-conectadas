import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { SessionService } from './core/session.service';
import { UserRole } from './core/models';
import { AuthPageComponent } from './auth-page.component';
import { AdminPageComponent } from './admin/admin-page.component';
import { MascotasAdoptanteComponent } from './adoptante/mascotas-adoptante.component';
import { OrganizacionDashboardComponent } from './organizacion/organizacion-dashboard.component';
import { PublicarMascotaComponent } from './organizacion/publicar-mascota.component';
import { EditarMascotaComponent } from './organizacion/editar-mascota.component';

const onlyRole = (role: UserRole): CanActivateFn => () => {
  const session = inject(SessionService);
  const router = inject(Router);
  return session.current()?.role === role ? true : session.current()
    ? router.parseUrl(session.homeFor(session.current()!.role)) : router.parseUrl('/login');
};

const onlyGuests: CanActivateFn = () => {
  const session = inject(SessionService);
  return session.current() ? inject(Router).parseUrl(session.homeFor(session.current()!.role)) : true;
};

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: () => {
    const session = inject(SessionService);
    return session.current() ? session.homeFor(session.current()!.role) : '/login';
  } },
  { path: 'login', component: AuthPageComponent, canActivate: [onlyGuests] },
  { path: 'registro', component: AuthPageComponent, canActivate: [onlyGuests] },
  { path: 'admin', component: AdminPageComponent, canActivate: [onlyRole('ADMIN')] },
  { path: 'organizacion', component: OrganizacionDashboardComponent, canActivate: [onlyRole('ORGANIZACION')] },
  { path: 'organizacion/publicar', component: PublicarMascotaComponent, canActivate: [onlyRole('ORGANIZACION')] },
  { path: 'organizacion/mascotas/:id/editar', component: EditarMascotaComponent, canActivate: [onlyRole('ORGANIZACION')] },
  { path: 'adoptante', component: MascotasAdoptanteComponent, canActivate: [onlyRole('ADOPTANTE')] },
  { path: 'adoptante/mascotas/:id', component: MascotasAdoptanteComponent, canActivate: [onlyRole('ADOPTANTE')] },
  { path: '**', redirectTo: '' }
];
