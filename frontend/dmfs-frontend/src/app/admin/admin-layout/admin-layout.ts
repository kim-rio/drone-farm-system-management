import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService, LoginResponse } from '../../services/auth.service';

interface AdminMenuItem {
  label: string;
  route: string;
}

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss'
})
export class AdminLayout {

  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  sidebarOpen = true;

  user: LoginResponse | null =
    this.authService.getCurrentUser();

  menuItems: AdminMenuItem[] = [
    {
      label: 'Dashboard',
      route: '/admin'
    },
    {
      label: 'Staff',
      route: '/admin/staff'
    },
    {
      label: 'Company',
      route: '/admin/company'
    },
    {
      label: 'Operations',
      route: '/admin/operations'
    },
    {
      label: 'Reports',
      route: '/admin/reports'
    },
    {
      label: 'Settings',
      route: '/admin/settings'
    }
  ];

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  navigate(route: string): void {
    this.router.navigate([route]);
  }

  isActive(route: string): boolean {

    if (route === '/admin') {
      return this.router.url === '/admin' ||
        this.router.url === '/admin/';
    }

    return this.router.url.startsWith(route);
  }

  getInitials(): string {

    if (!this.user) {
      return 'AD';
    }

    const first =
      this.user.firstName?.charAt(0) ?? '';

    const last =
      this.user.lastName?.charAt(0) ?? '';

    return `${first}${last}`.toUpperCase();
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
}
