import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-settings',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './settings.html',
  styleUrl: './settings.scss'
})
export class AdminSettings {

  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  user = this.authService.getCurrentUser();

  notifications = true;
  compactView = false;

  saved = false;

  savePreferences(): void {
    this.saved = true;

    setTimeout(() => {
      this.saved = false;
    }, 2500);
  }

  goBack(): void {
    this.router.navigate(['/admin']);
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: () => {
        this.router.navigate(['/login']);
      }
    });
  }
}
