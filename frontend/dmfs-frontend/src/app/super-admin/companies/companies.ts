import { Component, ChangeDetectorRef, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
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
  private readonly service = inject(SuperAdminService);
  private readonly cdr = inject(ChangeDetectorRef);

  companies: CompanyResponse[] = [];

  loading = true;
  error = '';

  sidebarOpen = true;

  user = this.service['http'] ? null : null;

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

        this.totalCompanies = data.length;
        this.activeCompanies =
          data.filter(c => c.status === 'ACTIVE').length;
        this.suspendedCompanies =
          data.filter(c => c.status === 'SUSPENDED').length;
        this.expiredCompanies =
          data.filter(c => c.status === 'EXPIRED').length;

        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load companies:', err);
        this.error = 'Failed to load companies.';
        this.loading = false;
        this.cdr.detectChanges();
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
    this.router.navigate(['/super-admin/companies/register']);
  }

  viewCompany(id: number): void {
    this.router.navigate(['/super-admin/companies', id]);
  }

  editCompany(id: number): void {
    this.router.navigate(['/super-admin/companies', id, 'edit']);
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString();
  }
}
