import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import {
  AuthService,
  LoginResponse
} from '../../services/auth.service';

interface WorkspaceItem {
  label: string;
  description: string;
  route: string;
}

@Component({
  selector: 'app-role-workspace',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './role-workspace.html',
  styleUrl: './role-workspace.scss'
})
export class RoleWorkspace implements OnInit {

  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);

  user: LoginResponse | null =
    this.authService.getCurrentUser();

  sidebarOpen = true;

  role = '';

  title = 'Dashboard';

  subtitle = '';

  menuItems: WorkspaceItem[] = [];

  get firstName(): string {
    return this.user?.firstName ?? 'User';
  }

  get lastName(): string {
    return this.user?.lastName ?? '';
  }

  ngOnInit(): void {

    const data = this.route.snapshot.data;

    this.role = data['role'] ?? '';

    this.title =
      data['title'] ?? this.getDefaultTitle();

    this.subtitle =
      data['subtitle'] ?? this.getDefaultSubtitle();

    this.menuItems =
      data['menuItems'] ?? [];
  }

  private getDefaultTitle(): string {

    switch (this.role) {

      case 'DRONE_OPERATOR':
        return 'Drone Operations Dashboard';

      case 'GEOLOGIST':
        return 'Geologist Dashboard';

      default:
        return 'Dashboard';
    }
  }

  private getDefaultSubtitle(): string {

    switch (this.role) {

      case 'DRONE_OPERATOR':
        return 'Manage assigned drone operations and survey activities.';

      case 'GEOLOGIST':
        return 'Review survey data, analysis and technical reports.';

      default:
        return 'Manage your assigned work from one place.';
    }
  }

  getRoleLabel(): string {

    switch (this.role) {

      case 'DRONE_OPERATOR':
        return 'DRONE OPERATOR';

      case 'GEOLOGIST':
        return 'GEOLOGIST';

      default:
        return this.role;
    }
  }

  getInitials(): string {

    if (!this.user) {
      return 'U';
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

    if (route === this.router.url) {
      return true;
    }

    return this.router.url.startsWith(route + '/');
  }

  logout(): void {

    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });

  }
}
