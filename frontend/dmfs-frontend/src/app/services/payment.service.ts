import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Payment {
  id: number;
  serviceRequestId: number;
  amount: number;
  currency: string;
  status: string;
  provider: string;
  merchantReference: string;
  providerTrackingId?: string;
  confirmationCode?: string;
  paymentMethod?: string;
  redirectUrl?: string;
  failureReason?: string;
  paidAt?: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private readonly http = inject(HttpClient);

  initiate(
    serviceRequestId: number
  ): Observable<Payment> {

    return this.http.post<Payment>(
      `/api/customer/payments/service-requests/${serviceRequestId}`,
      {},
      {
        withCredentials: true
      }
    );
  }

  getStatus(
    paymentId: number
  ): Observable<Payment> {

    return this.http.get<Payment>(
      `/api/customer/payments/${paymentId}`,
      {
        withCredentials: true
      }
    );
  }
}
