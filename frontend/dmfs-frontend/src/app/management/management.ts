import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

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
  selector: 'app-management',
  standalone: true,
  templateUrl: './management.html',
  styleUrl: './management.scss'
})
export class Management {

  private router = inject(Router);
  private authService = inject(AuthService);

  sidebarOpen = true;

  user = this.authService.getCurrentUser();

  menuItems: MenuItem[] = [
    {
      label: 'Dashboard',
      route: '/management'
    },
    {
      label: 'Clients',
      route: '/management/clients'
    },
    {
      label: 'Farms',
      route: '/management/farms'
    },
    {
      label: 'Survey Requests',
      route: '/management/surveys'
    },
    {
      label: 'Data Analysis',
      route: '/management/analysis'
    },
    {
      label: 'Reports',
      route: '/management/reports'
    },
    {
      label: 'Finance',
      route: '/management/finance'
    }
  ];

  dashboardStats: Stat[] = [
    {
      title: 'Registered Clients',
      value: 0,
      description: 'Total clients registered'
    },
    {
      title: 'Registered Farms',
      value: 0,
      description: 'Total farms in the system'
    },
    {
      title: 'Pending Survey Requests',
      value: 0,
      description: 'Requests awaiting action'
    },
    {
      title: 'Active Field Operations',
      value: 0,
      description: 'Operations currently active'
    }
  ];

  surveyProgress = 0;
  fieldOperationProgress = 0;
  dataAnalysisProgress = 0;
  farmProgress = 0;

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  navigate(route: string): void {
    this.router.navigate([route]);
  }

  registerClient(): void {
    this.router.navigate(['/management/clients/register']);
  }

  newSurveyRequest(): void {
    this.router.navigate(['/management/surveys/new']);
  }

  openSettings(): void {
    this.router.navigate(['/management/settings']);
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
      return 'M';
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
