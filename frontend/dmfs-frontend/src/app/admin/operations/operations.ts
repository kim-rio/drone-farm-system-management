import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

interface Operation {
  id: string;
  client: string;
  service: string;
  location: string;
  assignedTeam: string;
  status: 'PENDING' | 'ACTIVE' | 'COMPLETED';
}

@Component({
  selector: 'app-admin-operations',
  standalone: true,
  templateUrl: './operations.html',
  styleUrl: './operations.scss'
})
export class AdminOperations {

  private readonly router = inject(Router);

  operations: Operation[] = [];

  filters = ['ALL', 'PENDING', 'ACTIVE', 'COMPLETED'];
  selectedFilter = 'ALL';

  get filteredOperations(): Operation[] {
    if (this.selectedFilter === 'ALL') {
      return this.operations;
    }

    return this.operations.filter(
      operation => operation.status === this.selectedFilter
    );
  }

  setFilter(filter: string): void {
    this.selectedFilter = filter;
  }

  goBack(): void {
    this.router.navigate(['/admin']);
  }
}
