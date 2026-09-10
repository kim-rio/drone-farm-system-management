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

  private readonly auth =
    inject(AuthService);

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly cdr =
    inject(ChangeDetectorRef);


  /* =========================================================
     PAGE
     ========================================================= */

  page = 'dashboard';


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
    action:
      'accept'
      | 'start'
      | 'complete',

    mission: Mission
  ): void {

    if (this.busy) {
      return;
    }

    this.busy = true;

    const call =
      action === 'accept'
        ? this.api.accept(mission.id)
        : action === 'start'
          ? this.api.startMission(mission.id)
          : this.api.completeMission(mission.id);


    call.subscribe({

      next: () => {

        this.busy = false;

        this.load(
          mission.id
        );
      },

      error: (error) => {

        console.error(
          'MISSION ACTION ERROR:',
          error
        );

        this.busy = false;

        this.error =
          error?.error?.message
          ||
          'The mission could not be updated. Please try again.';

        this.cdr.detectChanges();
      }

    });
  }


  /* =========================================================
     CREATE / OPEN SURVEY
     ========================================================= */

  launchSurvey(
    mission: Mission
  ): void {

    const survey =
      this.surveyFor(mission);

    if (survey) {

      this.router.navigate([
        '/drone-operator/surveys'
      ]);

      return;
    }


    if (!mission.serviceRequest?.id) {

      this.error =
        'This mission is missing its service request.';

      return;
    }


    if (this.busy) {
      return;
    }


    this.busy = true;


    const now =
      new Date().toISOString();


    this.api
      .createSurvey({

        serviceRequestId:
          mission.serviceRequest.id,

        operatorId:
          this.user?.userId,

        surveyCode:
          `SRV-${mission.missionCode.replace(
            'MIS-',
            ''
          )}`,

        surveyName:
          `${mission.missionCode} field survey`,

        startedAt:
          now,

        startLatitude:
          mission.farmBlock?.centerLatitude,

        startLongitude:
          mission.farmBlock?.centerLongitude

      })
      .subscribe({

        next: () => {

          this.busy = false;

          this.load(
            mission.id
          );
        },

        error: (error) => {

          console.error(
            'CREATE SURVEY ERROR:',
            error
          );

          this.busy = false;

          this.error =
            error?.error?.message
            ||
            'Unable to create the survey.';

          this.cdr.detectChanges();
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