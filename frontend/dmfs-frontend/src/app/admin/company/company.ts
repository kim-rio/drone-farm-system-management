import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-company',
  standalone: true,
  templateUrl: './company.html',
  styleUrl: './company.scss'
})
export class AdminCompany {

  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  user = this.authService.getCurrentUser();

  company = {
    name: 'Subscriber Company',
    registrationNumber: 'Not available',
    tin: 'Not available',
    email: 'Not available',
    phone: 'Not available',
    country: 'Not available',
    region: 'Not available',
    city: 'Not available',
    physicalAddress: 'Not available',
    status: 'ACTIVE'
  };

  goBack(): void {
    this.router.navigate(['/admin']);
  }

  openStaff(): void {
    this.router.navigate(['/admin/staff']);
  }
}
