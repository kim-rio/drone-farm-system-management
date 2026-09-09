import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import {
  Client,
  ClientService
} from '../../../services/client.service';

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

import {
  CreateServiceRequestPayload,
  ServiceRequestService
} from '../../../services/service-request.service';


@Component({
  selector: 'app-create-service-request',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './create-service-request.html',
  styleUrl: './create-service-request.scss'
})
export class CreateServiceRequest implements OnInit {

  private readonly clientService =
    inject(ClientService);

  private readonly farmService =
    inject(FarmService);

  private readonly blockService =
    inject(BlockService);

  private readonly catalogueService =
    inject(ServiceCatalogueService);

  private readonly requestService =
    inject(ServiceRequestService);

  private readonly router =
    inject(Router);

  private readonly cdr =
    inject(ChangeDetectorRef);


  /* ==============================
     DATA
     ============================== */

  clients: Client[] = [];

  farms: Farm[] = [];

  blocks: Block[] = [];

  services: ServiceCatalogue[] = [];


  /* ==============================
     FORM
     ============================== */

  selectedClientId: number | null = null;

  selectedFarmId: number | null = null;

  selectedBlockId: number | null = null;

  selectedServiceId: number | null = null;

  notes = '';


  /* ==============================
     STATE
     ============================== */

  loadingClients = true;

  loadingServices = true;

  loadingFarms = false;

  loadingBlocks = false;

  saving = false;

  errorMessage = '';

  successMessage = '';


  /* ==============================
     INIT
     ============================== */

  ngOnInit(): void {

    this.loadClients();

    this.loadServices();
  }


  /* ==============================
     clients
     ============================== */

  loadClients(): void {

    this.loadingClients = true;

    this.clientService
      .getClients()
      .subscribe({

        next: (clients) => {

          this.clients = clients;

          this.loadingClients = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Client LOAD ERROR:',
            error
          );

          this.loadingClients = false;

          this.errorMessage =
            'Unable to load clients.';

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     SERVICES
     ============================== */

  loadServices(): void {

    this.loadingServices = true;

    this.catalogueService
      .getServices()
      .subscribe({

        next: (services) => {

          this.services =
            services.filter(
              service =>
                service.status === 'ACTIVE'
            );

          this.loadingServices = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'SERVICE LOAD ERROR:',
            error
          );

          this.loadingServices = false;

          this.errorMessage =
            'Unable to load available services.';

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     Client CHANGED
     ============================== */

  onClientChange(): void {

    this.selectedFarmId = null;

    this.selectedBlockId = null;

    this.farms = [];

    this.blocks = [];

    this.errorMessage = '';

    if (!this.selectedClientId) {
      return;
    }

    this.loadFarms(
      this.selectedClientId
    );
  }


  /* ==============================
     LOAD FARMS
     ============================== */

  loadFarms(
    clientId: number
  ): void {

    this.loadingFarms = true;

    this.farmService
      .getClientFarms(clientId)
      .subscribe({

        next: (farms) => {

          this.farms = farms;

          this.loadingFarms = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'FARM LOAD ERROR:',
            error
          );

          this.loadingFarms = false;

          this.errorMessage =
            'Unable to load farms for this Client.';

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     FARM CHANGED
     ============================== */

  onFarmChange(): void {

    this.selectedBlockId = null;

    this.blocks = [];

    this.errorMessage = '';

    if (!this.selectedFarmId) {
      return;
    }

    this.loadBlocks(
      this.selectedFarmId
    );
  }


  /* ==============================
     LOAD BLOCKS
     ============================== */

  loadBlocks(
    farmId: number
  ): void {

    this.loadingBlocks = true;

    this.blockService
      .getFarmBlocks(farmId)
      .subscribe({

        next: (blocks) => {

          this.blocks = blocks;

          this.loadingBlocks = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'BLOCK LOAD ERROR:',
            error
          );

          this.loadingBlocks = false;

          this.errorMessage =
            'Unable to load blocks for this farm.';

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     SELECTED SERVICE
     ============================== */

  get selectedService():
    ServiceCatalogue | undefined {

    if (!this.selectedServiceId) {
      return undefined;
    }

    return this.services.find(
      service =>
        service.id === this.selectedServiceId
    );
  }

  get selectedBlock(): Block | undefined {
    return this.blocks.find(block => block.id === this.selectedBlockId);
  }

  get quotedTotal(): number | null {
    if (!this.selectedService || !this.selectedBlock?.areaHectares) return null;
    return this.selectedService.standardPrice * this.selectedBlock.areaHectares;
  }


  /* ==============================
     SUBMIT
     ============================== */

  createRequest(): void {

    this.errorMessage = '';

    if (!this.selectedClientId) {

      this.errorMessage =
        'Please select a Client.';

      return;
    }

    if (!this.selectedFarmId) {

      this.errorMessage =
        'Please select a farm.';

      return;
    }

    if (!this.selectedBlockId) {

      this.errorMessage =
        'Please select a farm block.';

      return;
    }

    if (!this.selectedServiceId) {

      this.errorMessage =
        'Please select a service.';

      return;
    }

    if (!this.selectedBlock?.areaHectares) {
      this.errorMessage = 'The selected block needs an area before it can be serviced.';
      return;
    }

    if (this.selectedService && this.selectedBlock.areaHectares < this.selectedService.minimumArea) {
      this.errorMessage = `This block is below the minimum service area (${this.selectedService.minimumArea} ${this.selectedService.unitOfMeasurement}).`;
      return;
    }


    const payload:
      CreateServiceRequestPayload = {

      client: {
        id: this.selectedClientId
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

      notes:
        this.notes.trim(),

      status:
        'PENDING'
    };


    this.saving = true;


    this.requestService
      .createRequest(payload)
      .subscribe({

        next: (request) => {

          console.log(
            'SERVICE REQUEST CREATED:',
            request
          );

          this.saving = false;

          this.successMessage =
            'Service request created successfully.';

          this.cdr.detectChanges();

          setTimeout(() => {

            this.router.navigate([
              '/management/service-requests'
            ]);

          }, 800);
        },

        error: (error) => {

          console.error(
            'SERVICE REQUEST CREATE ERROR:',
            error
          );

          this.saving = false;

          if (error?.status === 400) {

            this.errorMessage =
              'The selected Client, farm, block or service is invalid.';

          } else if (error?.status === 401) {

            this.errorMessage =
              'Your session has expired. Please log in again.';

          } else if (error?.status === 403) {

            this.errorMessage =
              'You do not have permission to create service requests.';

          } else {

            this.errorMessage =
              'Unable to create the service request.';
          }

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     CANCEL
     ============================== */

  cancel(): void {

    this.router.navigate([
      '/management/service-requests'
    ]);
  }
}
