import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

interface Stat {
  title: string;
  value: number;
  description: string;
}

@Component({
  selector: 'app-admin',
  standalone: true,
  templateUrl: './admin.html',
  styleUrl: './admin.scss'
})
export class Admin {

  private readonly router = inject(Router);

  dashboardStats: Stat[] = [
    {
      title: 'Total Staff',
      value: 0,
      description: 'Staff members in your company'
    },
    {
      title: 'Active Staff',
      value: 0,
      description: 'Currently active staff'
    },
    {
      title: 'Pending Work',
      value: 0,
      description: 'Work requiring attention'
    },
    {
      title: 'Active Operations',
      value: 0,
      description: 'Operations currently active'
    }
  ];

  get inactiveStaff(): number {
    return Math.max(
      this.dashboardStats[0].value -
      this.dashboardStats[1].value,
      0
    );
  }

  get totalOperations(): number {
    return (
      this.dashboardStats[2].value +
      this.dashboardStats[3].value
    );
  }

  get activeStaffProgress(): number {

    const total =
      this.dashboardStats[0].value;

    if (total <= 0) {
      return 0;
    }

    return Math.round(
      (this.dashboardStats[1].value / total) * 100
    );
  }

  get operationProgress(): number {

    const total =
      this.totalOperations;

    if (total <= 0) {
      return 0;
    }

    return Math.round(
      (this.dashboardStats[3].value / total) * 100
    );
  }

  getProgressOffset(progress: number): number {

    const circumference = 301.59;

    const safeProgress =
      Math.min(
        Math.max(progress, 0),
        100
      );

    return circumference -
      (circumference * safeProgress / 100);
  }

  manageStaff(): void {
    this.router.navigate(['/admin/staff']);
  }

  openCompany(): void {
    this.router.navigate(['/admin/company']);
  }

  openOperations(): void {
    this.router.navigate(['/admin/operations']);
  }

  openReports(): void {
    this.router.navigate(['/admin/reports']);
  }
}
