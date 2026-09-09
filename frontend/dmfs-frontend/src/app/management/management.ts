import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService, LoginResponse } from '../services/auth.service';

interface ManagementMenuItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-management',
  standalone: true,
  templateUrl: './management.html',
  styleUrl: './management.scss'
})
export class Management {

  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  sidebarOpen = true;

  user: LoginResponse | null =
    this.authService.getCurrentUser();

  menuItems: ManagementMenuItem[] = [

    {
      label: 'Dashboard',
      route: '/management',
      icon: '▦'
    },

    {
      label: 'Clients',
      route: '/management/clients',
      icon: '♙'
    },

    {
      label: 'Farms',
      route: '/management/farms',
      icon: '⌂'
    },

    {
      label: 'Service Requests',
      route: '/management/service-requests',
      icon: '✓'
    }

  ];

  getInitials(): string {

    if (!this.user) {
      return 'MG';
    }

    const first =
      this.user.firstName?.charAt(0) ?? '';

    const last =
      this.user.lastName?.charAt(0) ?? '';

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
      return (
        this.router.url === '/management' ||
        this.router.url === '/management/'
      );
    }

    return this.router.url.startsWith(route);
  }

  logout(): void {

    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });

  }
}
