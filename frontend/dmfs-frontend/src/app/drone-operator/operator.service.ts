import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export type MissionStatus = 'PLANNED' | 'ASSIGNED' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Mission {
  id: number; missionCode: string; status: MissionStatus; scheduledDate: string; notes?: string;
  customer?: { clientCode?: string }; farm?: { name?: string; areaHectares?: number };
  farmBlock?: { name?: string; areaHectares?: number; centerLatitude?: number; centerLongitude?: number };
  drone?: { name?: string; model?: string; serialNumber?: string; status?: string };
  serviceRequest?: { id: number };
}

export interface Survey {
  id: number; surveyCode: string; surveyName?: string; serviceRequestId: number; status: 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'FAILED';
  startedAt?: string; endedAt?: string; startLatitude?: number; startLongitude?: number; endLatitude?: number; endLongitude?: number;
}

@Injectable({ providedIn: 'root' })
export class OperatorService {
  private http = inject(HttpClient);
  private api = 'http://localhost:8080/api';
  private options = { withCredentials: true };

  missions() { return this.http.get<Mission[]>(`${this.api}/missions/my`, this.options); }
  mission(id: number) { return this.http.get<Mission>(`${this.api}/missions/${id}`, this.options); }
  accept(id: number) { return this.http.patch<Mission>(`${this.api}/missions/${id}/accept`, {}, this.options); }
  startMission(id: number) { return this.http.patch<Mission>(`${this.api}/missions/${id}/start`, {}, this.options); }
  completeMission(id: number) { return this.http.patch<Mission>(`${this.api}/missions/${id}/complete`, {}, this.options); }
  surveys() { return this.http.get<Survey[]>(`${this.api}/surveys/my`, this.options); }
  createSurvey(payload: object) { return this.http.post<Survey>(`${this.api}/surveys`, payload, this.options); }
  startSurvey(id: number) { return this.http.patch<Survey>(`${this.api}/surveys/${id}/start`, {}, this.options); }
  completeSurvey(id: number) { return this.http.patch<Survey>(`${this.api}/surveys/${id}/complete`, {}, this.options); }
}
