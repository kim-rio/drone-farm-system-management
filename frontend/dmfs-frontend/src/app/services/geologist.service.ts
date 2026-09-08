import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GeologistDashboard { pendingMapReviews: number; completedSurveys: number; approvedMaps: number; aiReportsReady: number; }
export interface GeologistSurvey { id: number; surveyCode: string; surveyName: string; companyName: string; status: string; startedAt: string; endedAt?: string; }
export interface AnomalyMap { id: number; surveyId: number; surveyCode: string; surveyName: string; companyName: string; mapName: string; filePath: string; anomalyCount: number; decision: string; reviewComment?: string; generatedAt: string; reviewedAt?: string; }
export interface GeologistReport { id: number; surveyId: number; surveyCode: string; reportName: string; filePath: string; fileType?: string; fileSize?: number; generatedAt: string; }

@Injectable({ providedIn: 'root' })
export class GeologistService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/geologist';
  dashboard(): Observable<GeologistDashboard> { return this.http.get<GeologistDashboard>(`${this.apiUrl}/dashboard`, { withCredentials: true }); }
  surveys(): Observable<GeologistSurvey[]> { return this.http.get<GeologistSurvey[]>(`${this.apiUrl}/surveys`, { withCredentials: true }); }
  maps(status?: string): Observable<AnomalyMap[]> { return this.http.get<AnomalyMap[]>(`${this.apiUrl}/maps`, { params: status && status !== 'ALL' ? { status } : {}, withCredentials: true }); }
  review(mapId: number, decision: 'APPROVED' | 'REJECTED' | 'REPROCESSED', comment: string): Observable<AnomalyMap> { return this.http.post<AnomalyMap>(`${this.apiUrl}/maps/${mapId}/review`, { decision, comment }, { withCredentials: true }); }
  reports(): Observable<GeologistReport[]> { return this.http.get<GeologistReport[]>(`${this.apiUrl}/reports`, { withCredentials: true }); }
  mapFileUrl(mapId: number): string { return `${this.apiUrl}/maps/${mapId}/file`; }
}
