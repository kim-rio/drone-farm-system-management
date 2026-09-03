import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  CompanyResponse,
  SuperAdminService
} from '../../../services/super-admin.service';

@Component({
  selector: 'app-company-details',
  standalone: true,
  imports: [],
  templateUrl: './company-details.html',
  styleUrl: './company-details.scss'
})
export class CompanyDetails implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(SuperAdminService);
  private readonly cdr = inject(ChangeDetectorRef);

  company: CompanyResponse | null = null;

  loading = true;
  error = '';
  actionLoading = false;
  actionError = '';

  ngOnInit(): void {

    const idParam =
      this.route.snapshot.paramMap.get('id');

    console.log(
      'Company Details route ID:',
      idParam
    );

    const id = Number(idParam);

    if (!id || Number.isNaN(id)) {

      this.error = 'Invalid company ID.';
      this.loading = false;

      this.cdr.detectChanges();

      return;
    }

    this.loadCompany(id);
  }

  private loadCompany(id: number): void {

    this.loading = true;
    this.error = '';

    console.log(
      'Loading company details:',
      id
    );

    this.service.getCompany(id).subscribe({

      next: (company) => {

        console.log(
          'Company details received:',
          company
        );

        this.company = company;
        this.loading = false;

        console.log(
          'Loading state:',
          this.loading
        );

        this.cdr.detectChanges();

        console.log(
          'Company details loaded.'
        );
      },

      error: (err) => {

        console.error(
          'Failed to load company details:',
          err
        );

        this.error =
          err?.error?.message ||
          'Failed to load company.';

        this.loading = false;

        this.cdr.detectChanges();
      },

      complete: () => {

        console.log(
          'Company details request completed.'
        );
      }

    });
  }

  backToCompanies(): void {

    this.router.navigate([
      '/super-admin/companies'
    ]);
  }

  goToDashboard(): void {

    this.router.navigate([
      '/super-admin'
    ]);
  }

  logout(): void {

    this.router.navigate([
      '/login'
    ]);
  }

  editCompany(): void {

    if (!this.company) {
      return;
    }

    this.router.navigate([
      '/super-admin/companies',
      this.company.id,
      'edit'
    ]);
  }

  activate(): void {

    if (!this.company) {
      return;
    }

    this.changeStatus('activate');
  }

  suspend(): void {

    if (!this.company) {
      return;
    }

    this.changeStatus('suspend');
  }

  expire(): void {

    if (!this.company) {
      return;
    }

    this.changeStatus('expire');
  }

  private changeStatus(
    action: 'activate' | 'suspend' | 'expire'
  ): void {

    if (!this.company) {
      return;
    }

    this.actionLoading = true;
    this.actionError = '';

    let request$;

    if (action === 'activate') {

      request$ =
        this.service.activateCompany(
          this.company.id
        );

    } else if (action === 'suspend') {

      request$ =
        this.service.suspendCompany(
          this.company.id
        );

    } else {

      request$ =
        this.service.expireCompany(
          this.company.id
        );
    }

    request$.subscribe({

      next: (company) => {

        console.log(
          'Company status updated:',
          company
        );

        this.company = company;
        this.actionLoading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {

        console.error(
          'Failed to change company status:',
          err
        );

        this.actionError =
          err?.error?.message ||
          'Failed to change company status.';

        this.actionLoading = false;

        this.cdr.detectChanges();
      }

    });
  }

  statusClass(): string {

    if (!this.company) {
      return '';
    }

    return this.company.status.toLowerCase();
  }

  formatDate(
    date: string | null | undefined
  ): string {

    if (!date) {
      return '—';
    }

    return new Date(date).toLocaleString();
  }
}
