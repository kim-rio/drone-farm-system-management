import { Component, inject } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { filter, startWith } from 'rxjs';
import {
  CompanyBrandingResponse,
  CompanyBrandingService
} from '../../services/company-branding.service';
import {
  AuthService,
  LoginResponse
} from '../../services/auth.service';

interface GeologistMenuItem {
  label: string;
  route: string;
}

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './layout.html',
  styleUrl: './layout.scss'
})
export class Layout {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly brandingService = inject(CompanyBrandingService);

  sidebarOpen = true;

  branding: CompanyBrandingResponse = {
    companyId: 0,
    companyName: '',
    logoUrl: null
  };

  readonly user: LoginResponse | null = this.authService.getCurrentUser();
  readonly initials = this.buildInitials();

  readonly menuItems: GeologistMenuItem[] = [
    { label: 'Dashboard', route: '/geologist/dashboard' },
    { label: 'Survey History', route: '/geologist/survey-history' },
    { label: 'Anomaly Map Review', route: '/geologist/anomaly-map-review' },
    { label: 'AI Reports', route: '/geologist/ai-reports' }
  ];

  activeRoute = this.router.url;

  constructor() {
    this.brandingService.getBranding().subscribe({
      next: branding => this.branding = branding,
      error: error => console.warn('COMPANY BRANDING LOAD ERROR:', error)
    });

    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        startWith(null)
      )
      .subscribe(() => {
        this.activeRoute = this.router.url;
      });
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  navigate(route: string): void {
    if (this.activeRoute === route) {
      return;
    }
    this.router.navigateByUrl(route);
  }

  isActive(route: string): boolean {
    return this.activeRoute === route ||
      (route !== '/geologist/dashboard' && this.activeRoute.startsWith(route));
  }

  private buildInitials(): string {
    if (!this.user) {
      return 'GL';
    }

    const first = this.user.firstName?.charAt(0) ?? '';
    const last = this.user.lastName?.charAt(0) ?? '';

    return `${first}${last}`.toUpperCase() || 'GL';
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }
}
