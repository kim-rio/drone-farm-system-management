import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { Router } from '@angular/router';

import {
  Customer,
  CustomerService
} from '../../../services/customer.service';

@Component({
  selector: 'app-client-list',
  standalone: true,
  templateUrl: './client-list.html',
  styleUrl: './client-list.scss'
})
export class ClientList implements OnInit {

  private readonly customerService = inject(CustomerService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  clients: Customer[] = [];

  loading = true;

  errorMessage = '';

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {

    this.loading = true;
    this.errorMessage = '';

    this.customerService.getCustomers().subscribe({

      next: (customers: Customer[]) => {

        console.log('CUSTOMERS RECEIVED:', customers);

        this.clients = customers;

        this.loading = false;

        console.log('LOADING:', this.loading);
        console.log('CLIENT COUNT:', this.clients.length);

        // Force Angular to update the page
        this.cdr.detectChanges();
      },

      error: (error: unknown) => {

        console.error('CUSTOMER LOAD ERROR:', error);

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

        // Force Angular to update the page
        this.cdr.detectChanges();
      }
    });
  }

  registerClient(): void {

    this.router.navigate([
      '/management/clients/register'
    ]);
  }

  openClient(client: Customer): void {

    if (!client.id) {
      return;
    }

    this.router.navigate([
      '/management/clients',
      client.id
    ]);
  }

  getClientName(client: Customer): string {

    if (client.companyName?.trim()) {
      return client.companyName;
    }

    const fullName =
      `${client.firstName ?? ''} ${client.lastName ?? ''}`
        .trim();

    return fullName || 'Unnamed Client';
  }
}