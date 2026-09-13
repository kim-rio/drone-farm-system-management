import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import {
  Mission,
  MissionService,
  DroneOperator
} from '../../../services/mission.service';

@Component({
  selector: 'app-mission-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './mission-details.html',
  styleUrl: './mission-details.scss'
})
export class MissionDetails implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly missionService =
    inject(MissionService);

  private readonly cdr =
    inject(ChangeDetectorRef);


  mission: Mission | null = null;

  operators: DroneOperator[] = [];

  selectedOperatorId: number | null = null;

  loading = true;

  assigning = false;

  removing = false;

  errorMessage = '';

  successMessage = '';


  ngOnInit(): void {

    const id =
      Number(
        this.route.snapshot.paramMap.get('id')
      );

    if (!id) {

      this.errorMessage =
        'Invalid mission ID.';

      this.loading = false;

      return;
    }

    this.loadMission(id);
  }


  loadMission(id: number): void {

    this.loading = true;

    this.missionService
      .getMission(id)
      .subscribe({

        next: mission => {

          this.mission = mission;

          this.selectedOperatorId =
            mission.operator?.id ?? null;

          this.loading = false;

          this.loadOperators();

          this.cdr.detectChanges();
        },

        error: error => {

          console.error(
            'MISSION LOAD ERROR:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'Unable to load mission.';

          this.cdr.detectChanges();
        }
      });
  }


  loadOperators(): void {

    this.missionService
      .getDroneOperators()
      .subscribe({

        next: operators => {

          this.operators = operators;

          this.cdr.detectChanges();
        },

        error: error => {

          console.error(
            'OPERATOR LOAD ERROR:',
            error
          );

          this.errorMessage =
            'Unable to load drone operators.';

          this.cdr.detectChanges();
        }
      });
  }


  assignOperator(): void {

    if (
      !this.mission ||
      !this.selectedOperatorId
    ) {
      return;
    }


    this.assigning = true;

    this.errorMessage = '';

    this.successMessage = '';


    this.missionService
      .assignOperator(
        this.mission.id,
        this.selectedOperatorId
      )
      .subscribe({

        next: mission => {

          this.mission = mission;

          this.assigning = false;

          this.successMessage =
            'Drone operator assigned successfully.';

          this.cdr.detectChanges();
        },

        error: error => {

          console.error(
            'ASSIGN OPERATOR ERROR:',
            error
          );

          this.assigning = false;

          this.errorMessage =
            error?.error?.message ||
            'Unable to assign drone operator.';

          this.cdr.detectChanges();
        }
      });
  }


  removeOperator(): void {

    if (!this.mission) {
      return;
    }


    if (
      !window.confirm(
        'Remove the assigned drone operator from this mission?'
      )
    ) {
      return;
    }


    this.removing = true;

    this.errorMessage = '';

    this.successMessage = '';


    this.missionService
      .removeOperator(
        this.mission.id
      )
      .subscribe({

        next: mission => {

          this.mission = mission;

          this.selectedOperatorId = null;

          this.removing = false;

          this.successMessage =
            'Drone operator removed successfully.';

          this.cdr.detectChanges();
        },

        error: error => {

          console.error(
            'REMOVE OPERATOR ERROR:',
            error
          );

          this.removing = false;

          this.errorMessage =
            error?.error?.message ||
            'Unable to remove drone operator.';

          this.cdr.detectChanges();
        }
      });
  }


  getOperatorName(): string {

    if (!this.mission?.operator) {
      return 'Not assigned';
    }

    return `${this.mission.operator.firstName} ${this.mission.operator.lastName}`;
  }


  back(): void {

    this.router.navigate([
      '/management/missions'
    ]);
  }
}