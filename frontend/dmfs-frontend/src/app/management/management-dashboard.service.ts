import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ManagementDashboard {
  clients: number;
  farms: number;
  blocks: number;
  serviceRequests: number;
  pendingPayments: number;
  paidPayments: number;
  requestStatuses: Record<string, number>;
}

@Injectable({ providedIn: 'root' })
export class ManagementDashboardService {
  private readonly http = inject(HttpClient);

  getDashboard(): Observable<ManagementDashboard> {
    return this.http.get<ManagementDashboard>('/api/management/dashboard');
  }
}
