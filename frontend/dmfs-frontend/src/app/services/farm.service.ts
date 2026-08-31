import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Farm {
  id: number;

  name: string;

  description: string | null;

  latitude: number | null;

  longitude: number | null;

  areaHectares: number | null;

  customerId: number | null;

  createdAt?: string;

  updatedAt?: string;
}

export interface CreateFarmRequest {
  name: string;

  description?: string;

  latitude: number;

  longitude: number;

  areaHectares?: number;
}

@Injectable({
  providedIn: 'root'
})
export class FarmService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/farms';

  createFarm(
    customerId: number,
    farm: CreateFarmRequest
  ): Observable<Farm> {

    return this.http.post<Farm>(
      `${this.apiUrl}/customer/${customerId}`,
      farm,
      {
        withCredentials: true
      }
    );
  }

  getCustomerFarms(
    customerId: number
  ): Observable<Farm[]> {

    return this.http.get<Farm[]>(
      `${this.apiUrl}/customer/${customerId}`,
      {
        withCredentials: true
      }
    );
  }

  getFarm(
    id: number
  ): Observable<Farm> {

    return this.http.get<Farm>(
      `${this.apiUrl}/${id}`,
      {
        withCredentials: true
      }
    );
  }

  deleteFarm(
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