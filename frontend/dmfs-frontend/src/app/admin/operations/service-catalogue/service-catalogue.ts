import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import {
  ServiceCatalogue,
  ServiceCatalogueService
} from '../../../services/service-catalogue.service';

@Component({
  selector: 'app-service-catalogue',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './service-catalogue.html',
  styleUrl: './service-catalogue.scss'
})
export class ServiceCataloguePage implements OnInit {

  private readonly router = inject(Router);

  private readonly serviceCatalogueService =
    inject(ServiceCatalogueService);

  services: ServiceCatalogue[] = [];

  loading = true;

  errorMessage = '';

  showForm = false;

  editingId: number | null = null;

  form = {
    name: '',
    category: '',
    description: '',
    status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
    unitOfMeasurement: '',
    standardPrice: 0,
    minimumArea: 0,
    requiredEquipment: '',
    requiredPersonnel: '',
    estimatedDurationMinutes: 60
  };

  ngOnInit(): void {
    this.loadServices();
  }

  loadServices(): void {

    this.loading = true;
    this.errorMessage = '';

    this.serviceCatalogueService.getServices().subscribe({

      next: services => {

        this.services = services;

        this.loading = false;

      },

      error: () => {

        this.errorMessage =
          'Unable to load the service catalogue.';

        this.loading = false;

      }

    });

  }

  openCreate(): void {

    this.editingId = null;

    this.form = {

      name: '',
      category: '',
      description: '',
      status: 'ACTIVE',
      unitOfMeasurement: '',
      standardPrice: 0,
      minimumArea: 0,
      requiredEquipment: '',
      requiredPersonnel: '',
      estimatedDurationMinutes: 60

    };

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
      requiredPersonnel: service.requiredPersonnel ?? '',
      estimatedDurationMinutes:
        service.estimatedDurationMinutes

    };

    this.showForm = true;

  }

  closeForm(): void {

    this.showForm = false;

    this.editingId = null;

  }

  save(): void {

    const payload = {
      ...this.form
    };

    if (this.editingId === null) {

      this.serviceCatalogueService
        .createService(payload)
        .subscribe({

          next: () => {

            this.closeForm();

            this.loadServices();

          },

          error: () => {

            this.errorMessage =
              'Unable to create the service.';

          }

        });

      return;

    }

    this.serviceCatalogueService
      .updateService(this.editingId, payload)
      .subscribe({

        next: () => {

          this.closeForm();

          this.loadServices();

        },

        error: () => {

          this.errorMessage =
            'Unable to update the service.';

        }

      });

  }

  deactivate(service: ServiceCatalogue): void {

    if (service.status === 'INACTIVE') {
      return;
    }

    const confirmed = window.confirm(
      `Deactivate "${service.name}"?`
    );

    if (!confirmed) {
      return;
    }

    this.serviceCatalogueService
      .deactivateService(service.id)
      .subscribe({

        next: () => this.loadServices(),

        error: () => {

          this.errorMessage =
            'Unable to deactivate the service.';

        }

      });

  }

  goBack(): void {
    this.router.navigate(['/admin/operations']);
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
