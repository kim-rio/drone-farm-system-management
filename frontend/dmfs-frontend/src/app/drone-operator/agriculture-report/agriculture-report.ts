import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { OperatorService, AgricultureReport } from '../operator.service';
import { OperatorShell } from '../shared/operator-shell/operator-shell';

@Component({
  selector: 'app-agriculture-report',
  standalone: true,
  imports: [CommonModule, FormsModule, OperatorShell],
  templateUrl: './agriculture-report.html',
  styleUrl: './agriculture-report.scss'
})
export class AgricultureReportPage implements OnInit {
  private readonly api = inject(OperatorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  missionId = 0;
  mission: any = null;
  report: AgricultureReport | null = null;
  loading = true;
  saving = false;
  error = '';
  success = '';

  photo1: File | null = null;
  photo2: File | null = null;
  photo1Name = '';
  photo2Name = '';

  form: any = {
    applicationDate: '',
    operatorLicense: '',
    cropType: '',
    growthStage: '',
    totalAreaTreatedHa: null,
    targetProblem: '',
    applicationStartTime: '',
    applicationEndTime: '',
    temperatureC: null,
    windSpeedKmh: null,
    windDirection: '',
    relativeHumidity: null,
    skyConditions: '',
    rainForecast: '',
    tradeName: '',
    registrationNumber: '',
    activeIngredient: '',
    totalProductUsedL: null,
    totalWaterVolumeL: null,
    productRateLHa: null,
    waterRateLHa: null,
    adjuvants: '',
    equipmentUsed: '',
    nozzleType: '',
    dropletSizeMicrons: null,
    preHarvestIntervalDays: null,
    restrictedEntryIntervalHours: null,
    bufferZoneNotes: '',
    coverageQuality: '',
    coverageObservations: '',
    incidentsNotes: '',
    nextScoutDate: '',
    applicatorSignature: ''
  };

  ngOnInit(): void {
    this.missionId = Number(this.route.snapshot.paramMap.get('missionId'));
    if (!this.missionId) {
      this.error = 'Invalid agriculture mission.';
      this.loading = false;
      return;
    }

    this.api.mission(this.missionId).subscribe({
      next: mission => {
        this.mission = mission;
        if (mission.category !== 'AGRICULTURE') {
          this.error = 'This mission is not an agriculture mission.';
          this.loading = false;
          return;
        }
        this.loadReport();
      },
      error: (e: HttpErrorResponse) => {
        this.error = e?.error?.message || 'Unable to load the mission.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private loadReport(): void {
    this.api.getAgricultureReportForMission(this.missionId).subscribe({
      next: report => {
        this.report = report;
        this.form.applicationDate = report.applicationDate || this.mission?.scheduledDate || '';
        this.form.totalAreaTreatedHa = report.totalAreaTreatedHa ?? report.blockAreaHa ?? null;
        const values: any = report;
        for (const key of Object.keys(this.form)) {
          if (values[key] !== undefined && values[key] !== null) this.form[key] = values[key];
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (e: HttpErrorResponse) => {
        this.error = e?.error?.message || 'Unable to load the agriculture report.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  selectPhoto(event: Event, slot: 1 | 2): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] || null;
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.error = 'Please select an image file.';
      input.value = '';
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.error = 'Each photo must be 10 MB or smaller.';
      input.value = '';
      return;
    }
    this.error = '';
    if (slot === 1) { this.photo1 = file; this.photo1Name = file.name; }
    else { this.photo2 = file; this.photo2Name = file.name; }
  }

  save(finalize = false): void {
    if (this.saving) return;
    this.error = '';
    this.success = '';

    if (!this.form.cropType || !this.form.totalAreaTreatedHa || !this.form.targetProblem) {
      this.error = 'Complete crop type, treated area and target pest/problem before saving.';
      return;
    }

    if (finalize && !this.photo1 && !this.photo2 && !this.report?.photo1Url) {
      this.error = 'Two application/farm photos are required before finalizing.';
      return;
    }
    if (finalize && (!this.photo1 && !this.report?.photo1Url || !this.photo2 && !this.report?.photo2Url)) {
      this.error = 'Please upload both required photos before finalizing the report.';
      return;
    }

    const data = new FormData();
    const payload = { ...this.form, finalizeReport: finalize };
    data.append('report', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
    if (this.photo1) data.append('photo1', this.photo1);
    if (this.photo2) data.append('photo2', this.photo2);

    this.saving = true;
    this.api.saveAgricultureReport(this.missionId, data).subscribe({
      next: report => {
        this.report = report;
        this.saving = false;
        this.success = finalize ? 'Agricultural application report finalized.' : 'Report draft saved.';
        this.cdr.detectChanges();

        if (finalize) {
          this.api.completeMission(this.missionId).subscribe({
            next: () => this.router.navigate(['/drone-operator/agriculture-reports']),
            error: (e: HttpErrorResponse) => {
              this.error = e?.error?.message || 'Report was finalized, but the mission could not be completed.';
              this.cdr.detectChanges();
            }
          });
        }
      },
      error: (e: HttpErrorResponse) => {
        this.saving = false;
        this.error = e?.error?.message || 'Unable to save the agriculture report.';
        this.cdr.detectChanges();
      }
    });
  }

  back(): void { this.router.navigate(['/drone-operator/missions', this.missionId]); }
}
