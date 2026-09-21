import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Mission, OperatorService, Survey, SurveyDataPackage } from '../../operator.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-survey-data',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './survey-data.html',
  styleUrl: './survey-data.scss'
})
export class SurveyData implements OnInit {
  private readonly api = inject(OperatorService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  survey?: Survey;
  mission?: Mission;

  sidebarOpen = true;

  menuItems = [
    { label: 'Dashboard', route: '/drone-operator' },
    { label: 'My Missions', route: '/drone-operator/missions' },
    { label: 'Field Surveys', route: '/drone-operator/surveys' }
  ];
  user = this.auth.getCurrentUser();

  loading = true;
  uploading = false;
  submitting = false;
  error = '';
  success = '';

  selectedFiles: File[] = [];
  package?: SurveyDataPackage;
  expandedFileId?: number;

  readonly acceptedExtensions = ['uav', 'xyz', 'kml', 'bna'];

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id) {
      this.error = 'Invalid survey.';
      this.loading = false;
      return;
    }

    this.api.surveys().subscribe({
      next: surveys => {
        this.survey = surveys.find(s => Number(s.id) === id);

        if (!this.survey) {
          this.error = 'Survey not found or you do not have access to it.';
        } else if (this.survey.status !== 'COMPLETED') {
          this.error = 'Survey data can only be uploaded after the survey is completed.';
        } else {
          this.loadMissionForSurvey();
        }

        this.loading = false;
        this.cdr.detectChanges();
      },
      error: e => {
        this.error = e?.error?.message || 'Unable to load the survey.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private loadMissionForSurvey(): void {
    if (!this.survey?.serviceRequestId) {
      return;
    }

    this.api.missions().subscribe({
      next: missions => {
        this.mission = missions.find(
          mission => mission.serviceRequest?.id === this.survey?.serviceRequestId
        );
        this.cdr.detectChanges();
      },
      error: error => {
        console.warn('MISSION STATUS COULD NOT BE LOADED:', error);
      }
    });
  }


  get operatorName(): string {
    return `${this.user?.firstName || ''} ${this.user?.lastName || ''}`.trim() || 'Operator';
  }

  get initials(): string {
    return `${this.user?.firstName?.[0] || 'O'}${this.user?.lastName?.[0] || ''}`.toUpperCase();
  }

  get statusLabel(): string {
    return this.survey?.status?.replace(/_/g, ' ') || 'UNKNOWN';
  }

  get totalSize(): number {
    return this.selectedFiles.reduce((sum, file) => sum + file.size, 0);
  }

  get totalSizeLabel(): string {
    return this.formatSize(this.totalSize);
  }

  get hasUav(): boolean {
    return this.selectedFiles.some(file => this.extension(file) === 'uav');
  }

  get canValidate(): boolean {
    return !!this.survey &&
      this.survey.status === 'COMPLETED' &&
      this.hasUav &&
      this.selectedFiles.length > 0 &&
      !this.uploading &&
      !this.submitting;
  }

  get packageIsValid(): boolean {
    return this.package?.status === 'VALID';
  }

  get packageIsSubmitted(): boolean {
    return this.package?.status === 'SUBMITTED';
  }

  get packageHasInvalidFiles(): boolean {
    return this.package?.files?.some(file => file.validationStatus === 'INVALID') ?? false;
  }

  get packageHasUav(): boolean {
    return this.package?.files?.some(file => file.fileType.toUpperCase() === 'UAV') ?? false;
  }

  get packageHasOptionalFiles(): boolean {
    return (this.package?.files?.length ?? 0) > 1;
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.addFiles(Array.from(input.files || []));
    input.value = '';
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.addFiles(Array.from(event.dataTransfer?.files || []));
  }

  allowDrop(event: DragEvent): void {
    event.preventDefault();
  }

  private addFiles(files: File[]): void {
    this.error = '';
    this.success = '';

    for (const file of files) {
      const ext = this.extension(file);

      if (!this.acceptedExtensions.includes(ext)) {
        this.error = `Unsupported file: ${file.name}. Accepted formats are UAV, XYZ, KML and BNA.`;
        continue;
      }

      if (this.selectedFiles.some(existing => this.extension(existing) === ext)) {
        this.error = `Only one ${ext.toUpperCase()} file can be included in a package.`;
        continue;
      }

      this.selectedFiles.push(file);
    }

    this.cdr.detectChanges();
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.error = '';
  }

  resetPackage(): void {
    this.package = undefined;
    this.selectedFiles = [];
    this.expandedFileId = undefined;
    this.error = '';
    this.success = '';
  }

  toggleFileDetails(fileId: number): void {
    this.expandedFileId = this.expandedFileId === fileId ? undefined : fileId;
  }

  extension(file: File): string {
    return file.name.split('.').pop()?.toLowerCase() || '';
  }

  typeLabel(file: File): string {
    return this.extension(file).toUpperCase();
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
    if (bytes < 1073741824) return `${(bytes / 1048576).toFixed(2)} MB`;
    return `${(bytes / 1073741824).toFixed(2)} GB`;
  }

  uploadPackage(): void {
    if (!this.canValidate || !this.survey) return;

    this.uploading = true;
    this.error = '';
    this.success = '';

    this.api.uploadSurveyDataPackage(this.survey.id, this.selectedFiles).subscribe({
      next: result => {
        this.package = result;
        this.uploading = false;
        this.success = result.status === 'VALID'
          ? 'Survey data package created and validated successfully.'
          : 'Survey data package was created. Review the validation result below.';
        this.cdr.detectChanges();
      },
      error: e => {
        this.error = e?.error?.message || 'The survey data package could not be validated.';
        this.uploading = false;
        this.cdr.detectChanges();
      }
    });
  }

  submitPackage(): void {
    if (!this.package || this.package.status !== 'VALID' || this.submitting) return;

    this.submitting = true;
    this.error = '';
    this.success = '';

    this.api.submitSurveyDataPackage(this.package.id).subscribe({
      next: result => {
        this.package = result;
        this.submitting = false;
        this.loadMissionForSurvey();
        this.success = result.status === 'SUBMITTED'
          ? 'Survey data package submitted successfully.'
          : 'The package response was received. Review its current status below.';
        this.cdr.detectChanges();
      },
      error: e => {
        this.error = e?.error?.message || 'The survey data package could not be submitted.';
        this.submitting = false;
        this.cdr.detectChanges();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/drone-operator/surveys', this.survey?.id]);
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  navigate(route: string): void {
    this.router.navigateByUrl(route);
  }

  isActive(route: string): boolean {
    if (route === '/drone-operator') {
      return this.router.url === '/drone-operator' || this.router.url === '/drone-operator/';
    }
    return this.router.url.startsWith(route);
  }


  logout(): void {
    this.auth.logout().subscribe({
      complete: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }
}
