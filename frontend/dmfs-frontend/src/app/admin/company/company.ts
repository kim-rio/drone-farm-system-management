import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import { Router } from '@angular/router';

import {
  Company,
  CompanyService
} from './company.service';

@Component({
  selector: 'app-admin-company',
  standalone: true,
  templateUrl: './company.html',
  styleUrl: './company.scss'
})
export class AdminCompany implements OnInit {

  private readonly router = inject(Router);
  private readonly companyService = inject(CompanyService);

  company = signal<Company | null>(null);

  loading = signal(true);

  errorMessage = signal('');

  ngOnInit(): void {
    this.loadCompany();
  }

  loadCompany(): void {

    this.loading.set(true);
    this.errorMessage.set('');

    this.companyService.getCompany().subscribe({

      next: (company: Company) => {
        console.log('MY COMPANY:', company);

        this.company.set(company);
        this.loading.set(false);
      },

      error: (error) => {
        console.error(
          'Unable to load company information:',
          error
        );

        this.errorMessage.set(
          'Unable to load company information.'
        );

        this.loading.set(false);
      }

    });
  }

  goBack(): void {
    this.router.navigate(['/admin']);
  }

  openStaff(): void {
    this.router.navigate(['/admin/staff']);
  }
}
