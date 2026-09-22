import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  Client,
  ClientService,
  CreateClientRequest,
  ClientType
} from '../../../services/client.service';

import {
  FarmService,
  CreateFarmRequest
} from '../../../services/farm.service';

import {
  BlockService,
  CreateBlockRequest
} from '../../../services/block.service';

interface RegistrationBlock extends CreateBlockRequest {
  tempId: number;
}

interface RegistrationFarm extends CreateFarmRequest {
  tempId: number;
  blocks: RegistrationBlock[];
}

@Component({
  selector: 'app-register-clients',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './register-clients.html',
  styleUrl: './register-clients.scss'
})
export class RegisterClients {

  private readonly clientService = inject(ClientService);
  private readonly farmService = inject(FarmService);
  private readonly blockService = inject(BlockService);
  private readonly router = inject(Router);

  loading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  private nextFarmId = 1;
  private nextBlockId = 1;

  client: CreateClientRequest = {
    clientCode: '',
    type: 'INDIVIDUAL',
    companyName: '',
    registrationNumber: '',
    tin: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: ''
  };

  farms: RegistrationFarm[] = [];

  setType(type: ClientType): void {
    this.client.type = type;

    this.errorMessage.set('');
    this.successMessage.set('');

    if (type === 'INDIVIDUAL') {
      this.client.companyName = '';
      this.client.registrationNumber = '';
      this.client.tin = '';
    } else {
      this.client.firstName = '';
      this.client.lastName = '';
    }
  }

  addFarm(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    this.farms.push({
      tempId: this.nextFarmId++,
      name: '',
      description: '',
      latitude: undefined as unknown as number,
      longitude: undefined as unknown as number,
      areaHectares: undefined,
      blocks: []
    });
  }

  removeFarm(index: number): void {
    if (this.loading()) {
      return;
    }

    this.farms.splice(index, 1);

    this.errorMessage.set('');
    this.successMessage.set('');
  }

  addBlock(farm: RegistrationFarm): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    farm.blocks.push({
      tempId: this.nextBlockId++,
      name: '',
      description: '',
      areaHectares: undefined,
      centerLatitude: undefined,
      centerLongitude: undefined
    });
  }

  removeBlock(
    farm: RegistrationFarm,
    blockIndex: number
  ): void {
    if (this.loading()) {
      return;
    }

    farm.blocks.splice(blockIndex, 1);

    this.errorMessage.set('');
    this.successMessage.set('');
  }

  canRegister(): boolean {
    if (this.loading()) {
      return false;
    }

    if (this.farms.length === 0) {
      return false;
    }

    return this.farms.every(
      farm => farm.blocks.length > 0
    );
  }

  register(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    const validationError = this.validateRegistration();

    if (validationError) {
      this.errorMessage.set(validationError);
      return;
    }

    this.client.clientCode =
      'CLI-' + Date.now().toString().slice(-6);

    this.loading.set(true);

    /*
     * Step 1:
     * Create the client.
     */
    this.clientService
      .createClient(this.client)
      .subscribe({

        next: (createdClient: Client) => {

          console.log(
            'CLIENT CREATED:',
            createdClient
          );

          /*
           * Step 2:
           * Create each farm for the newly created client.
           */
          this.createFarms(
            createdClient.id,
            0
          );
        },

        error: (error: unknown) => {
          this.handleRegistrationError(
            error,
            'Unable to register client.'
          );
        }
      });
  }

  private createFarms(
    clientId: number,
    farmIndex: number
  ): void {

    if (farmIndex >= this.farms.length) {

      this.loading.set(false);

      this.successMessage.set(
        'Client, farms and blocks registered successfully.'
      );

      setTimeout(() => {
        this.router.navigate(['/management/clients']);
      }, 900);

      return;
    }

    const farm = this.farms[farmIndex];

    this.farmService
      .createFarm(
        clientId,
        {
          name: farm.name.trim(),
          description: farm.description?.trim(),
          latitude: Number(farm.latitude),
          longitude: Number(farm.longitude),
          areaHectares: Number(farm.areaHectares)
        }
      )
      .subscribe({

        next: (createdFarm) => {

          console.log(
            'FARM CREATED:',
            createdFarm
          );

          /*
           * Step 3:
           * Create all blocks belonging to this farm.
           */
          this.createBlocks(
            createdFarm.id,
            farm,
            0,
            () => {
              this.createFarms(
                clientId,
                farmIndex + 1
              );
            }
          );
        },

        error: (error: unknown) => {
          this.handleRegistrationError(
            error,
            `Unable to create farm "${farm.name}".`
          );
        }
      });
  }

  private createBlocks(
    farmId: number,
    farm: RegistrationFarm,
    blockIndex: number,
    onComplete: () => void
  ): void {

    if (blockIndex >= farm.blocks.length) {
      onComplete();
      return;
    }

    const block = farm.blocks[blockIndex];

    this.blockService
      .createBlock(
        farmId,
        {
          name: block.name.trim(),
          description: block.description?.trim(),
          areaHectares:
            block.areaHectares === undefined
              ? undefined
              : Number(block.areaHectares),
          centerLatitude:
            block.centerLatitude === undefined
              ? undefined
              : Number(block.centerLatitude),
          centerLongitude:
            block.centerLongitude === undefined
              ? undefined
              : Number(block.centerLongitude)
        }
      )
      .subscribe({

        next: (createdBlock) => {

          console.log(
            'BLOCK CREATED:',
            createdBlock
          );

          this.createBlocks(
            farmId,
            farm,
            blockIndex + 1,
            onComplete
          );
        },

        error: (error: unknown) => {
          this.handleRegistrationError(
            error,
            `Unable to create block "${block.name}".`
          );
        }
      });
  }

  private validateRegistration(): string | null {

    if (!this.client.email?.trim()) {
      return this.client.type === 'COMPANY'
        ? 'Company email is required.'
        : 'Individual email is required.';
    }

    if (!this.client.phone?.trim()) {
      return this.client.type === 'COMPANY'
        ? 'Company phone is required.'
        : 'Individual phone is required.';
    }

    if (this.client.type === 'INDIVIDUAL') {

      if (!this.client.firstName?.trim()) {
        return 'First name is required.';
      }

      if (!this.client.lastName?.trim()) {
        return 'Last name is required.';
      }

      this.client.companyName = '';
      this.client.registrationNumber = '';
      this.client.tin = '';

    } else {

      if (!this.client.companyName?.trim()) {
        return 'Company name is required.';
      }

      this.client.firstName = '';
      this.client.lastName = '';
    }

    if (this.farms.length === 0) {
      return 'Add at least one farm before registering the client.';
    }

    for (let i = 0; i < this.farms.length; i++) {

      const farm = this.farms[i];

      if (!farm.name.trim()) {
        return `Farm ${i + 1}: farm name is required.`;
      }

      if (
        farm.latitude === undefined ||
        farm.latitude === null ||
        Number.isNaN(Number(farm.latitude))
      ) {
        return `Farm ${i + 1}: latitude is required.`;
      }

      if (
        Number(farm.latitude) < -90 ||
        Number(farm.latitude) > 90
      ) {
        return `Farm ${i + 1}: latitude must be between -90 and 90.`;
      }

      if (
        farm.longitude === undefined ||
        farm.longitude === null ||
        Number.isNaN(Number(farm.longitude))
      ) {
        return `Farm ${i + 1}: longitude is required.`;
      }

      if (
        Number(farm.longitude) < -180 ||
        Number(farm.longitude) > 180
      ) {
        return `Farm ${i + 1}: longitude must be between -180 and 180.`;
      }

      if (
        farm.areaHectares === undefined ||
        farm.areaHectares === null ||
        Number(farm.areaHectares) <= 0
      ) {
        return `Farm ${i + 1}: area must be greater than 0 hectares.`;
      }

      if (farm.blocks.length === 0) {
        return `Farm ${i + 1}: add at least one block.`;
      }

      for (
        let blockIndex = 0;
        blockIndex < farm.blocks.length;
        blockIndex++
      ) {

        const block = farm.blocks[blockIndex];

        if (!block.name.trim()) {
          return `Farm ${i + 1}, Block ${blockIndex + 1}: block name is required.`;
        }

        if (
          block.areaHectares !== undefined &&
          block.areaHectares !== null &&
          Number(block.areaHectares) < 0
        ) {
          return `Farm ${i + 1}, Block ${blockIndex + 1}: area cannot be negative.`;
        }

        if (
          block.centerLatitude !== undefined &&
          block.centerLatitude !== null &&
          (
            Number(block.centerLatitude) < -90 ||
            Number(block.centerLatitude) > 90
          )
        ) {
          return `Farm ${i + 1}, Block ${blockIndex + 1}: latitude must be between -90 and 90.`;
        }

        if (
          block.centerLongitude !== undefined &&
          block.centerLongitude !== null &&
          (
            Number(block.centerLongitude) < -180 ||
            Number(block.centerLongitude) > 180
          )
        ) {
          return `Farm ${i + 1}, Block ${blockIndex + 1}: longitude must be between -180 and 180.`;
        }
      }
    }

    return null;
  }

  private handleRegistrationError(
    error: unknown,
    fallbackMessage: string
  ): void {

    console.error(
      'CLIENT REGISTRATION ERROR:',
      error
    );

    this.loading.set(false);

    if (
      typeof error === 'object' &&
      error !== null &&
      'error' in error
    ) {

      const response = error as {
        error?: {
          message?: string;
        };
      };

      this.errorMessage.set(
        response.error?.message ??
        fallbackMessage
      );

    } else {

      this.errorMessage.set(
        fallbackMessage
      );
    }
  }

  cancel(): void {
    this.router.navigate(['/management/clients']);
  }
}

