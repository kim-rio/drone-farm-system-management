import {
  Component,
  OnInit,
  inject,
  signal,
  computed
} from '@angular/core';

import { Router } from '@angular/router';

import {
  StaffMember,
  StaffService
} from './staff/staff.service';

import {
  Company,
  CompanyService
} from './company/company.service';

import {
  ServiceRequest,
  ServiceRequestService
} from '../services/service-request.service';

interface Stat {
  title: string;
  value: number;
  description: string;
}

@Component({
  selector: 'app-admin',
  standalone: true,
  templateUrl: './admin.html',
  styleUrl: './admin.scss'
})
export class Admin implements OnInit {

  private readonly router = inject(Router);
  private readonly staffService = inject(StaffService);
  private readonly companyService = inject(CompanyService);
  private readonly serviceRequestService =
    inject(ServiceRequestService);

  dashboardStats = signal<Stat[]>([
    {
      title: 'Total Staff',
      value: 0,
      description: 'Staff members in your company'
    },
    {
      title: 'Active Staff',
      value: 0,
      description: 'Currently active staff'
    },
    {
      title: 'Pending Work',
      value: 0,
      description: 'Work requiring attention'
    },
    {
      title: 'Active Operations',
      value: 0,
      description: 'Operations currently active'
    }
  ]);

  company = signal<Company | null>(null);

  serviceRequests = signal<ServiceRequest[]>([]);

  errorMessage = signal('');

  inactiveStaff = computed(() => {

    const stats = this.dashboardStats();

    return Math.max(
      stats[0].value - stats[1].value,
      0
    );
  });

  totalOperations = computed(() => {

    const stats = this.dashboardStats();

    return (
      stats[2].value +
      stats[3].value
    );
  });

  activeStaffProgress = computed(() => {

    const stats = this.dashboardStats();

    const total = stats[0].value;

    if (total <= 0) {
      return 0;
    }

    return Math.round(
      (
        stats[1].value /
        total
      ) * 100
    );
  });

  operationProgress = computed(() => {

    const total = this.totalOperations();

    if (total <= 0) {
      return 0;
    }

    return Math.round(
      (
        this.dashboardStats()[3].value /
        total
      ) * 100
    );
  });

  ngOnInit(): void {
    this.loadStaff();
    this.loadCompany();
    this.loadServiceRequests();
  }

  private loadStaff(): void {

    this.staffService.getStaff().subscribe({

      next: (staff: StaffMember[]) => {

        console.log(
          'ADMIN DASHBOARD STAFF:',
          staff
        );

        const currentStats =
          this.dashboardStats();

        this.dashboardStats.set([
          {
            ...currentStats[0],
            value: staff.length
          },
          {
            ...currentStats[1],
            value: staff.filter(
              member => member.active
            ).length
          },
          currentStats[2],
          currentStats[3]
        ]);

        console.log(
          'STAFF STATS:',
          staff.length,
          staff.filter(
            member => member.active
          ).length
        );
      },

      error: (error) => {

        console.error(
          'Unable to load staff:',
          error
        );

        this.errorMessage.set(
          'Unable to load staff information.'
        );
      }

    });
  }

  private loadCompany(): void {

    this.companyService.getCompany().subscribe({

      next: (company: Company) => {

        console.log(
          'ADMIN DASHBOARD COMPANY:',
          company
        );

        this.company.set(company);
      },

      error: (error) => {

        console.error(
          'Unable to load company:',
          error
        );

        if (!this.errorMessage()) {
          this.errorMessage.set(
            'Unable to load company information.'
          );
        }
      }

    });
  }

  private loadServiceRequests(): void {

    this.serviceRequestService
      .getRequests()
      .subscribe({

        next: (requests: ServiceRequest[]) => {

          console.log(
            'ADMIN DASHBOARD REQUESTS:',
            requests
          );

          this.serviceRequests.set(requests);

          this.updateOperationStats(requests);
        },

        error: (error) => {

          console.error(
            'Unable to load service requests:',
            error
          );

          if (!this.errorMessage()) {
            this.errorMessage.set(
              'Unable to load operational information.'
            );
          }
        }

      });
  }

  private updateOperationStats(
    requests: ServiceRequest[]
  ): void {

    const currentStats =
      this.dashboardStats();

    this.dashboardStats.set([
      currentStats[0],
      currentStats[1],
      {
        ...currentStats[2],
        value: requests.filter(
          request =>
            request.status?.toUpperCase() === 'PENDING'
        ).length
      },
      {
        ...currentStats[3],
        value: requests.filter(
          request =>
            request.status?.toUpperCase() === 'ACTIVE'
        ).length
      }
    ]);

    console.log(
      'OPERATION STATS:',
      requests.filter(
        request =>
          request.status?.toUpperCase() === 'PENDING'
      ).length,
      requests.filter(
        request =>
          request.status?.toUpperCase() === 'ACTIVE'
      ).length
    );
  }

  getProgressOffset(
    progress: number
  ): number {

    const circumference = 301.59;

    const safeProgress =
      Math.min(
        Math.max(progress, 0),
        100
      );

    return circumference -
      (
        circumference *
        safeProgress /
        100
      );
  }

  manageStaff(): void {
    this.router.navigate([
      '/admin/staff'
    ]);
  }

  openCompany(): void {
    this.router.navigate([
      '/admin/company'
    ]);
  }

  openOperations(): void {
    this.router.navigate([
      '/admin/operations'
    ]);
  }

  openReports(): void {
    this.router.navigate([
      '/admin/reports'
    ]);
  }
}
