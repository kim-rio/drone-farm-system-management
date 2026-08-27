import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

interface MenuItem {
  label: string;
  route: string;
}

interface DashboardStat {
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

  /*
   * SIDEBAR
   */
  sidebarOpen = true;


  /*
   * CURRENT MANAGEMENT USER
   */
  user = this.authService.getCurrentUser();


  /*
   * SIDEBAR MENU
   *
   * No Staff / Users section.
   */
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
      label: 'Field Operations',
      route: '/management/operations'
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


  /*
   * DASHBOARD STATISTICS
   *
   * These remain temporary until the
   * management API is connected.
   */
  dashboardStats: DashboardStat[] = [
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


  /*
   * EXISTING PERCENTAGES
   *
   * Keep these for now.
   * They can later be connected to the backend.
   */
  surveyProgress = 0;

  fieldOperationProgress = 0;

  dataAnalysisProgress = 0;

  farmProgress = 0;


  /*
   * TOGGLE SIDEBAR
   */
  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }


  /*
   * NAVIGATION
   */
  navigate(route: string): void {
    this.router.navigate([route]);
  }


  /*
   * SETTINGS
   */
  openSettings(): void {
    this.router.navigate(['/management/settings']);
  }


  /*
   * LOGOUT
   */
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


  /*
   * USER INITIALS
   */
  getInitials(): string {

    if (!this.user) {
      return 'M';
    }

    const firstName =
      this.user.firstName?.charAt(0) ?? '';

    const lastName =
      this.user.lastName?.charAt(0) ?? '';

    return (
      firstName + lastName
    ).toUpperCase();
  }


  /*
   * CIRCULAR PROGRESS
   */
  getProgressOffset(progress: number): number {

    const circumference = 301.59;

    return circumference -
      (progress / 100) * circumference;
  }

}
