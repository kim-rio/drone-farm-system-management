import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import {
  ServiceRequest,
  ServiceRequestService,
  CreateServiceRequestPayload
} from '../../../services/service-request.service';

import {
  Customer,
  CustomerService
} from '../../../services/customer.service';

import {
  Farm,
  FarmService
} from '../../../services/farm.service';

import {
  Block,
  BlockService
} from '../../../services/block.service';

import {
  ServiceCatalogue,
  ServiceCatalogueService
} from '../../../services/service-catalogue.service';


@Component({
  selector: 'app-service-request-details',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl: './service-request-details.html',
  styleUrl: './service-request-details.scss'
})
export class ServiceRequestDetails implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly requestService =
    inject(ServiceRequestService);

  private readonly customerService =
    inject(CustomerService);

  private readonly farmService =
    inject(FarmService);

  private readonly blockService =
    inject(BlockService);

  private readonly catalogueService =
    inject(ServiceCatalogueService);

  private readonly cdr =
    inject(ChangeDetectorRef);


  /* ==============================
     STATE
     ============================== */

  request: ServiceRequest | null = null;

  loading = true;

  saving = false;

  deleting = false;

  errorMessage = '';

  successMessage = '';

  editing = false;


  /* ==============================
     EDIT FORM
     ============================== */

  customers: Customer[] = [];

  farms: Farm[] = [];

  blocks: Block[] = [];

  services: ServiceCatalogue[] = [];


  selectedCustomerId: number | null = null;

  selectedFarmId: number | null = null;

  selectedBlockId: number | null = null;

  selectedServiceId: number | null = null;

  requestedDate = '';

  notes = '';


  /* ==============================
     INIT
     ============================== */

  ngOnInit(): void {

    const id =
      Number(
        this.route.snapshot.paramMap.get('id')
      );

    if (!id) {

      this.errorMessage =
        'Invalid service request ID.';

      this.loading = false;

      return;
    }

    this.loadRequest(id);
  }


  /* ==============================
     LOAD REQUEST
     ============================== */

  loadRequest(id: number): void {

    this.loading = true;

    this.errorMessage = '';

    this.requestService
      .getRequest(id)
      .subscribe({

        next: (request) => {

          console.log(
            'SERVICE REQUEST:',
            request
          );

          this.request = request;

          this.populateForm(request);

          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'REQUEST LOAD ERROR:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'Unable to load this service request.';

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     POPULATE FORM
     ============================== */

  populateForm(
    request: ServiceRequest
  ): void {

    this.selectedCustomerId =
      request.customer?.id ?? null;

    this.selectedFarmId =
      request.farm?.id ?? null;

    this.selectedBlockId =
      request.farmBlock?.id ?? null;

    this.selectedServiceId =
      request.serviceCatalogue?.id ?? null;

    this.requestedDate =
      request.requestedDate ?? '';

    this.notes =
      request.notes ?? '';
  }


  /* ==============================
     START EDIT
     ============================== */

  startEditing(): void {

    this.errorMessage = '';

    this.successMessage = '';

    this.editing = true;

    this.loadCustomers();

    this.loadServices();

    if (this.selectedCustomerId) {

      this.loadFarms(
        this.selectedCustomerId
      );
    }

    if (this.selectedFarmId) {

      this.loadBlocks(
        this.selectedFarmId
      );
    }
  }


  /* ==============================
     CANCEL EDIT
     ============================== */

  cancelEditing(): void {

    if (this.request) {

      this.populateForm(
        this.request
      );
    }

    this.editing = false;

    this.errorMessage = '';
  }


  /* ==============================
     CUSTOMERS
     ============================== */

  loadCustomers(): void {

    this.customerService
      .getCustomers()
      .subscribe({

        next: (customers) => {

          this.customers = customers;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'CUSTOMER LOAD ERROR:',
            error
          );
        }
      });
  }


  /* ==============================
     FARMS
     ============================== */

  loadFarms(
    customerId: number
  ): void {

    this.farms = [];

    this.blocks = [];

    this.selectedFarmId = null;

    this.selectedBlockId = null;

    this.farmService
      .getCustomerFarms(customerId)
      .subscribe({

        next: (farms) => {

          this.farms = farms;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'FARM LOAD ERROR:',
            error
          );
        }
      });
  }


  onCustomerChange(): void {

    if (!this.selectedCustomerId) {

      this.farms = [];

      this.blocks = [];

      this.selectedFarmId = null;

      this.selectedBlockId = null;

      return;
    }

    this.loadFarms(
      this.selectedCustomerId
    );
  }


  /* ==============================
     BLOCKS
     ============================== */

  loadBlocks(
    farmId: number
  ): void {

    this.blocks = [];

    this.selectedBlockId = null;

    this.blockService
      .getFarmBlocks(farmId)
      .subscribe({

        next: (blocks) => {

          this.blocks = blocks;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'BLOCK LOAD ERROR:',
            error
          );
        }
      });
  }


  onFarmChange(): void {

    if (!this.selectedFarmId) {

      this.blocks = [];

      this.selectedBlockId = null;

      return;
    }

    this.loadBlocks(
      this.selectedFarmId
    );
  }


  /* ==============================
     SERVICE CATALOGUE
     ============================== */

  loadServices(): void {

    this.catalogueService
      .getServices()
      .subscribe({

        next: (services) => {

          this.services =
            services.filter(
              service =>
                service.status === 'ACTIVE' ||
                service.id === this.selectedServiceId
            );

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'SERVICE LOAD ERROR:',
            error
          );
        }
      });
  }


  /* ==============================
     SAVE
     ============================== */

  saveChanges(): void {

    if (!this.request) {
      return;
    }

    if (
      !this.selectedCustomerId ||
      !this.selectedFarmId ||
      !this.selectedBlockId ||
      !this.selectedServiceId ||
      !this.requestedDate
    ) {

      this.errorMessage =
        'Please complete all required fields.';

      return;
    }

    const payload:
      CreateServiceRequestPayload = {

      customer: {
        id: this.selectedCustomerId
      },

      farm: {
        id: this.selectedFarmId
      },

      farmBlock: {
        id: this.selectedBlockId
      },

      serviceCatalogue: {
        id: this.selectedServiceId
      },

      requestedDate:
        this.requestedDate,

      notes:
        this.notes,

      status:
        this.request.status
    };


    this.saving = true;

    this.errorMessage = '';

    this.successMessage = '';


    this.requestService
      .updateRequest(
        this.request.id,
        payload
      )
      .subscribe({

        next: (updatedRequest) => {

          this.request =
            updatedRequest;

          this.populateForm(
            updatedRequest
          );

          this.editing = false;

          this.saving = false;

          this.successMessage =
            'Service request updated successfully.';

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'REQUEST UPDATE ERROR:',
            error
          );

          this.saving = false;

          this.errorMessage =
            'Unable to update service request.';

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     STATUS
     ============================== */

  changeStatus(
    status: string
  ): void {

    if (!this.request) {
      return;
    }

    this.errorMessage = '';

    this.successMessage = '';


    this.requestService
      .updateStatus(
        this.request.id,
        status
      )
      .subscribe({

        next: () => {

          if (this.request) {

            this.request.status =
              status;
          }

          this.successMessage =
            `Request status changed to ${status}.`;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'STATUS UPDATE ERROR:',
            error
          );

          this.errorMessage =
            'Unable to update request status.';

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     DELETE
     ============================== */

  deleteRequest(): void {

    if (!this.request) {
      return;
    }

    const confirmed =
      window.confirm(
        `Delete service request #${this.request.id}?`
      );

    if (!confirmed) {
      return;
    }


    this.deleting = true;

    this.errorMessage = '';


    this.requestService
      .deleteRequest(
        this.request.id
      )
      .subscribe({

        next: () => {

          this.router.navigate([
            '/management/service-requests'
          ]);
        },

        error: (error) => {

          console.error(
            'DELETE ERROR:',
            error
          );

          this.deleting = false;

          this.errorMessage =
            'Unable to delete service request.';

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     NAVIGATION
     ============================== */

  backToRequests(): void {

    this.router.navigate([
      '/management/service-requests'
    ]);
  }


  /* ==============================
     CUSTOMER DISPLAY
     ============================== */

  getCustomerName(): string {

    if (!this.request?.customer) {
      return 'Unknown Customer';
    }

    const customer =
      this.request.customer;

    if (customer.companyName?.trim()) {
      return customer.companyName;
    }

    const fullName =
      `${customer.firstName ?? ''} ${customer.lastName ?? ''}`
        .trim();

    return fullName ||
      customer.customerCode ||
      'Unknown Customer';
  }
}