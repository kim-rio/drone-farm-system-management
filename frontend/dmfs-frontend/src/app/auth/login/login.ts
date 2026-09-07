import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  email = '';
  password = '';

  loading = false;
  errorMessage = '';

  login(): void {

    if (!this.email.trim() || !this.password.trim()) {
      this.errorMessage = 'Please enter your email and password.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(
      this.email.trim(),
      this.password
    ).subscribe({

      next: (user) => {

        this.loading = false;

        switch (user.role) {

          case 'SUPER_ADMIN':
            this.router.navigate(['/super-admin']);
            break;

          case 'ADMIN':
            this.router.navigate(['/admin']);
            break;

          case 'MANAGEMENT':
            this.router.navigate(['/management']);
            break;

          case 'DRONE_OPERATOR':
            this.router.navigate(['/drone-operator']);
            break;

          case 'GEOLOGIST':
            this.router.navigate(['/geologist']);
            break;

          default:
            this.authService.clearSession();
            this.errorMessage =
              `Unsupported user role: ${user.role}`;
            break;
        }
      },

      error: (error) => {

        this.loading = false;

        if (error?.status === 401) {
          this.errorMessage = 'Invalid email or password.';
        } else {
          this.errorMessage =
            'Unable to connect to the server.';
        }

        console.error('LOGIN ERROR:', error);
      }
    });
  }
}
