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
  clientId: number | null;
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
    clientId: number,
    farm: CreateFarmRequest
  ): Observable<Farm> {

    return this.http.post<Farm>(
      `${this.apiUrl}/client/${clientId}`,
      farm,
      {
        withCredentials: true
      }
    );
  }

  getClientFarms(
    clientId: number
  ): Observable<Farm[]> {

    return this.http.get<Farm[]>(
      `${this.apiUrl}/client/${clientId}`,
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