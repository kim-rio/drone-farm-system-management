import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GeologistDashboard { pendingMapReviews: number; completedSurveys: number; approvedMaps: number; aiReportsReady: number; }
export interface GeologistSurvey { id: number; surveyCode: string; surveyName: string; companyName: string; status: string; startedAt: string; endedAt?: string; }
export interface AnomalyMap { id: number; surveyId: number; surveyCode: string; surveyName: string; companyName: string; mapName: string; filePath: string; anomalyCount: number; decision: string; reviewComment?: string; generatedAt: string; reviewedAt?: string; }
export interface GeologistReport { id: number; surveyId: number; surveyCode: string; reportName: string; filePath: string; fileType?: string; fileSize?: number; generatedAt: string; }

// --- Added for the survey review-data bundle (GET /surveys/{id}/review-data) ---

export interface RawDataFile { fileId: number; fileName: string; fileType?: string; fileExtension?: string; fileSize?: number; validationStatus: string; uploadedAt: string; downloadUrl: string; }
export interface RawDataPackage { packageId: number; packageCode: string; status: string; createdAt: string; submittedAt?: string; files: RawDataFile[]; }
export interface ProcessedDataSummary { id: number; fileName: string; filePath: string; fileType?: string; fileSize?: number; recordCount?: number; processedAt: string; surveyDataId: number; }
export interface SurveyMeta { id: number; surveyCode: string; surveyName: string; description?: string; companyName: string; status: string; startedAt: string; endedAt?: string; }
export interface AnomalyGeoJson { type: 'FeatureCollection'; features: Array<{ type: 'Feature'; geometry: { type: 'Point'; coordinates: [number, number] }; properties: Record<string, unknown>; }>; }
export interface SurveyReviewData { survey: SurveyMeta; map: AnomalyMap | null; anomalies: AnomalyGeoJson; processedData: ProcessedDataSummary | null; rawData: RawDataPackage[]; }

@Injectable({ providedIn: 'root' })
export class GeologistService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/geologist';
  dashboard(): Observable<GeologistDashboard> { return this.http.get<GeologistDashboard>(`${this.apiUrl}/dashboard`, { withCredentials: true }); }
  surveys(): Observable<GeologistSurvey[]> { return this.http.get<GeologistSurvey[]>(`${this.apiUrl}/surveys`, { withCredentials: true }); }
  maps(status?: string): Observable<AnomalyMap[]> { return this.http.get<AnomalyMap[]>(`${this.apiUrl}/maps`, { params: status && status !== 'ALL' ? { status } : {}, withCredentials: true }); }
  review(mapId: number, decision: 'APPROVED' | 'REJECTED' | 'REPROCESSED', comment: string): Observable<AnomalyMap> { return this.http.post<AnomalyMap>(`${this.apiUrl}/maps/${mapId}/review`, { decision, comment }, { withCredentials: true }); }
  reports(): Observable<GeologistReport[]> { return this.http.get<GeologistReport[]>(`${this.apiUrl}/reports`, { withCredentials: true }); }
  mapFileUrl(mapId: number): string { return `${this.apiUrl}/maps/${mapId}/file`; }

  /** Bundled survey + map + anomalies + processed data + raw data, used by the review screen. */
  surveyReviewData(surveyId: number): Observable<SurveyReviewData> { return this.http.get<SurveyReviewData>(`${this.apiUrl}/surveys/${surveyId}/review-data`, { withCredentials: true }); }

  /** Direct, browser-loadable URL to download a raw survey data file. */
  rawDataFileUrl(fileId: number): string { return `${this.apiUrl}/survey-data-files/${fileId}/download`; }
}
