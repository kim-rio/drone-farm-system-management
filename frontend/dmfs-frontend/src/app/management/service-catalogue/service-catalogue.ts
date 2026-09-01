import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  ServiceCatalogue,
  CreateServiceCatalogueRequest,
  ServiceCatalogueService
} from '../../services/service-catalogue.service';

@Component({
  selector: 'app-service-catalogue',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './service-catalogue.html',
  styleUrl: './service-catalogue.scss'
})
export class ServiceCatalogueComponent implements OnInit {

  private readonly serviceCatalogueService =
    inject(ServiceCatalogueService);

  private readonly cdr =
    inject(ChangeDetectorRef);


  /* ==============================
     DATA
     ============================== */

  services: ServiceCatalogue[] = [];

  loading = true;

  errorMessage = '';

  successMessage = '';


  /* ==============================
     SEARCH / FILTER
     ============================== */

  searchTerm = '';

  selectedCategory = 'ALL';

  selectedStatus = 'ALL';


  /* ==============================
     MODAL
     ============================== */

  showForm = false;

  editingService: ServiceCatalogue | null = null;

  saving = false;


  /* ==============================
     FORM
     ============================== */

  form: CreateServiceCatalogueRequest = {
    name: '',
    category: '',
    description: '',
    status: 'ACTIVE',
    unitOfMeasurement: '',
    standardPrice: 0,
    minimumArea: 0,
    requiredEquipment: '',
    requiredPersonnel: '',
    estimatedDurationMinutes: 0
  };


  /* ==============================
     INITIALIZATION
     ============================== */

  ngOnInit(): void {
    this.loadServices();
  }


  /* ==============================
     LOAD SERVICES
     ============================== */

  loadServices(): void {

    this.loading = true;
    this.errorMessage = '';

    this.serviceCatalogueService
      .getServices()
      .subscribe({

        next: (services) => {

          console.log(
            'SERVICE CATALOGUE RECEIVED:',
            services
          );

          this.services = services;

          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'SERVICE CATALOGUE LOAD ERROR:',
            error
          );

          this.loading = false;

          if (error?.status === 401) {

            this.errorMessage =
              'Your session has expired. Please log in again.';

          } else if (error?.status === 403) {

            this.errorMessage =
              'You do not have permission to view the service catalogue.';

          } else {

            this.errorMessage =
              'Unable to load the service catalogue.';
          }

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     FILTERED SERVICES
     ============================== */

  get filteredServices(): ServiceCatalogue[] {

    const search =
      this.searchTerm.trim().toLowerCase();

    return this.services.filter(service => {

      const matchesSearch =
        !search ||
        service.name.toLowerCase().includes(search) ||
        service.category.toLowerCase().includes(search) ||
        service.description.toLowerCase().includes(search);

      const matchesCategory =
        this.selectedCategory === 'ALL' ||
        service.category === this.selectedCategory;

      const matchesStatus =
        this.selectedStatus === 'ALL' ||
        service.status === this.selectedStatus;

      return matchesSearch &&
        matchesCategory &&
        matchesStatus;
    });
  }


  /* ==============================
     CATEGORIES
     ============================== */

  get categories(): string[] {

    return [
      ...new Set(
        this.services
          .map(service => service.category)
          .filter(Boolean)
      )
    ];
  }


  /* ==============================
     OPEN CREATE FORM
     ============================== */

  openCreateForm(): void {

    this.editingService = null;

    this.resetForm();

    this.successMessage = '';
    this.errorMessage = '';

    this.showForm = true;
  }


  /* ==============================
     OPEN EDIT FORM
     ============================== */

  openEditForm(service: ServiceCatalogue): void {

    this.editingService = service;

    this.form = {
      name: service.name,
      category: service.category,
      description: service.description,
      status: service.status,
      unitOfMeasurement: service.unitOfMeasurement,
      standardPrice: service.standardPrice,
      minimumArea: service.minimumArea,
      requiredEquipment:
        service.requiredEquipment ?? '',
      requiredPersonnel:
        service.requiredPersonnel ?? '',
      estimatedDurationMinutes:
        service.estimatedDurationMinutes
    };

    this.successMessage = '';
    this.errorMessage = '';

    this.showForm = true;
  }


  /* ==============================
     CLOSE FORM
     ============================== */

  closeForm(): void {

    if (this.saving) {
      return;
    }

    this.showForm = false;

    this.editingService = null;

    this.resetForm();
  }


  /* ==============================
     RESET FORM
     ============================== */

  resetForm(): void {

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
      estimatedDurationMinutes: 0
    };
  }


  /* ==============================
     SAVE SERVICE
     ============================== */

  saveService(): void {

    this.errorMessage = '';
    this.successMessage = '';

    if (!this.form.name.trim()) {

      this.errorMessage =
        'Service name is required.';

      return;
    }

    if (!this.form.category.trim()) {

      this.errorMessage =
        'Category is required.';

      return;
    }

    if (!this.form.unitOfMeasurement.trim()) {

      this.errorMessage =
        'Unit of measurement is required.';

      return;
    }

    if (this.form.standardPrice < 0) {

      this.errorMessage =
        'Standard price cannot be negative.';

      return;
    }

    if (this.form.minimumArea < 0) {

      this.errorMessage =
        'Minimum area cannot be negative.';

      return;
    }

    if (this.form.estimatedDurationMinutes < 0) {

      this.errorMessage =
        'Estimated duration cannot be negative.';

      return;
    }


    this.saving = true;


    if (this.editingService) {

      this.serviceCatalogueService
        .updateService(
          this.editingService.id,
          this.form
        )
        .subscribe({

          next: () => {

            this.saving = false;

            this.showForm = false;

            this.editingService = null;

            this.successMessage =
              'Service updated successfully.';

            this.resetForm();

            this.loadServices();
          },

          error: (error) => {

            console.error(
              'SERVICE UPDATE ERROR:',
              error
            );

            this.saving = false;

            this.errorMessage =
              this.getSaveErrorMessage(error);

            this.cdr.detectChanges();
          }
        });

    } else {

      this.serviceCatalogueService
        .createService(this.form)
        .subscribe({

          next: () => {

            this.saving = false;

            this.showForm = false;

            this.successMessage =
              'Service created successfully.';

            this.resetForm();

            this.loadServices();
          },

          error: (error) => {

            console.error(
              'SERVICE CREATE ERROR:',
              error
            );

            this.saving = false;

            this.errorMessage =
              this.getSaveErrorMessage(error);

            this.cdr.detectChanges();
          }
        });
    }
  }


  /* ==============================
     DEACTIVATE SERVICE
     ============================== */

  deactivateService(
    service: ServiceCatalogue
  ): void {

    const confirmed =
      window.confirm(
        `Deactivate "${service.name}"?`
      );

    if (!confirmed) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.serviceCatalogueService
      .deactivateService(service.id)
      .subscribe({

        next: () => {

          this.successMessage =
            'Service deactivated successfully.';

          this.loadServices();
        },

        error: (error) => {

          console.error(
            'SERVICE DEACTIVATION ERROR:',
            error
          );

          if (error?.status === 403) {

            this.errorMessage =
              'You do not have permission to deactivate this service.';

          } else {

            this.errorMessage =
              'Unable to deactivate the service.';
          }

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     ERROR HANDLING
     ============================== */

  private getSaveErrorMessage(
    error: any
  ): string {

    if (error?.status === 400) {
      return 'Please check the service details and try again.';
    }

    if (error?.status === 401) {
      return 'Your session has expired. Please log in again.';
    }

    if (error?.status === 403) {
      return 'You do not have permission to perform this action.';
    }

    if (error?.status === 409) {
      return 'A service with these details already exists.';
    }

    return 'Unable to save the service.';
  }
}