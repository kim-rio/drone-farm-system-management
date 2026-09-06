import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { Router } from '@angular/router';

import {
  Client,
  ClientService
} from '../../../services/client.service';

@Component({
  selector: 'app-client-list',
  standalone: true,
  templateUrl: './client-list.html',
  styleUrl: './client-list.scss'
})
export class ClientList implements OnInit {

  private readonly clientService =
    inject(ClientService);

  private readonly router =
    inject(Router);

  private readonly cdr =
    inject(ChangeDetectorRef);

  clients: Client[] = [];

  loading = true;

  errorMessage = '';

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {

    this.loading = true;
    this.errorMessage = '';

    this.clientService
      .getClients()
      .subscribe({

        next: (clients: Client[]) => {

          this.clients = clients;

          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (error: unknown) => {

          console.error(
            'CLIENT LOAD ERROR:',
            error
          );

          this.loading = false;

          if (
            typeof error === 'object' &&
            error !== null &&
            'status' in error
          ) {

            const status =
              (error as { status: number }).status;

            if (status === 401) {

              this.errorMessage =
                'Your session has expired. Please log in again.';

            } else if (status === 403) {

              this.errorMessage =
                'You do not have permission to view clients.';

            } else {

              this.errorMessage =
                'Unable to load clients.';
            }

          } else {

            this.errorMessage =
              'Unable to load clients.';
          }

          this.cdr.detectChanges();
        }
      });
  }

  registerClient(): void {

    this.router.navigate([
      '/management/clients/register'
    ]);
  }

  openClient(client: Client): void {

    if (!client.id) {
      return;
    }

    this.router.navigate([
      '/management/clients',
      client.id
    ]);
  }

  getClientName(client: Client): string {

    if (client.type === 'COMPANY') {

      return client.companyName?.trim()
        || 'Unnamed Company';
    }

    const fullName =
      `${client.firstName ?? ''} ${client.lastName ?? ''}`
        .trim();

    return fullName || 'Unnamed Client';
  }

  getClientInitial(client: Client): string {

    return this.getClientName(client)
      .charAt(0)
      .toUpperCase();
  }
}
