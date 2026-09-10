import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Mission {
  id: number;
  missionCode: string;
  status: string;
  scheduledDate: string;
  notes?: string | null;
  createdAt?: string;
  updatedAt?: string;

  serviceRequest?: {
    id: number;
    requestedDate?: string;
    status?: string;
  };

  customer?: {
    id: number;
    clientCode?: string;
    type?: string;
    status?: string;
  };

  farm?: {
    id: number;
    name: string;
    areaHectares?: number;
  };

  farmBlock?: {
    id: number;
    name: string;
    areaHectares?: number;
    centerLatitude?: number;
    centerLongitude?: number;
  };

  operator?: {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
  };

  drone?: {
    id: number;
    name: string;
    serialNumber: string;
    model: string;
    status?: string;
  };
}

export interface DroneOperator {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface CreateMissionPayload {
  serviceRequestId: number;
  operatorId: number;
  scheduledDate: string;
  notes?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MissionService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/missions';

  private readonly options = { withCredentials: true };


  getMissions(): Observable<Mission[]> {
    return this.http.get<Mission[]>(
      this.apiUrl, this.options
    );
  }


  getMyMissions(): Observable<Mission[]> {
    return this.http.get<Mission[]>(
      `${this.apiUrl}/my`, this.options
    );
  }


  getMission(id: number): Observable<Mission> {
    return this.http.get<Mission>(
      `${this.apiUrl}/${id}`, this.options
    );
  }


  createMission(
    payload: CreateMissionPayload
  ): Observable<Mission> {

    return this.http.post<Mission>(
      this.apiUrl,
      payload, this.options
    );
  }


  getDroneOperators(): Observable<DroneOperator[]> {

    return this.http.get<DroneOperator[]>(
      `${this.apiUrl}/operators`, this.options
    );
  }


  assignOperator(
    missionId: number,
    operatorId: number
  ): Observable<Mission> {

    return this.http.patch<Mission>(
      `${this.apiUrl}/${missionId}/operator/${operatorId}`,
      {}, this.options
    );
  }


  removeOperator(
    missionId: number
  ): Observable<Mission> {

    return this.http.delete<Mission>(
      `${this.apiUrl}/${missionId}/operator`, this.options
    );
  }


  acceptMission(
    missionId: number
  ): Observable<Mission> {

    return this.http.patch<Mission>(
      `${this.apiUrl}/${missionId}/accept`,
      {}, this.options
    );
  }


  startMission(
    missionId: number
  ): Observable<Mission> {

    return this.http.patch<Mission>(
      `${this.apiUrl}/${missionId}/start`,
      {}, this.options
    );
  }


  completeMission(
    missionId: number
  ): Observable<Mission> {

    return this.http.patch<Mission>(
      `${this.apiUrl}/${missionId}/complete`,
      {}, this.options
    );
  }


  cancelMission(
    missionId: number
  ): Observable<Mission> {

    return this.http.patch<Mission>(
      `${this.apiUrl}/${missionId}/cancel`,
      {}, this.options
    );
  }
}
