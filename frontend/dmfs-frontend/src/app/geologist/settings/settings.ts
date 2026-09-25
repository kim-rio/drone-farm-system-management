import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService, LoginResponse } from '../../services/auth.service';

@Component({
  selector: 'app-geologist-settings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './settings.html',
  styleUrl: './settings.scss'
})
export class Settings {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly user: LoginResponse | null = this.authService.getCurrentUser();

  loggingOut = false;

  get initials(): string {
    if (!this.user) {
      return 'GL';
    }
    const first = this.user.firstName?.charAt(0) ?? '';
    const last = this.user.lastName?.charAt(0) ?? '';
    return `${first}${last}`.toUpperCase() || 'GL';
  }

  logout(): void {
    this.loggingOut = true;
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }
}
