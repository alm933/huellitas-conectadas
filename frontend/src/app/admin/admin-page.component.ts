import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { OrganizationSummary } from '../core/models';
import { OrganizationsApiService } from '../core/organizations-api.service';
import { SessionService } from '../core/session.service';
import { StatusNoticeComponent } from '../shared/status-notice.component';

@Component({
  selector: 'app-admin-page',
  imports: [ReactiveFormsModule, StatusNoticeComponent],
  templateUrl: './admin-page.component.html'
})
export class AdminPageComponent implements OnInit {
  protected readonly session = inject(SessionService);
  private readonly api = inject(OrganizationsApiService);
  protected readonly organizations = signal<OrganizationSummary[]>([]);
  protected readonly loadingOrganizations = signal(false);
  protected readonly organizationError = signal('');
  protected readonly organizationNotice = signal('');
  protected readonly updatingOrganizationId = signal<number | null>(null);
  protected readonly editing = signal<OrganizationSummary | null>(null);
  protected readonly saving = signal(false);
  protected readonly pendingOrganizations = computed(() => this.organizations().filter(org => !org.activo));
  protected readonly activeOrganizations = computed(() => this.organizations().filter(org => org.activo));

  protected readonly editForm = new FormGroup({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(120)] }),
    tipo: new FormControl('REFUGIO', { nonNullable: true, validators: [Validators.required] }),
    correo: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    telefono: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    direccion: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    distrito: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    descripcion: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] })
  });

  ngOnInit(): void { this.refreshOrganizations(); }

  protected refreshOrganizations(): void {
    this.loadingOrganizations.set(true);
    this.organizationError.set('');
    this.api.list().subscribe({
      next: organizations => {
        this.organizations.set(organizations);
        this.loadingOrganizations.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loadingOrganizations.set(false);
        this.organizationError.set(error.status === 401 || error.status === 403
          ? 'Tu sesión no tiene permiso para consultar las organizaciones.'
          : 'No se pudieron cargar las organizaciones. Intenta actualizar la lista.');
      }
    });
  }

  protected toggleOrganization(organization: OrganizationSummary): void {
    this.updatingOrganizationId.set(organization.id);
    this.organizationError.set('');
    this.organizationNotice.set('');
    this.api.toggle(organization.id, !organization.activo).subscribe({
      next: updated => {
        this.replace(updated);
        this.updatingOrganizationId.set(null);
        this.organizationNotice.set(`${updated.nombre} fue ${updated.activo ? 'activada' : 'desactivada'}.`);
      },
      error: () => {
        this.updatingOrganizationId.set(null);
        this.organizationError.set('No se pudo cambiar el estado de la organización.');
      }
    });
  }

  protected startEdit(organization: OrganizationSummary): void {
    this.editing.set(organization);
    this.editForm.reset({
      nombre: organization.nombre, tipo: organization.tipo, correo: organization.correo,
      telefono: organization.telefono, direccion: organization.direccion,
      distrito: organization.distrito, descripcion: organization.descripcion ?? ''
    });
    this.organizationError.set('');
    this.organizationNotice.set('');
  }

  protected saveEdit(): void {
    const organization = this.editing();
    if (!organization || this.saving()) return;
    if (this.editForm.invalid) { this.editForm.markAllAsTouched(); return; }
    this.saving.set(true);
    this.api.update(organization.id, { ...this.editForm.getRawValue(), fotoUrl: organization.fotoUrl }).subscribe({
      next: updated => {
        this.replace(updated);
        this.editing.set(null);
        this.saving.set(false);
        this.organizationNotice.set(`Los datos de ${updated.nombre} se guardaron.`);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.organizationError.set(error.status === 409
          ? 'Ese correo ya está registrado.' : 'No se pudieron guardar los datos. Revisa el formulario.');
      }
    });
  }

  private replace(updated: OrganizationSummary): void {
    this.organizations.update(items => items.map(item => item.id === updated.id ? updated : item));
  }
}
