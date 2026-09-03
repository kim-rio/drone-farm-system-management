import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type DroneStatus =
  | 'AVAILABLE'
  | 'ASSIGNED'
  | 'IN_FLIGHT'
  | 'DAMAGED'
  | 'RETIRED';

export interface Drone {
  id: number;
  name: string;
  serialNumber: string;
  model?: string;
  manufacturer?: string;
  droneType: string;
  status: DroneStatus;
  purchaseDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface DroneRequest {
  name: string;
  serialNumber: string;
  model?: string;
  manufacturer?: string;
  droneType: string;
  purchaseDate?: string;
}

export interface DroneStatusRequest {
  status: DroneStatus;
}

@Injectable({
  providedIn: 'root'
})
export class DroneService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/drones';


  getDrones(): Observable<Drone[]> {

    return this.http.get<Drone[]>(
      this.apiUrl,
      {
        withCredentials: true
      }
    );
  }


  getDrone(id: number): Observable<Drone> {

    return this.http.get<Drone>(
      `${this.apiUrl}/${id}`,
      {
        withCredentials: true
      }
    );
  }


  createDrone(
    drone: DroneRequest
  ): Observable<Drone> {

    return this.http.post<Drone>(
      this.apiUrl,
      drone,
      {
        withCredentials: true
      }
    );
  }


  updateDrone(
    id: number,
    drone: DroneRequest
  ): Observable<Drone> {

    return this.http.put<Drone>(
      `${this.apiUrl}/${id}`,
      drone,
      {
        withCredentials: true
      }
    );
  }


  updateDroneStatus(
    id: number,
    status: DroneStatus
  ): Observable<Drone> {

    return this.http.put<Drone>(
      `${this.apiUrl}/${id}/status`,
      {
        status: status
      },
      {
        withCredentials: true
      }
    );
  }


  deleteDrone(id: number): Observable<void> {

    return this.http.delete<void>(
      `${this.apiUrl}/${id}`,
      {
        withCredentials: true
      }
    );
  }

}