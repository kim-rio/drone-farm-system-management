import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export type MissionStatus = 'PLANNED' | 'ASSIGNED' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Mission {
  id: number; missionCode: string; status: MissionStatus; category: 'MINING' | 'AGRICULTURE'; scheduledDate: string; notes?: string;
  customer?: { clientCode?: string }; farm?: { name?: string; areaHectares?: number };
  farmBlock?: { name?: string; areaHectares?: number; centerLatitude?: number; centerLongitude?: number };
  drone?: { name?: string; model?: string; serialNumber?: string; status?: string };
  serviceRequest?: { id: number };
}


export interface AgricultureReport {
  id?: number; missionId: number; missionCode: string; category?: string; reportCode?: string;
  farmId?: number; farmName?: string; farmAreaHa?: number; farmBlockId?: number; farmBlockName?: string; blockAreaHa?: number;
  operatorId?: number; operatorName?: string; operatorLicense?: string;
  applicationDate?: string; nextScoutDate?: string;
  cropType?: string; growthStage?: string; totalAreaTreatedHa?: number; targetProblem?: string;
  applicationStartTime?: string; applicationEndTime?: string; temperatureC?: number; windSpeedKmh?: number;
  windDirection?: string; relativeHumidity?: number; skyConditions?: string; rainForecast?: string;
  tradeName?: string; registrationNumber?: string; activeIngredient?: string; totalProductUsedL?: number;
  totalWaterVolumeL?: number; productRateLHa?: number; waterRateLHa?: number; adjuvants?: string;
  equipmentUsed?: string; nozzleType?: string; dropletSizeMicrons?: number;
  preHarvestIntervalDays?: number; restrictedEntryIntervalHours?: number; bufferZoneNotes?: string;
  coverageQuality?: string; coverageObservations?: string; incidentsNotes?: string; applicatorSignature?: string;
  photo1Url?: string; photo2Url?: string; finalized: boolean; createdAt?: string; updatedAt?: string;
}

export interface Survey {
  id: number; surveyCode: string; surveyName?: string; serviceRequestId: number; status: 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'FAILED';
  startedAt?: string; endedAt?: string; startLatitude?: number; startLongitude?: number; endLatitude?: number; endLongitude?: number;
}
export interface SurveyDataPackageFile {
  id: number;
  fileName: string;
  fileType: string;
  fileExtension: string;
  fileSize: number;
  validationStatus: 'PENDING' | 'VALIDATING' | 'VALID' | 'INVALID';
  validationMessage?: string;
  uploadedAt?: string;
}

export interface SurveyDataPackage {
  id: number;
  surveyId: number;
  packageCode: string;
  status: 'DRAFT' | 'VALIDATING' | 'VALID' | 'SUBMITTED' | 'QUEUED' | 'PROCESSING' | 'PROCESSED' | 'FAILED' | 'PROCESSING_FAILED';
  files: SurveyDataPackageFile[];
  createdAt?: string;
  validatedAt?: string;
  submittedAt?: string;
  submittedBy?: number;
}


@Injectable({ providedIn: 'root' })
export class OperatorService {
  private http = inject(HttpClient);
  private api = '/api';
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
uploadSurveyDataPackage(surveyId: number, files: File[]) {
  const formData = new FormData();

  for (const file of files) {
    formData.append('files', file);
  }

  return this.http.post<SurveyDataPackage>(
    `${this.api}/surveys/${surveyId}/data-packages`,
    formData,
    this.options
  );
}

submitSurveyDataPackage(packageId: number) {
  return this.http.post<SurveyDataPackage>(
    `${this.api}/surveys/data-packages/${packageId}/submit`,
    {},
    this.options
  );
}

getAgricultureReports() {
  return this.http.get<AgricultureReport[]>(`${this.api}/agriculture-reports`, this.options);
}

getAgricultureReportForMission(missionId: number) {
  return this.http.get<AgricultureReport>(`${this.api}/agriculture-reports/mission/${missionId}`, this.options);
}

saveAgricultureReport(missionId: number, data: FormData) {
  return this.http.post<AgricultureReport>(`${this.api}/agriculture-reports/mission/${missionId}`, data, this.options);
}
}
