import {
  Component,
  ChangeDetectorRef,
  OnInit,
  inject
} from '@angular/core';

import { Router } from '@angular/router';

import {
  AuthService,
  LoginResponse
} from '../../services/auth.service';

import {
  CompanyResponse,
  SuperAdminService
} from '../../services/super-admin.service';

@Component({
  selector: 'app-super-admin-companies',
  standalone: true,
  imports: [],
  templateUrl: './companies.html',
  styleUrl: './companies.scss'
})
export class Companies implements OnInit {

  private readonly router = inject(Router);

  private readonly service =
    inject(SuperAdminService);

  private readonly authService =
    inject(AuthService);

  private readonly cdr =
    inject(ChangeDetectorRef);

  companies: CompanyResponse[] = [];

  loading = true;

  error = '';

  sidebarOpen = true;

  user: LoginResponse | null =
    this.authService.getCurrentUser();

  totalCompanies = 0;

  activeCompanies = 0;

  suspendedCompanies = 0;

  expiredCompanies = 0;

  ngOnInit(): void {
    this.loadCompanies();
  }

  loadCompanies(): void {

    this.loading = true;

    this.error = '';

    this.service.getCompanies().subscribe({

      next: (data) => {

        this.companies = data;

        this.totalCompanies =
          data.length;

        this.activeCompanies =
          data.filter(
            company =>
              company.status === 'ACTIVE'
          ).length;

        this.suspendedCompanies =
          data.filter(
            company =>
              company.status === 'SUSPENDED'
          ).length;

        this.expiredCompanies =
          data.filter(
            company =>
              company.status === 'EXPIRED'
          ).length;

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {

        console.error(
          'Failed to load companies:',
          err
        );

        this.error =
          err?.error?.message ||
          'Failed to load companies.';

        this.loading = false;

        this.cdr.detectChanges();
      }

    });
  }

  toggleSidebar(): void {
    this.sidebarOpen =
      !this.sidebarOpen;
  }

  navigate(route: string): void {
    this.router.navigate([route]);
  }

  registerCompany(): void {

    this.router.navigate([
      '/super-admin/companies/register'
    ]);
  }

  viewCompany(id: number): void {

    this.router.navigate([
      '/super-admin/companies',
      id
    ]);
  }

  editCompany(id: number): void {

    this.router.navigate([
      '/super-admin/companies',
      id,
      'edit'
    ]);
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }

  formatDate(date: string): string {
    return new Date(date)
      .toLocaleDateString();
  }

  getInitials(): string {

    if (!this.user) {
      return 'SA';
    }

    const first =
      this.user.firstName?.charAt(0) ?? '';

    const last =
      this.user.lastName?.charAt(0) ?? '';

    return (
      first + last
    ).toUpperCase();
  }

  logout(): void {

    this.authService.logout().subscribe({

      next: () => {
        this.router.navigate(['/login']);
      },

      error: () => {

        this.authService.clearSession();

        this.router.navigate(['/login']);
      }

    });
  }
}
