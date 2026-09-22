import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface InvitationDetails {
  clientId: number;
  clientCode: string;
  email: string;
  expiresAt: string;
}

@Component({
  selector: 'app-customer-activate',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './activate.html',
  styleUrl: './activate.scss'
})
export class Activate implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly http =
    inject(HttpClient);

  private readonly cdr =
    inject(ChangeDetectorRef);

  token = '';

  invitation: InvitationDetails | null = null;

  firstName = '';
  lastName = '';
  password = '';
  confirmPassword = '';

  loading = true;
  activating = false;

  errorMessage = '';
  successMessage = '';

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
  ngOnInit(): void {

    this.token =
      this.route.snapshot.queryParamMap.get('token') || '';

    if (!this.token) {
      this.loading = false;
      this.errorMessage =
        'This activation link is missing its invitation token.';
      return;
    }

    this.loadInvitation();
  }

  loadInvitation(): void {

    this.http
      .get<InvitationDetails>(
        `/api/client-portal/invitations/${encodeURIComponent(this.token)}`
      )
      .subscribe({

        next: (invitation) => {

          this.invitation = invitation;
          this.loading = false;


          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'INVITATION VALIDATION ERROR:',
            error
          );

          this.loading = false;
          this.errorMessage =
            'Unable to validate this activation link.';

          this.cdr.detectChanges();
        }
      });
  }

  activate(): void {

    this.errorMessage = '';
    this.successMessage = '';

    if (!this.firstName.trim()) {
      this.errorMessage = 'First name is required.';
      return;
    }

    if (!this.lastName.trim()) {
      this.errorMessage = 'Last name is required.';
      return;
    }

    if (this.password.length < 8) {
      this.errorMessage =
        'Password must contain at least 8 characters.';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.errorMessage =
        'Passwords do not match.';
      return;
    }

    this.activating = true;

    this.http
      .post(
        '/api/client-portal/activate',
        {
          token: this.token,
          firstName: this.firstName.trim(),
          lastName: this.lastName.trim(),
          password: this.password
        }
      )
      .subscribe({

        next: () => {

          this.activating = false;

          this.successMessage =
            'Your portal account has been activated. Redirecting to login...';

          this.cdr.detectChanges();

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 1200);
        },

        error: (error) => {

          console.error(
            'PORTAL ACTIVATION ERROR:',
            error
          );

          this.activating = false;

          this.errorMessage =
            error?.error?.message ||
            'Unable to activate your portal account.';

          this.cdr.detectChanges();
        }
      });
  }
}

