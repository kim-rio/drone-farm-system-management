import { CommonModule, DatePipe } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';
import {
  ActivatedRoute,
  Router,
  RouterLink
} from '@angular/router';

import {
  OperatorService,
  Survey
} from '../../operator.service';

import { AuthService } from '../../../services/auth.service';
import {
  CompanyBrandingService,
  CompanyBrandingResponse
} from '../../../services/company-branding.service';

@Component({
  selector: 'app-survey-details',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    DatePipe
  ],
  templateUrl: './survey-details.html',
  styleUrl: './survey-details.scss'
})
export class SurveyDetails implements OnInit {

  private readonly api = inject(OperatorService);
  private readonly auth = inject(AuthService);
  private readonly brandingService = inject(CompanyBrandingService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  survey?: Survey;

  sidebarOpen = true;

  branding: CompanyBrandingResponse = {
    companyId: 0,
    companyName: '',
    logoUrl: null
  };

  menuItems = [
    { label: 'Dashboard', route: '/drone-operator' },
    { label: 'My Missions', route: '/drone-operator/missions' },
    { label: 'Field Surveys', route: '/drone-operator/surveys' },
    { label: 'Agricultural Reports', route: '/drone-operator/agriculture-reports' }
  ];

  loading = true;
  busy = false;
  error = '';

  user = this.auth.getCurrentUser();


  /* =========================================================
     INITIALIZATION
     ========================================================= */

  ngOnInit(): void {

    const surveyId = Number(
      this.route.snapshot.paramMap.get('id')
    );

    if (!surveyId) {

      this.error = 'Invalid survey.';
      this.loading = false;

      this.cdr.detectChanges();

      return;
    }

    this.loadSurvey(surveyId);
  }


  /* =========================================================
     LOAD SURVEY
     ========================================================= */

  private loadSurvey(id: number): void {

    this.loading = true;
    this.error = '';

    this.api.surveys().subscribe({

      next: surveys => {

        const survey = surveys.find(
          item => Number(item.id) === id
        );

        if (!survey) {

          this.error =
            'Survey not found or you do not have access to it.';

          this.loading = false;

          this.cdr.detectChanges();

          return;
        }

        this.survey = survey;

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: error => {

        console.error(
          'FIELD SURVEY LOAD ERROR:',
          error
        );

        this.error =
          error?.error?.message ||
          'Unable to load the field survey.';

        this.loading = false;

        this.cdr.detectChanges();
      }

    });
  }


  /* =========================================================
     OPERATOR INFORMATION
     ========================================================= */

  get name(): string {

    return this.user?.firstName || 'Operator';
  }


  get initials(): string {

    return (
      `${this.user?.firstName?.[0] || 'O'}` +
      `${this.user?.lastName?.[0] || ''}`
    ).toUpperCase();
  }


  get operatorName(): string {

    return (
      `${this.user?.firstName || ''} ` +
      `${this.user?.lastName || ''}`
    ).trim() || 'Operator';
  }


  /* =========================================================
     SURVEY STATUS
     ========================================================= */

  get statusLabel(): string {

    if (!this.survey?.status) {

      return 'DRAFT';
    }

    return this.survey.status.replace(/_/g, ' ');
  }


  /* =========================================================
     START SURVEY
     ========================================================= */

  startSurvey(): void {

    if (!this.survey || this.busy) {

      return;
    }

    this.busy = true;
    this.error = '';

    this.api.startSurvey(this.survey.id).subscribe({

      next: updatedSurvey => {

        this.survey = updatedSurvey;
        this.busy = false;

        this.cdr.detectChanges();
      },

      error: error => {

        this.error =
          error?.error?.message ||
          'Unable to start the survey.';

        this.busy = false;

        this.cdr.detectChanges();
      }

    });
  }


  /* =========================================================
     COMPLETE SURVEY
     ========================================================= */

  completeSurvey(): void {

    if (!this.survey || this.busy) {

      return;
    }

    this.busy = true;
    this.error = '';

    this.api.completeSurvey(this.survey.id).subscribe({

      next: updatedSurvey => {

        this.survey = updatedSurvey;
        this.busy = false;

        this.cdr.detectChanges();
      },

      error: error => {

        this.error =
          error?.error?.message ||
          'Unable to complete the survey.';

        this.busy = false;

        this.cdr.detectChanges();
      }

    });
  }


  /* =========================================================
     OPEN DATA CAPTURE
     ========================================================= */

  openDataUpload(): void {

    if (!this.survey || this.survey.status !== 'COMPLETED') {
      return;
    }

    this.router.navigate([
      '/drone-operator/surveys',
      this.survey.id,
      'data'
    ]);
  }


  /* =========================================================
     GO BACK
     ========================================================= */

  goBack(): void {

    this.router.navigate([
      '/drone-operator/missions'
    ]);
  }


  /* =========================================================
     NAVIGATION
     ========================================================= */

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


  /* =========================================================
     LOGOUT
     ========================================================= */

  logout(): void {

    this.auth.logout().subscribe({

      complete: () => {

        this.router.navigate(['/login']);
      },

      error: () => {

        this.router.navigate(['/login']);
      }

    });
  }

}