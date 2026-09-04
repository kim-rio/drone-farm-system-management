import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ServiceRequest {
  id: number;

  customer: {
    id: number;
    customerCode?: string;
    type?: string;
    companyName?: string;
    firstName?: string;
    lastName?: string;
    email?: string;
    phone?: string;
    status?: string;
  };

  farm: {
    id: number;
    name?: string;
    description?: string;
    areaHectares?: number;
  };

  farmBlock: {
    id: number;
    name?: string;
    description?: string;
    areaHectares?: number;
    centerLatitude?: number;
    centerLongitude?: number;
  };

  serviceCatalogue: {
    id: number;
    name?: string;
    category?: string;
    description?: string;
    status?: string;
    unitOfMeasurement?: string;
    standardPrice?: number;
    minimumArea?: number;
    estimatedDurationMinutes?: number;
  };

  requestedDate: string;

  notes?: string;

  status: string;

  createdAt?: string;

  updatedAt?: string;
}

export interface CreateServiceRequestPayload {

  customer: {
    id: number;
  };

  farm: {
    id: number;
  };

  farmBlock: {
    id: number;
  };

  serviceCatalogue: {
    id: number;
  };

  requestedDate: string;

  notes?: string;

  status?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ServiceRequestService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/service-requests';

  getRequests(): Observable<ServiceRequest[]> {
    return this.http.get<ServiceRequest[]>(
      this.apiUrl,
      {
        withCredentials: true
      }
    );
  }

  getRequest(
    id: number
  ): Observable<ServiceRequest> {
    return this.http.get<ServiceRequest>(
      `${this.apiUrl}/${id}`,
      {
        withCredentials: true
      }
    );
  }

  createRequest(
    request: CreateServiceRequestPayload
  ): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>(
      this.apiUrl,
      request,
      {
        withCredentials: true
      }
    );
  }

  updateRequest(
    id: number,
    request: CreateServiceRequestPayload
  ): Observable<ServiceRequest> {
    return this.http.put<ServiceRequest>(
      `${this.apiUrl}/${id}`,
      request,
      {
        withCredentials: true
      }
    );
  }

  updateStatus(
    id: number,
    status: string
  ): Observable<void> {
    return this.http.patch<void>(
      `${this.apiUrl}/${id}/status`,
      {},
      {
        params: {
          status
        },
        withCredentials: true
      }
    );
  }

  deleteRequest(
    id: number
  ): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`,
      {
        withCredentials: true
      }
    );
  }
}
