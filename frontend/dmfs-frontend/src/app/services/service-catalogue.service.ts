import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ServiceCatalogue {
  id: number;
  name: string;
  category: string;
  description: string;
  status: string;
  unitOfMeasurement: string;
  standardPrice: number;
  minimumArea: number;
  requiredEquipment?: string;
  requiredPersonnel?: string;
  estimatedDurationMinutes: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateServiceCatalogueRequest {
  name: string;
  category: string;
  description: string;
  status?: string;
  unitOfMeasurement: string;
  standardPrice: number;
  minimumArea: number;
  requiredEquipment?: string;
  requiredPersonnel?: string;
  estimatedDurationMinutes: number;
}

@Injectable({
  providedIn: 'root'
})
export class ServiceCatalogueService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/service-catalogue';

  getServices(): Observable<ServiceCatalogue[]> {
    return this.http.get<ServiceCatalogue[]>(
      this.apiUrl,
      {
        withCredentials: true
      }
    );
  }

  getService(id: number): Observable<ServiceCatalogue> {
    return this.http.get<ServiceCatalogue>(
      `${this.apiUrl}/${id}`,
      {
        withCredentials: true
      }
    );
  }

  createService(
    service: CreateServiceCatalogueRequest
  ): Observable<ServiceCatalogue> {
    return this.http.post<ServiceCatalogue>(
      this.apiUrl,
      service,
      {
        withCredentials: true
      }
    );
  }

  updateService(
    id: number,
    service: CreateServiceCatalogueRequest
  ): Observable<ServiceCatalogue> {
    return this.http.put<ServiceCatalogue>(
      `${this.apiUrl}/${id}`,
      service,
      {
        withCredentials: true
      }
    );
  }

  deactivateService(
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
