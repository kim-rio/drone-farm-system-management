import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ServiceCatalogue {
  id: number;
  name: string;
  category: string;
  description: string;
  status: 'ACTIVE' | 'INACTIVE';
  unitOfMeasurement: string;
  standardPrice: number;
  minimumArea: number;
  requiredEquipment?: string;
  requiredPersonnel?: string;
  estimatedDurationMinutes: number;
  createdAt: string;
  updatedAt: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class ServiceCatalogueService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/service-catalogue';

  getServices(): Observable<ServiceCatalogue[]> {
    return this.http.get<ServiceCatalogue[]>(this.apiUrl);
  }

  getService(id: number): Observable<ServiceCatalogue> {
    return this.http.get<ServiceCatalogue>(
      `${this.apiUrl}/${id}`
    );
  }

  createService(
    service: Omit<ServiceCatalogue, 'id' | 'createdAt' | 'updatedAt'>
  ): Observable<ServiceCatalogue> {
    return this.http.post<ServiceCatalogue>(
      this.apiUrl,
      service
    );
  }

  updateService(
    id: number,
    service: Omit<ServiceCatalogue, 'id' | 'createdAt' | 'updatedAt'>
  ): Observable<ServiceCatalogue> {
    return this.http.put<ServiceCatalogue>(
      `${this.apiUrl}/${id}`,
      service
    );
  }

  deactivateService(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }
}
