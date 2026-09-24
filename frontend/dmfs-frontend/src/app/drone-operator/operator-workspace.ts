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
  catchError,
  finalize,
  of,
  timeout
} from 'rxjs';

import {
  CompanyBrandingResponse,
  CompanyBrandingService
} from '../services/company-branding.service';

import { AuthService } from '../services/auth.service';

import {
  Mission,
  OperatorService,
  Survey
} from './operator.service';


@Component({
  selector: 'app-operator-workspace',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    DatePipe
  ],
  templateUrl: './operator-workspace.html',
  styleUrl: './operator-workspace.scss'
})
export class OperatorWorkspace implements OnInit {

  private readonly api =
    inject(OperatorService);

  private readonly brandingService =
    inject(CompanyBrandingService);

  branding: CompanyBrandingResponse = {
    companyId: 0,
    companyName: '',
    logoUrl: null
  };

  private readonly brandingLoad =
    this.brandingService.getBranding().subscribe({
      next: branding => {
        this.branding = branding;
      },
      error: error => {
        console.warn('COMPANY BRANDING LOAD ERROR:', error);
      }
    });

  private readonly auth =
    inject(AuthService);

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly cdr =
    inject(ChangeDetectorRef);


  /* =========================================================
     PAGE / NAVIGATION
     ========================================================= */

  page = 'dashboard';

  sidebarOpen = true;

  menuItems = [
    { label: 'Dashboard', route: '/drone-operator' },
    { label: 'My Missions', route: '/drone-operator/missions' },
    { label: 'Field Surveys', route: '/drone-operator/surveys' },
    { label: 'Agricultural Reports', route: '/drone-operator/agriculture-reports' }
  ];


  /* =========================================================
     DATA
     ========================================================= */

  missions: Mission[] = [];

  surveys: Survey[] = [];

  selected?: Mission;


  /* =========================================================
     LOADING
     ========================================================= */

  missionsLoading = false;

  surveysLoading = false;


  /* =========================================================
     ACTION STATE
     ========================================================= */

  busy = false;

  error = '';


  /* =========================================================
     USER
     ========================================================= */

  user =
    this.auth.getCurrentUser();


  /* =========================================================
     INIT
     ========================================================= */

  ngOnInit(): void {

    this.page =
      this.route.snapshot.data['page']
      || 'dashboard';

    const id =
      Number(
        this.route.snapshot.paramMap.get('id')
      );

    this.load(
      id || undefined
    );
  }


  /* =========================================================
     LOAD WORKSPACE
     ========================================================= */

  load(id?: number): void {

    this.error = '';

    this.missionsLoading = true;

    this.api
      .missions()
      .pipe(

        timeout(15000),

        finalize(() => {

          this.missionsLoading = false;

          /*
           * Force the view to update immediately after
           * the HTTP request finishes.
           */
          this.cdr.detectChanges();

        })

      )
      .subscribe({

        next: (missions) => {

          this.missions =
            missions || [];

          this.selected =
            id
              ? this.missions.find(
                  mission =>
                    mission.id === id
                )
              : undefined;

          this.cdr.detectChanges();

          /*
           * Surveys are completely independent from
           * mission loading.
           */
          this.loadSurveys();
        },

        error: (error) => {

          console.error(
            'LOAD MISSIONS ERROR:',
            error
          );

          this.missions = [];

          this.selected = undefined;

          this.error =
            error?.error?.message
            ||
            'Unable to load your assigned missions. Please sign in again and retry.';

          this.cdr.detectChanges();
        }

      });
  }


  /* =========================================================
     LOAD SURVEYS
     ========================================================= */

  private loadSurveys(): void {

    this.surveysLoading = true;

    this.api
      .surveys()
      .pipe(

        timeout(10000),

        catchError(error => {

          /*
           * Survey failure must NEVER break
           * the operator mission workspace.
           */

          console.warn(
            'SURVEYS COULD NOT BE LOADED:',
            error
          );

          this.surveys = [];

          return of<Survey[]>([]);

        }),

        finalize(() => {

          this.surveysLoading = false;

          this.cdr.detectChanges();

        })

      )
      .subscribe({

        next: (surveys) => {

          this.surveys =
            surveys || [];

          this.cdr.detectChanges();

        }

      });
  }


  /* =========================================================
     USER DISPLAY
     ========================================================= */

  get name(): string {

    return this.user?.firstName
      || 'Operator';
  }


  get initials(): string {

    return (
      `${this.user?.firstName?.[0] || 'O'}`
      +
      `${this.user?.lastName?.[0] || ''}`
    ).toUpperCase();
  }


  /* =========================================================
     MISSION FILTERS
     ========================================================= */

  get pending(): Mission[] {

    return this.missions.filter(
      mission =>
        mission.status === 'ASSIGNED'
    );
  }


  get today(): Mission[] {

    const date =
      new Date()
        .toISOString()
        .slice(0, 10);

    return this.missions.filter(
      mission =>
        mission.scheduledDate === date
    );
  }


  get active(): Mission[] {

    return this.missions.filter(
      mission =>
        mission.status === 'IN_PROGRESS'
    );
  }


  get completed(): Mission[] {

    return this.missions.filter(
      mission =>
        mission.status === 'COMPLETED'
    );
  }


  /* =========================================================
     DISPLAY HELPERS
     ========================================================= */

  statusLabel(
    status: string
  ): string {

    return status
      .replace(/_/g, ' ');
  }


  surveyFor(
    mission: Mission
  ): Survey | undefined {

    return this.surveys.find(
      survey =>
        survey.serviceRequestId
        === mission.serviceRequest?.id
    );
  }


  /* =========================================================
     MISSION ACTION
     ========================================================= */

  action(
  action: 'accept' | 'start' | 'complete',
  mission: Mission
): void {

  if (this.busy) {
    return;
  }

  this.busy = true;
  this.error = '';

  if (action === 'start') {

    this.api.startMission(mission.id).subscribe({

      next: updatedMission => {

        this.busy = false;

        // Agriculture missions use their own field application report workflow.
        if (updatedMission.category === 'AGRICULTURE') {
          this.router.navigate(['/drone-operator/agriculture-reports/mission', updatedMission.id]);
        } else {
          // Mining missions retain the existing survey/data architecture.
          this.launchSurvey(updatedMission);
        }
      },

      error: error => {

        this.error =
          error?.error?.message ||
          'The mission could not be started.';

        this.busy = false;
      }

    });

    return;
  }

  const call =
    action === 'accept'
      ? this.api.accept(mission.id)
      : this.api.completeMission(mission.id);

  call.subscribe({

    next: () => {

      this.busy = false;
      this.load(mission.id);
    },

    error: error => {

      this.error =
        error?.error?.message ||
        'The mission could not be updated.';

      this.busy = false;
    }

  });
}

  /* =========================================================
     CREATE / OPEN SURVEY
     ========================================================= */

  launchSurvey(mission: Mission): void {

  const existingSurvey = this.surveyFor(mission);

  /*
   * Survey already exists.
   * Open it instead of creating another one.
   */
  if (existingSurvey) {

    this.router.navigate([
      '/drone-operator/surveys',
      existingSurvey.id
    ]);

    return;
  }

  /*
   * No survey exists yet.
   * Create the field survey for this mission.
   */

  if (!mission.serviceRequest?.id) {

    this.error =
      'This mission does not have a service request attached to it.';

    return;
  }

  this.busy = true;
  this.error = '';

  const now = new Date().toISOString();

  this.api.createSurvey({

    serviceRequestId:
      mission.serviceRequest.id,

    operatorId:
      this.user?.userId,

    surveyCode:
      `SRV-${mission.missionCode.replace('MIS-', '')}`,

    surveyName:
      `${mission.missionCode} field survey`,

    startedAt:
      now,

    startLatitude:
      mission.farmBlock?.centerLatitude,

    startLongitude:
      mission.farmBlock?.centerLongitude

  }).subscribe({

    next: survey => {

      this.busy = false;

      /*
       * Backend created the survey.
       * Navigate directly to the new survey.
       */
      this.router.navigate([
        '/drone-operator/surveys',
        survey.id
      ]);

    },

    error: error => {

      this.error =
        error?.error?.message ||
        'Unable to create the field survey.';

      this.busy = false;
    }

  });
}


  /* =========================================================
     SURVEY ACTION
     ========================================================= */

  surveyAction(
    action:
      'start'
      | 'complete',

    survey: Survey
  ): void {

    if (this.busy) {
      return;
    }

    this.busy = true;


    const call =
      action === 'start'
        ? this.api.startSurvey(survey.id)
        : this.api.completeSurvey(survey.id);


    call.subscribe({

      next: () => {

        this.busy = false;

        this.load();

      },

      error: (error) => {

        console.error(
          'SURVEY ACTION ERROR:',
          error
        );

        this.busy = false;

        this.error =
          error?.error?.message
          ||
          'The survey could not be updated.';

        this.cdr.detectChanges();
      }

    });
  }


  openAgricultureReport(missionId: number): void {
    this.router.navigate(['/drone-operator/agriculture-reports/mission', missionId]);
  }

  openAgricultureReports(): void {
    this.router.navigate(['/drone-operator/agriculture-reports']);
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

    this.auth
      .logout()
      .subscribe({

        complete: () => {

          this.router.navigate([
            '/login'
          ]);

        },

        error: () => {

          this.router.navigate([
            '/login'
          ]);

        }

      });
  }

}