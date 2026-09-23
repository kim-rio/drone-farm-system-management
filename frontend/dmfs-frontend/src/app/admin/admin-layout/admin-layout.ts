import {
  Component,
  inject
} from '@angular/core';

import {
  NavigationEnd,
  Router,
  RouterOutlet
} from '@angular/router';

import {
  filter,
  startWith
} from 'rxjs';

import {
  CompanyBrandingService,
  CompanyBrandingResponse
} from '../../services/company-branding.service';

import {
  AuthService,
  LoginResponse
} from '../../services/auth.service';

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

  private readonly router =
    inject(Router);

  private readonly authService =
    inject(AuthService);

  private readonly brandingService =
    inject(CompanyBrandingService);

  sidebarOpen = true;

  branding: CompanyBrandingResponse | null = null;

  readonly user: LoginResponse | null =
    this.authService.getCurrentUser();

  readonly initials =
    this.buildInitials();

  readonly menuItems: AdminMenuItem[] = [
    {
      label: 'Dashboard',
      route: '/admin'
    },
    {
      label: 'Staff',
      route: '/admin/staff'
    },
    {
      label: 'Services',
      route: '/admin/services'
    }
  ];

  activeRoute =
    this.router.url;

  constructor() {

    this.loadBranding();

    this.router.events
      .pipe(
        filter(
          event =>
            event instanceof NavigationEnd
        ),
        startWith(null)
      )
      .subscribe(() => {

        this.activeRoute =
          this.router.url;

      });

  }

  private loadBranding(): void {

    this.brandingService
      .getBranding()
      .subscribe({

        next: branding => {

          this.branding =
            branding;

        },

        error: error => {

          console.error(
            'ADMIN BRANDING ERROR:',
            error
          );

        }

      });

  }

  toggleSidebar(): void {

    this.sidebarOpen =
      !this.sidebarOpen;

  }

  navigate(route: string): void {

    if (this.activeRoute === route) {
      return;
    }

    this.router.navigateByUrl(route);

  }

  isActive(route: string): boolean {

    if (route === '/admin') {

      return (
        this.activeRoute === '/admin' ||
        this.activeRoute === '/admin/'
      );

    }

    return this.activeRoute
      .startsWith(route);

  }

  private buildInitials(): string {

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

    this.authService
      .logout()
      .subscribe({

        next: () => {

          this.router.navigate(
            ['/login']
          );

        },

        error: () => {

          this.router.navigate(
            ['/login']
          );

        }

      });

  }

}
