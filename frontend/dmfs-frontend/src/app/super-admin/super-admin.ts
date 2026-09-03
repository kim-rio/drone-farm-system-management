import { Component, ChangeDetectorRef, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import {
  SuperAdminDashboardResponse,
  SuperAdminService
} from '../services/super-admin.service';

interface MenuItem {
  label: string;
  route: string;
}

interface Stat {
  title: string;
  value: number;
  description: string;
}

@Component({
  selector: 'app-super-admin',
  standalone: true,
  imports: [],
  templateUrl: './super-admin.html',
  styleUrl: './super-admin.scss'
})
export class SuperAdmin implements OnInit {

  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly superAdminService = inject(SuperAdminService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  sidebarOpen = true;

  user = this.authService.getCurrentUser();

  loading = true;
  error = '';

  menuItems: MenuItem[] = [
    {
      label: 'Dashboard',
      route: '/super-admin'
    },
    {
      label: 'Companies',
      route: '/super-admin/companies'
    }
  ];

  dashboardStats: Stat[] = [
    {
      title: 'Subscriber Companies',
      value: 0,
      description: 'Total companies registered'
    },
    {
      title: 'Active Companies',
      value: 0,
      description: 'Companies currently active'
    },
    {
      title: 'Suspended Companies',
      value: 0,
      description: 'Companies currently suspended'
    },
    {
      title: 'Expired Companies',
      value: 0,
      description: 'Companies with expired status'
    }
  ];

  platformUsers = {
    total: 0,
    active: 0,
    inactive: 0
  };

  activeCompanyProgress = 0;
  userActivityProgress = 0;
  companyHealthProgress = 0;

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = '';

    this.superAdminService.getDashboard().subscribe({
      next: (data: SuperAdminDashboardResponse) => {
        this.dashboardStats = [
          {
            title: 'Subscriber Companies',
            value: data.totalCompanies,
            description: 'Total companies registered'
          },
          {
            title: 'Active Companies',
            value: data.activeCompanies,
            description: 'Companies currently active'
          },
          {
            title: 'Suspended Companies',
            value: data.suspendedCompanies,
            description: 'Companies currently suspended'
          },
          {
            title: 'Expired Companies',
            value: data.expiredCompanies,
            description: 'Companies with expired status'
          }
        ];

        this.platformUsers = {
          total: data.totalUsers,
          active: data.activeUsers,
          inactive: data.inactiveUsers
        };

        this.activeCompanyProgress =
          data.totalCompanies > 0
            ? Math.round(
                (data.activeCompanies / data.totalCompanies) * 100
              )
            : 0;

        this.userActivityProgress =
          data.totalUsers > 0
            ? Math.round(
                (data.activeUsers / data.totalUsers) * 100
              )
            : 0;

        this.companyHealthProgress =
          data.totalCompanies > 0
            ? Math.round(
                (
                  (data.activeCompanies +
                    data.suspendedCompanies) /
                  data.totalCompanies
                ) * 100
              )
            : 0;

        this.loading = false;

        this.changeDetectorRef.detectChanges();
      },

      error: (err) => {
        console.error(
          'Failed to load Super Admin dashboard:',
          err
        );

        this.error = 'Failed to load dashboard data.';
        this.loading = false;

        this.changeDetectorRef.detectChanges();
      }
    });
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  navigate(route: string): void {
    this.router.navigate([route]);
  }

  registerCompany(): void {
    this.router.navigate([
      '/super-admin/companies/register'
    ]);
  }

  viewCompanies(): void {
    this.router.navigate([
      '/super-admin/companies'
    ]);
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: () => {
        this.router.navigate(['/login']);
      }
    });
  }

  getInitials(): string {
    if (!this.user) {
      return 'S';
    }

    const first =
      this.user.firstName?.charAt(0) ?? '';

    const last =
      this.user.lastName?.charAt(0) ?? '';

    return (first + last).toUpperCase();
  }

  getProgressOffset(progress: number): number {
    const circumference = 301.59;

    return circumference -
      (progress / 100) * circumference;
  }
}
