import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService, LoginResponse } from '../services/auth.service';
import { ManagementDashboardService } from './management-dashboard.service';

interface ManagementMenuItem {
  label: string;
  route: string;
}

interface ManagementDashboard {
  clients: number;
  farms: number;
  blocks: number;
  serviceRequests: number;
  pendingPayments: number;
  paidPayments: number;
  requestStatuses: Record<string, number>;
  missions: number;
  missionStatuses: Record<string, number>;
  paidRequestsAwaitingMission: number;
  missionsAwaitingAssignment: number;
  missionsAwaitingAcceptance: number;
}

@Component({
  selector: 'app-management',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './management.html',
  styleUrl: './management.scss'
})
export class Management {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly dashboardService = inject(ManagementDashboardService);

  dashboard: ManagementDashboard = {
    clients: 0,
    farms: 0,
    blocks: 0,
    serviceRequests: 0,
    pendingPayments: 0,
    paidPayments: 0,
    requestStatuses: {},
    missions: 0,
    missionStatuses: {},
    paidRequestsAwaitingMission: 0,
    missionsAwaitingAssignment: 0,
    missionsAwaitingAcceptance: 0
  };

  sidebarOpen = true;
  user: LoginResponse | null = this.authService.getCurrentUser();

  menuItems: ManagementMenuItem[] = [
    { label: 'Dashboard', route: '/management' },
    { label: 'Clients', route: '/management/clients' },
    { label: 'Farms', route: '/management/farms' },
    { label: 'Service Requests', route: '/management/service-requests' },
    { label: 'Missions', route: '/management/missions' }
  ];

  constructor() {
    this.dashboardService.getDashboard().subscribe({
      next: data => this.dashboard = data,
      error: err => console.error('Management dashboard failed:', err)
    });
  }

  getInitials(): string {
    if (!this.user) return 'MG';

    const first = this.user.firstName?.charAt(0) ?? '';
    const last = this.user.lastName?.charAt(0) ?? '';

    return `${first}${last}`.toUpperCase();
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  navigate(route: string): void {
    this.router.navigateByUrl(route);
  }

  isActive(route: string): boolean {
    if (route === '/management') {
      return this.router.url === '/management' || this.router.url === '/management/';
    }

    return this.router.url.startsWith(route);
  }

  isDashboard(): boolean {
    return this.router.url === '/management' || this.router.url === '/management/';
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }
}