import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  Client,
  ClientService
} from '../../../services/client.service';

import {
  Farm,
  FarmService
} from '../../../services/farm.service';

import { AddFarm } from './add-farm/add-farm';

@Component({
  selector: 'app-client-details',
  standalone: true,
  imports: [AddFarm],
  templateUrl: './client-details.html',
  styleUrl: './client-details.scss'
})
export class ClientDetails implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly clientService =
    inject(ClientService);

  private readonly farmService =
    inject(FarmService);

  private readonly cdr =
    inject(ChangeDetectorRef);

  client: Client | null = null;

  farms: Farm[] = [];

  loading = true;

  farmsLoading = true;

  errorMessage = '';

  farmErrorMessage = '';

  showAddFarm = false;

  ngOnInit(): void {

    const idParam =
      this.route.snapshot.paramMap.get('id');

    const clientId =
      Number(idParam);

    if (!clientId || Number.isNaN(clientId)) {

      this.loading = false;

      this.farmsLoading = false;

      this.errorMessage =
        'Invalid client ID.';

      this.cdr.detectChanges();

      return;
    }

    this.loadClient(clientId);

    this.loadFarms(clientId);
  }

  // ==========================================
  // LOAD CLIENT
  // ==========================================

  loadClient(id: number): void {

    this.clientService
      .getClient(id)
      .subscribe({

        next: (client: Client) => {

          console.log(
            'CLIENT DETAILS:',
            client
          );

          this.client = client;

          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (error: unknown) => {

          console.error(
            'CLIENT DETAILS ERROR:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'Unable to load client details.';

          this.cdr.detectChanges();
        }
      });
  }

  // ==========================================
  // LOAD FARMS
  // ==========================================

  loadFarms(clientId: number): void {

    this.farmsLoading = true;

    this.farmErrorMessage = '';

    this.farmService
      .getClientFarms(clientId)
      .subscribe({

        next: (farms: Farm[]) => {

          console.log(
            'CLIENT FARMS:',
            farms
          );

          this.farms = farms;

          this.farmsLoading = false;

          this.cdr.detectChanges();
        },

        error: (error: unknown) => {

          console.error(
            'FARMS ERROR:',
            error
          );

          this.farmsLoading = false;

          this.farmErrorMessage =
            'Unable to load farms.';

          this.cdr.detectChanges();
        }
      });
  }

  // ==========================================
  // OPEN FARM DETAILS
  // ==========================================

  openFarm(farm: Farm): void {

    if (!this.client?.id || !farm.id) {
      return;
    }

    this.router.navigate([
      '/management/clients',
      this.client.id,
      'farms',
      farm.id
    ]);
  }

  // ==========================================
  // ADD FARM
  // ==========================================

  addFarm(): void {

    if (!this.client?.id) {
      return;
    }

    this.showAddFarm = true;

    this.cdr.detectChanges();
  }

  closeAddFarm(): void {

    this.showAddFarm = false;

    this.cdr.detectChanges();
  }

  farmCreated(): void {

    this.showAddFarm = false;

    if (this.client?.id) {
      this.loadFarms(this.client.id);
    }

    this.cdr.detectChanges();
  }

  // ==========================================
  // BACK
  // ==========================================

  backToClients(): void {

    this.router.navigate([
      '/management/clients'
    ]);
  }

  // ==========================================
  // CLIENT NAME
  // ==========================================

  getClientName(): string {

    if (!this.client) {
      return '';
    }

    if (this.client.companyName?.trim()) {
      return this.client.companyName;
    }

    const fullName =
      `${this.client.firstName ?? ''} ${this.client.lastName ?? ''}`
        .trim();

    return fullName || 'Unnamed Client';
  }
}
