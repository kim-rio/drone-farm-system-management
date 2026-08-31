import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import {
  Customer,
  CustomerService,
  CreateCustomerRequest
} from '../../../services/customer.service';

@Component({
  selector: 'app-register-clients',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './register-clients.html',
  styleUrl: './register-clients.scss'
})
export class RegisterClients {

  private readonly customerService = inject(CustomerService);
  private readonly router = inject(Router);

  loading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  client: CreateCustomerRequest = {
    customerCode: '',
    type: 'INDIVIDUAL',
    companyName: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
    identificationNumber: '',
    tin: '',
    password: ''
  };

  register(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (!this.client.firstName?.trim()) {
      this.errorMessage.set('First name is required.');
      return;
    }

    if (!this.client.lastName?.trim()) {
      this.errorMessage.set('Last name is required.');
      return;
    }

    if (!this.client.email?.trim()) {
      this.errorMessage.set('Email is required.');
      return;
    }

    if (!this.client.phone?.trim()) {
      this.errorMessage.set('Contact number is required.');
      return;
    }

    if (!this.client.password?.trim()) {
      this.errorMessage.set('Password is required.');
      return;
    }

    this.client.customerCode =
      'CUS-' + Date.now().toString().slice(-6);

    this.loading.set(true);

    this.customerService
      .createCustomer(this.client)
      .subscribe({
        next: (customer: Customer) => {
          console.log('CLIENT CREATED:', customer);

          this.loading.set(false);
          this.successMessage.set(
            'Client registered successfully.'
          );

          setTimeout(() => {
            this.router.navigate([
              '/management/clients'
            ]);
          }, 700);
        },

        error: (error: unknown) => {
          console.error(
            'CLIENT CREATION ERROR:',
            error
          );

          this.loading.set(false);

          const status =
            typeof error === 'object' &&
            error !== null &&
            'status' in error
              ? Number(
                  (error as { status: number }).status
                )
              : 0;

          if (status === 400) {
            this.errorMessage.set(
              'The information provided is invalid.'
            );
          } else if (status === 401) {
            this.errorMessage.set(
              'Your session has expired. Please log in again.'
            );
          } else if (status === 403) {
            this.errorMessage.set(
              'You do not have permission to register clients.'
            );
          } else if (status === 409) {
            this.errorMessage.set(
              'A client with this email already exists.'
            );
          } else {
            this.errorMessage.set(
              'Unable to register client.'
            );
          }
        }
      });
  }

  cancel(): void {
    this.router.navigate([
      '/management/clients'
    ]);
  }
}
