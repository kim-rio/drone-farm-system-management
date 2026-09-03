import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

interface ReportType {
  title: string;
  description: string;
  count: number;
}

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  templateUrl: './reports.html',
  styleUrl: './reports.scss'
})
export class AdminReports {

  private readonly router = inject(Router);

  reports: ReportType[] = [
    {
      title: 'Operational Reports',
      description: 'Summary of field operations and their status.',
      count: 0
    },
    {
      title: 'Survey Reports',
      description: 'Survey and exploration activity reports.',
      count: 0
    },
    {
      title: 'Staff Activity',
      description: 'Summary of staff activity within the company.',
      count: 0
    },
    {
      title: 'Company Activity',
      description: 'Overall company activity and service history.',
      count: 0
    }
  ];

  goBack(): void {
    this.router.navigate(['/admin']);
  }
}
