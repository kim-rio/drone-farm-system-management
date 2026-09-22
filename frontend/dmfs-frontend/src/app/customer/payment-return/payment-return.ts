import { CommonModule } from '@angular/common';

import {
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  inject
} from '@angular/core';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  Payment,
  PaymentService
} from '../../services/payment.service';

@Component({
  selector: 'app-payment-return',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-return.html',
  styleUrl: './payment-return.scss'
})
export class PaymentReturn
  implements OnInit, OnDestroy {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly paymentService =
    inject(PaymentService);

  private readonly cdr =
    inject(ChangeDetectorRef);

  payment: Payment | null = null;

  paymentId: number | null = null;

  loading = true;

  message =
    'Checking your payment status...';

  private pollTimer:
    ReturnType<typeof setTimeout> | null = null;

  private attempts = 0;

  private readonly maxAttempts = 8;

  ngOnInit(): void {

    const id =
      Number(
        this.route.snapshot.queryParamMap
          .get('paymentId')
      );

    if (!id || Number.isNaN(id)) {

      this.loading = false;

      this.message =
        'No payment reference was supplied.';

      return;
    }

    this.paymentId = id;

    this.checkStatus();
  }

  checkStatus(): void {

    if (!this.paymentId) {
      return;
    }

    this.paymentService
      .getStatus(this.paymentId)
      .subscribe({

        next: (payment) => {

          this.payment = payment;

          if (payment.status === 'PAID') {

            this.loading = false;
            this.message =
              'Payment verified successfully.';

            this.cdr.detectChanges();
            return;
          }

          if (
            payment.status === 'FAILED' ||
            payment.status === 'CANCELLED'
          ) {

            this.loading = false;

            this.message =
              payment.failureReason ||
              'The payment was not completed.';

            this.cdr.detectChanges();
            return;
          }

          this.attempts++;

          if (this.attempts >= this.maxAttempts) {

            this.loading = false;

            this.message =
              'Payment is still being processed. Please check your dashboard again shortly.';

            this.cdr.detectChanges();
            return;
          }

          this.message =
            'Payment is being verified securely...';

          this.cdr.detectChanges();

          this.pollTimer =
            setTimeout(
              () => this.checkStatus(),
              3000
            );
        },

        error: (error) => {

          console.error(
            'PAYMENT STATUS ERROR:',
            error
          );

          this.loading = false;

          this.message =
            'Unable to verify the payment status right now.';

          this.cdr.detectChanges();
        }
      });
  }

  goDashboard(): void {
    this.router.navigate(['/customer']);
  }

  ngOnDestroy(): void {

    if (this.pollTimer) {
      clearTimeout(this.pollTimer);
    }
  }
}
