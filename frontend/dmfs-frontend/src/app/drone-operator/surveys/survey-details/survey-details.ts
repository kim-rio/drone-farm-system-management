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
  Survey,
  SurveyData
} from '../../operator.service';

import { AuthService } from '../../../services/auth.service';

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
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  survey?: Survey;

  loading = true;
  busy = false;
  error = '';

  /* =========================================================
     UPLOADED SURVEY DATA
     ========================================================= */

  uploadedData: SurveyData[] = [];
  loadingData = false;

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

        /*
         * Once the survey is found, load any
         * data files already uploaded for it.
         */
        this.loadUploadedData(id);

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
     LOAD UPLOADED DATA FILES
     ========================================================= */

  private loadUploadedData(surveyId: number): void {

    this.loadingData = true;

    this.api.surveyData(surveyId).subscribe({

      next: data => {

        this.uploadedData = data;
        this.loadingData = false;

        this.cdr.detectChanges();
      },

      error: error => {

        console.error(
          'LOAD UPLOADED SURVEY DATA ERROR:',
          error
        );

        /*
         * If there is no data or the request fails,
         * keep the list empty.
         */
        this.uploadedData = [];
        this.loadingData = false;

        this.cdr.detectChanges();
      }

    });
  }


  /* =========================================================
     CHECK WHETHER DATA HAS BEEN UPLOADED
     ========================================================= */

  get hasUploadedData(): boolean {

    return this.uploadedData.length > 0;
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

  openDataCapture(): void {

    if (!this.survey) {

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