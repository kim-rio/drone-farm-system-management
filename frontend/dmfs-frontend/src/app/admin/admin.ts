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
    }
  ]);

  company = signal<Company | null>(null);

  errorMessage = signal('');

  inactiveStaff = computed(() => {
    const stats = this.dashboardStats();

    return Math.max(
      stats[0].value - stats[1].value,
      0
    );
  });

  activeStaffProgress = computed(() => {
    const stats = this.dashboardStats();

    const total = stats[0].value;

    if (total <= 0) {
      return 0;
    }

    return Math.round(
      (stats[1].value / total) * 100
    );
  });

  ngOnInit(): void {
    this.loadStaff();
    this.loadCompany();
  }

  private loadStaff(): void {
    this.staffService.getStaff().subscribe({
      next: (staff: StaffMember[]) => {
        this.dashboardStats.set([
          {
            title: 'Total Staff',
            value: staff.length,
            description: 'Staff members in your company'
          },
          {
            title: 'Active Staff',
            value: staff.filter(
              member => member.active
            ).length,
            description: 'Currently active staff'
          }
        ]);
      },

      error: error => {
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
        this.company.set(company);
      },

      error: error => {
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

  getProgressOffset(progress: number): number {
    const circumference = 301.59;

    const safeProgress = Math.min(
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
    this.router.navigate(['/admin/staff']);
  }

  openServices(): void {
    this.router.navigate(['/admin/services']);
  }

  openReports(): void {
    /*
     * Reports are not implemented in the Admin workspace yet.
     * Keep this method until the Reports route is introduced,
     * rather than navigating to the platform root.
     */
    console.info(
      'Admin reports are not implemented yet.'
    );
  }
}
