import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type ServiceCatalogueStatus = 'ACTIVE' | 'INACTIVE';

export interface ServiceCatalogue {
  id: number;
  name: string;
  category: string;
  description: string;
  status: ServiceCatalogueStatus;
  unitOfMeasurement: string;
  standardPrice: number;
  minimumArea: number;
  requiredEquipment?: string;
  requiredPersonnel?: string;
  estimatedDurationMinutes: number;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreateServiceCatalogueRequest {
  name: string;
  category: string;
  description: string;
  status: ServiceCatalogueStatus;
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

  private readonly apiUrl = '/api/service-catalogue';

  getServices(): Observable<ServiceCatalogue[]> {
    return this.http.get<ServiceCatalogue[]>(
      this.apiUrl
    );
  }

  getService(id: number): Observable<ServiceCatalogue> {
    return this.http.get<ServiceCatalogue>(
      `${this.apiUrl}/${id}`
    );
  }

  createService(
    service: CreateServiceCatalogueRequest
  ): Observable<ServiceCatalogue> {
    return this.http.post<ServiceCatalogue>(
      this.apiUrl,
      service
    );
  }

  updateService(
    id: number,
    service: CreateServiceCatalogueRequest
  ): Observable<ServiceCatalogue> {
    return this.http.put<ServiceCatalogue>(
      `${this.apiUrl}/${id}`,
      service
    );
  }

  deactivateService(
    id: number
  ): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }
}
