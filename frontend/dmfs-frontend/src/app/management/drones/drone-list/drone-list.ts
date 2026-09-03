import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { Router } from '@angular/router';

import {
  Drone,
  DroneService
} from '../../../services/drone.service';

@Component({
  selector: 'app-drone-list',
  standalone: true,
  templateUrl: './drone-list.html',
  styleUrl: './drone-list.scss'
})
export class DroneList implements OnInit {

  private readonly droneService = inject(DroneService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  drones: Drone[] = [];

  loading = true;

  errorMessage = '';

  ngOnInit(): void {
    this.loadDrones();
  }

  loadDrones(): void {

    this.loading = true;
    this.errorMessage = '';

    this.droneService.getDrones().subscribe({

      next: (drones: Drone[]) => {

        console.log('DRONES RECEIVED:', drones);

        this.drones = drones;

        this.loading = false;

        console.log('LOADING:', this.loading);
        console.log('DRONE COUNT:', this.drones.length);

        this.cdr.detectChanges();
      },

      error: (error: unknown) => {

        console.error('DRONE LOAD ERROR:', error);

        this.loading = false;

        if (
          typeof error === 'object' &&
          error !== null &&
          'status' in error
        ) {

          const status =
            (error as { status: number }).status;

          if (status === 401) {

            this.errorMessage =
              'Your session has expired. Please log in again.';

          } else if (status === 403) {

            this.errorMessage =
              'You do not have permission to view drones.';

          } else {

            this.errorMessage =
              'Unable to load drones.';
          }

        } else {

          this.errorMessage =
            'Unable to load drones.';
        }

        this.cdr.detectChanges();
      }
    });
  }

  registerDrone(): void {

    this.router.navigate([
      '/management/drones/register'
    ]);
  }

  openDrone(drone: Drone): void {

    if (!drone.id) {
      return;
    }

    this.router.navigate([
      '/management/drones',
      drone.id
    ]);
  }

  getStatusLabel(status: Drone['status']): string {

    return status
      .replace('_', ' ');
  }

}