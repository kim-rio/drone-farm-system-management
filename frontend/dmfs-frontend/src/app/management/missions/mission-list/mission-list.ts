import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  Mission,
  MissionService
} from '../../../services/mission.service';

@Component({
  selector: 'app-mission-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mission-list.html',
  styleUrl: './mission-list.scss'
})
export class MissionList implements OnInit {

  private readonly missionService =
    inject(MissionService);

  private readonly router =
    inject(Router);

  private readonly cdr =
    inject(ChangeDetectorRef);


  missions: Mission[] = [];

  loading = true;

  errorMessage = '';


  ngOnInit(): void {
    this.loadMissions();
  }


  loadMissions(): void {

    this.loading = true;
    this.errorMessage = '';

    this.missionService
      .getMissions()
      .subscribe({

        next: missions => {

          this.missions = missions;

          this.loading = false;

          this.cdr.detectChanges();
        },

        error: error => {

          console.error(
            'MISSION LOAD ERROR:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'Unable to load missions.';

          this.cdr.detectChanges();
        }
      });
  }


  createMission(): void {

    this.router.navigate([
      '/management/missions/new'
    ]);
  }


  viewMission(
    mission: Mission
  ): void {

    this.router.navigate([
      '/management/missions',
      mission.id
    ]);
  }


  getOperatorName(
    mission: Mission
  ): string {

    if (!mission.operator) {
      return 'Not assigned';
    }

    return `${mission.operator.firstName} ${mission.operator.lastName}`;
  }


  getStatusClass(
    status: string
  ): string {

    return status
      .toLowerCase()
      .replace(/_/g, '-');
  }
}