import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import {
  ServiceRequest,
  ServiceRequestService
} from '../../../services/service-request.service';

import {
  MissionService,
  CreateMissionPayload,
  DroneOperator
} from '../../../services/mission.service';

@Component({
  selector: 'app-create-mission',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './create-mission.html',
  styleUrl: './create-mission.scss'
})
export class CreateMission implements OnInit {

  private readonly missionService =
    inject(MissionService);

  private readonly requestService =
    inject(ServiceRequestService);

  private readonly router =
    inject(Router);

  private readonly cdr =
    inject(ChangeDetectorRef);


  requests: ServiceRequest[] = [];

  operators: DroneOperator[] = [];

  selectedRequestId: number | null = null;

  selectedOperatorId: number | null = null;

  scheduledDate = '';

  notes = '';

  loading = true;

  saving = false;

  errorMessage = '';


  ngOnInit(): void {
    this.loadRequests();
    this.loadOperators();
  }

  loadOperators(): void {
    this.missionService.getDroneOperators().subscribe({
      next: operators => {
        this.operators = operators;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Unable to load active drone operators.';
        this.cdr.detectChanges();
      }
    });
  }


  loadRequests(): void {

    this.requestService
      .getRequests()
      .subscribe({

        next: requests => {

          this.requests =
            requests.filter(
              request =>
                request.status !== 'COMPLETED' &&
                request.status !== 'CANCELLED'
            );

          this.loading = false;

          this.cdr.detectChanges();
        },

        error: error => {

          console.error(
            'REQUEST LOAD ERROR:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'Unable to load service requests.';

          this.cdr.detectChanges();
        }
      });
  }


  getSelectedRequest():
    ServiceRequest | undefined {

    return this.requests.find(
      request =>
        request.id === this.selectedRequestId
    );
  }


  getClientName(
    request: ServiceRequest
  ): string {

    const client =
      request.client;

    if (!client) {
      return 'Unknown Client';
    }

    if (client.companyName?.trim()) {
      return client.companyName;
    }

    const name =
      `${client.firstName ?? ''} ${client.lastName ?? ''}`
        .trim();

    return name ||
      client.clientCode ||
      'Unknown Client';
  }


  save(): void {

    if (
      !this.selectedRequestId ||
      !this.selectedOperatorId ||
      !this.scheduledDate
    ) {

      this.errorMessage =
        'Please select a service request, drone operator, and scheduled date.';

      return;
    }


    const payload:
      CreateMissionPayload = {

      serviceRequestId:
        this.selectedRequestId,

      operatorId:
        this.selectedOperatorId,

      scheduledDate:
        this.scheduledDate,

      notes:
        this.notes
    };


    this.saving = true;

    this.errorMessage = '';


    this.missionService
      .createMission(payload)
      .subscribe({

        next: mission => {

          this.router.navigate([
            '/management/missions',
            mission.id
          ]);
        },

        error: error => {

          console.error(
            'CREATE MISSION ERROR:',
            error
          );

          this.saving = false;

          this.errorMessage =
            error?.error?.message ||
            'Unable to create mission.';

          this.cdr.detectChanges();
        }
      });
  }


  cancel(): void {

    this.router.navigate([
      '/management/missions'
    ]);
  }
}
