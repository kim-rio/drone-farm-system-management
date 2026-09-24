import { CommonModule, DecimalPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import {
  ServiceCatalogue,
  ServiceCatalogueService
} from '../../services/service-catalogue.service';

@Component({
  selector: 'app-service-catalogue',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DecimalPipe
  ],
  templateUrl: './service-catalogue.html',
  styleUrl: './service-catalogue.scss'
})
export class ServiceCataloguePage implements OnInit {

  readonly serviceCategories = [
    { value: 'MINING', label: 'Mining', description: 'Geological, geophysical and mineral exploration services.' },
    { value: 'AGRICULTURE', label: 'Agriculture', description: 'Farm, crop and agricultural drone services.' }
  ];

  readonly personnelRoles = [
    {
      value: 'GEOLOGIST',
      label: 'Geologist',
      description: 'Required for geological analysis and interpretation.'
    },
    {
      value: 'DRONE_OPERATOR',
      label: 'Drone Operator',
      description: 'Required for drone field operations.'
    }
  ];

  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);

  private readonly serviceCatalogueService =
    inject(ServiceCatalogueService);

  services: ServiceCatalogue[] = [];

  loading = true;
  saving = false;
  errorMessage = '';
  showForm = false;
  editingId: number | null = null;

  form = this.createEmptyForm();

  selectedPersonnelRoles: string[] = [];

  ngOnInit(): void {
    this.loadServices();
  }

  private createEmptyForm() {
    return {
      name: '',
      category: 'MINING',
      description: '',
      status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
      unitOfMeasurement: '',
      standardPrice: 0,
      minimumArea: 1,
      requiredEquipment: '',
      estimatedDurationMinutes: 60
    };
  }

  loadServices(): void {
    this.loading = true;
    this.errorMessage = '';

    console.log('[Services] requesting catalogue...');

    this.serviceCatalogueService.getServices().subscribe({
      next: services => {
        console.log('[Services] catalogue received:', services);
        this.services = services;
        this.loading = false;
        this.changeDetector.detectChanges();
      },
      error: error => {
        console.error('[Services] catalogue failed:', error);
        this.errorMessage = 'Unable to load the service catalogue.';
        this.loading = false;
        this.changeDetector.detectChanges();
      },
      complete: () => {
        console.log('[Services] catalogue request completed');
      }
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.form = this.createEmptyForm();
    this.selectedPersonnelRoles = [];
    this.errorMessage = '';
    this.showForm = true;
  }

  openEdit(service: ServiceCatalogue): void {
    this.editingId = service.id;

    this.form = {
      name: service.name,
      category: service.category,
      description: service.description,
      status: service.status,
      unitOfMeasurement: service.unitOfMeasurement,
      standardPrice: service.standardPrice,
      minimumArea: service.minimumArea,
      requiredEquipment: service.requiredEquipment ?? '',
      estimatedDurationMinutes:
        service.estimatedDurationMinutes
    };

    this.selectedPersonnelRoles =
      (service.requiredPersonnel ?? '')
        .split(',')
        .map(role =>
          role.trim().toUpperCase().replace(/ /g, '_')
        )
        .filter(Boolean);

    this.errorMessage = '';
    this.showForm = true;
  }

  closeForm(): void {
    if (this.saving) {
      return;
    }

    this.showForm = false;
    this.editingId = null;
    this.errorMessage = '';
  }

  togglePersonnelRole(role: string): void {
    if (this.selectedPersonnelRoles.includes(role)) {
      this.selectedPersonnelRoles =
        this.selectedPersonnelRoles.filter(
          value => value !== role
        );
    } else {
      this.selectedPersonnelRoles = [
        ...this.selectedPersonnelRoles,
        role
      ];
    }

    this.errorMessage = '';
  }

  isPersonnelSelected(role: string): boolean {
    return this.selectedPersonnelRoles.includes(role);
  }

  save(): void {
    if (this.saving) {
      return;
    }

    this.errorMessage = '';

    const name = this.form.name.trim();
    const category = this.form.category;
    const description = this.form.description.trim();
    const unit = this.form.unitOfMeasurement.trim();
    const equipment = this.form.requiredEquipment.trim();

    if (!name || !category || !description || !unit) {
      this.errorMessage =
        'Please complete all required fields.';
      return;
    }

    if (this.form.standardPrice < 0) {
      this.errorMessage =
        'Price cannot be negative.';
      return;
    }

    if (this.form.minimumArea <= 0) {
      this.errorMessage =
        'Minimum service area must be greater than zero.';
      return;
    }

    if (this.form.estimatedDurationMinutes <= 0) {
      this.errorMessage =
        'Estimated duration must be greater than zero.';
      return;
    }

    if (!this.selectedPersonnelRoles.length) {
      this.errorMessage =
        'Select at least one required personnel role.';
      return;
    }

    const payload = {
      name,
      category,
      description,
      status: this.form.status,
      unitOfMeasurement: unit,
      standardPrice: Number(this.form.standardPrice),
      minimumArea: Number(this.form.minimumArea),
      requiredEquipment: equipment,
      requiredPersonnel:
        this.selectedPersonnelRoles.join(','),
      estimatedDurationMinutes:
        Number(this.form.estimatedDurationMinutes)
    };

    this.saving = true;

    const request$ =
      this.editingId === null
        ? this.serviceCatalogueService.createService(payload)
        : this.serviceCatalogueService.updateService(
            this.editingId,
            payload
          );

    request$.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.editingId = null;
        this.loadServices();
      },

      error: error => {
        console.error('Unable to save service:', error);

        this.errorMessage =
          error?.error?.message ||
          'Unable to save the service. Please check the information and try again.';

        this.saving = false;
      }
    });
  }

  deactivate(service: ServiceCatalogue): void {
    if (
      service.status === 'INACTIVE' ||
      this.saving
    ) {
      return;
    }

    const confirmed = window.confirm(
      `Deactivate "${service.name}"?`
    );

    if (!confirmed) {
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    this.serviceCatalogueService
      .deactivateService(service.id)
      .subscribe({
        next: () => {
          this.saving = false;
          this.loadServices();
        },

        error: error => {
          console.error(
            'Unable to deactivate service:',
            error
          );

          this.errorMessage =
            error?.error?.message ||
            'Unable to deactivate the service.';

          this.saving = false;
        }
      });
  }

  goBack(): void {
    this.router.navigate(['/admin']);
  }

  get activeCount(): number {
    return this.services.filter(
      service => service.status === 'ACTIVE'
    ).length;
  }

  get inactiveCount(): number {
    return this.services.filter(
      service => service.status === 'INACTIVE'
    ).length;
  }
}