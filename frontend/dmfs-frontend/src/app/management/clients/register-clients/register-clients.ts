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
  Customer,
  CustomerService,
  CreateCustomerRequest,
  ClientType
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

  register(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (!this.client.email?.trim()) {
      this.errorMessage.set(
        this.client.type === 'COMPANY'
          ? 'Company email is required.'
          : 'Individual email is required.'
      );
      return;
    }

    if (!this.client.phone?.trim()) {
      this.errorMessage.set(
        this.client.type === 'COMPANY'
          ? 'Company phone is required.'
          : 'Individual phone is required.'
      );
      return;
    }

    if (this.client.type === 'INDIVIDUAL') {
      if (!this.client.firstName?.trim()) {
        this.errorMessage.set('First name is required.');
        return;
      }

      if (!this.client.lastName?.trim()) {
        this.errorMessage.set('Last name is required.');
        return;
      }

      this.client.companyName = '';
      this.client.registrationNumber = '';
      this.client.tin = '';
    } else {
      if (!this.client.companyName?.trim()) {
        this.errorMessage.set('Company name is required.');
        return;
      }

      this.client.firstName = '';
      this.client.lastName = '';
    }

    this.client.clientCode =
      'CLI-' + Date.now().toString().slice(-6);

    this.loading.set(true);

    this.customerService.createCustomer(this.client).subscribe({
      next: (customer: Customer) => {
        console.log('CLIENT CREATED:', customer);

        this.loading.set(false);
        this.successMessage.set(
          'Client registered successfully.'
        );

        setTimeout(() => {
          this.router.navigate(['/management/clients']);
        }, 700);
      },

      error: (error: unknown) => {
        console.error('CLIENT REGISTRATION ERROR:', error);

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
            'Unable to register client.'
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
    this.router.navigate(['/management/clients']);
  }
}
