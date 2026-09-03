import { Component, OnInit, inject } from '@angular/core';
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

  company: Company = {
    id: 0,
    name: 'Loading...',
    registrationNumber: 'Loading...',
    tin: null,
    email: null,
    phone: null,
    country: 'Loading...',
    region: 'Loading...',
    city: 'Loading...',
    physicalAddress: 'Loading...',
    status: 'LOADING',
    createdAt: '',
    updatedAt: null
  };

  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.loadCompany();
  }

  loadCompany(): void {

    this.loading = true;
    this.errorMessage = '';

    this.companyService.getCompany().subscribe({

      next: (company) => {
        this.company = company;
        this.loading = false;
      },

      error: (error) => {
        console.error(
          'Unable to load company information:',
          error
        );

        this.errorMessage =
          'Unable to load company information.';

        this.loading = false;
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
