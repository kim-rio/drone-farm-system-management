import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';

import { Router } from '@angular/router';

import {
  Client,
  ClientService
} from '../../services/client.service';

import {
  Farm,
  FarmService
} from '../../services/farm.service';

interface FarmWithClient {
  farm: Farm;
  client: Client;
}

@Component({
  selector: 'app-farm-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './farm-list.html',
  styleUrl: './farm-list.scss'
})
export class FarmList implements OnInit {

  private readonly clientService = inject(ClientService);
  private readonly farmService = inject(FarmService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  farms: FarmWithClient[] = [];

  loading = true;

  errorMessage = '';

  ngOnInit(): void {
    this.loadFarms();
  }

  loadFarms(): void {

    this.loading = true;
    this.errorMessage = '';

    this.clientService.getClients().subscribe({

      next: (clients: Client[]) => {

        if (!clients.length) {
          this.farms = [];
          this.loading = false;
          this.cdr.detectChanges();
          return;
        }

        let completed = 0;

        const results: FarmWithClient[] = [];

        for (const client of clients) {

          if (!client.id) {
            completed++;

            if (completed === clients.length) {
              this.farms = results;
              this.loading = false;
              this.cdr.detectChanges();
            }

            continue;
          }

          this.farmService.getClientFarms(client.id).subscribe({

            next: (farms: Farm[]) => {

              for (const farm of farms) {

                results.push({
                  farm,
                  client
                });

              }

              completed++;

              if (completed === clients.length) {

                this.farms = results;
                this.loading = false;

                this.cdr.detectChanges();

              }

            },

            error: (error: unknown) => {

              console.error(
                'FARMS LOAD ERROR:',
                error
              );

              completed++;

              if (completed === clients.length) {

                this.farms = results;
                this.loading = false;

                this.cdr.detectChanges();

              }

            }

          });

        }

      },

      error: (error: unknown) => {

        console.error(
          'CLIENT LOAD ERROR:',
          error
        );

        this.loading = false;
        this.errorMessage =
          'Unable to load farms.';

        this.cdr.detectChanges();

      }

    });

  }

  getClientName(client: Client): string {

    if (client.companyName?.trim()) {
      return client.companyName;
    }

    const name =
      `${client.firstName ?? ''} ${client.lastName ?? ''}`
        .trim();

    return name || client.clientCode;
  }

  openFarm(item: FarmWithClient): void {

    this.router.navigate([
      '/management/clients',
      item.client.id,
      'farms',
      item.farm.id
    ]);

  }

  openClient(client: Client): void {

    this.router.navigate([
      '/management/clients',
      client.id
    ]);

  }

  backToDashboard(): void {
    this.router.navigate(['/management']);
  }

}
