import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import {
  ServiceRequest,
  ServiceRequestService
} from '../../services/service-request.service';

import {
  Payment,
  PaymentService
} from '../../services/payment.service';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-dashboard.html',
  styleUrl: './customer-dashboard.scss'
})
export class CustomerDashboard implements OnInit {

  private readonly requestService =
    inject(ServiceRequestService);

  private readonly paymentService =
    inject(PaymentService);

  private readonly authService =
    inject(AuthService);

  private readonly router =
    inject(Router);

  private readonly cdr =
    inject(ChangeDetectorRef);

  requests: ServiceRequest[] = [];

  loading = true;
  errorMessage = '';

  payingRequestId: number | null = null;

  userName = '';

  ngOnInit(): void {

    const user =
      this.authService.getCurrentUser();

    if (user) {
      this.userName =
        `${user.firstName} ${user.lastName}`.trim();
    }

    this.loadRequests();
  }

  loadRequests(): void {

    this.loading = true;
    this.errorMessage = '';

    this.requestService
      .getCustomerRequests()
      .subscribe({

        next: (requests) => {

          this.requests = requests;
          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'CUSTOMER REQUEST LOAD ERROR:',
            error
          );

          this.loading = false;

          if (error?.status === 401) {
            this.errorMessage =
              'Your session has expired. Please log in again.';
          } else if (error?.status === 403) {
            this.errorMessage =
              'You do not have permission to access the client portal.';
          } else {
            this.errorMessage =
              'Unable to load your service requests.';
          }

          this.cdr.detectChanges();
        }
      });
  }

  pay(request: ServiceRequest): void {

    if (
      !request.id ||
      request.paymentStatus === 'PAID' ||
      this.payingRequestId !== null
    ) {
      return;
    }

    this.payingRequestId = request.id;
    this.errorMessage = '';

    this.paymentService
      .initiate(request.id)
      .subscribe({

        next: (payment: Payment) => {

          this.payingRequestId = null;

          if (!payment.redirectUrl) {

            this.errorMessage =
              'The payment provider did not return a checkout URL.';

            this.cdr.detectChanges();
            return;
          }

          window.location.href =
            payment.redirectUrl;
        },

        error: (error) => {

          console.error(
            'PAYMENT INITIATION ERROR:',
            error
          );

          this.payingRequestId = null;

          this.errorMessage =
            error?.error?.message ||
            'Unable to start the payment. Please try again.';

          this.cdr.detectChanges();
        }
      });
  }

  viewRequest(request: ServiceRequest): void {

    if (!request.id) {
      return;
    }

    this.router.navigate([
      '/customer',
      'requests',
      request.id
    ]);
  }

  logout(): void {

    this.authService
      .logout()
      .subscribe({
        next: () => {
          this.router.navigate(['/login']);
        },
        error: () => {
          this.authService.clearSession();
          this.router.navigate(['/login']);
        }
      });
  }

  getPaymentLabel(
    request: ServiceRequest
  ): string {

    return request.paymentStatus ||
      'PENDING';
  }

  isPaid(
    request: ServiceRequest
  ): boolean {

    return request.paymentStatus === 'PAID';
  }
}

