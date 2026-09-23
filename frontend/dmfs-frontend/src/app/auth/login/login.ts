import {
  Component,
  inject,
  OnInit
} from '@angular/core';

import {
  Router
} from '@angular/router';

import {
  FormsModule
} from '@angular/forms';

import {
  AuthService
} from '../../services/auth.service';

import {
  PublicCompanyBranding,
  PublicCompanyBrandingService
} from '../../services/public-company-branding.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login implements OnInit {

  private readonly authService =
    inject(AuthService);

  private readonly brandingService =
    inject(PublicCompanyBrandingService);

  private readonly router =
    inject(Router);

  email = '';
  password = '';

  loading = false;
  brandingLoading = false;

  errorMessage = '';

  workspaceSlug: string | null = null;

  branding:
    PublicCompanyBranding | null = null;

  ngOnInit(): void {

    this.workspaceSlug =
      this.getWorkspaceFromHostname();

    console.log(
      'LOGIN HOST:',
      window.location.hostname
    );

    console.log(
      'LOGIN WORKSPACE:',
      this.workspaceSlug
    );

    /*
     * Company workspace branding is loaded
     * in the background.
     *
     * It must never block the login form.
     */

    if (this.workspaceSlug) {

      this.loadWorkspaceBranding();

    }

  }

  private getWorkspaceFromHostname():
    string | null {

    const hostname =
      window.location.hostname
        .trim()
        .toLowerCase();

    if (
      hostname === 'localhost' ||
      hostname === '127.0.0.1' ||
      hostname === '::1'
    ) {
      return null;
    }

    /*
     * Local company workspace:
     *
     * tukupala.localhost
     *        ↓
     * tukupala
     */

    if (hostname.endsWith('.localhost')) {

      const workspace =
        hostname.replace(
          /\.localhost$/,
          ''
        );

      return workspace || null;
    }

    /*
     * Production company workspace:
     *
     * tukupala.dmfs.com
     *        ↓
     * tukupala
     */

    const parts =
      hostname.split('.');

    if (parts.length < 3) {
      return null;
    }

    const workspace =
      parts[0].trim();

    if (
      !workspace ||
      workspace === 'www' ||
      workspace === 'api'
    ) {
      return null;
    }

    return workspace;
  }

  private loadWorkspaceBranding(): void {

    this.brandingLoading = true;

    this.brandingService
      .getBranding()
      .subscribe({

        next: branding => {

          this.branding =
            branding;

          this.workspaceSlug =
            branding.workspaceSlug;

          this.brandingLoading =
            false;

          console.log(
            'COMPANY WORKSPACE:',
            branding
          );

        },

        error: error => {

          this.brandingLoading =
            false;

          this.branding =
            null;

          if (error?.status === 404) {

            this.errorMessage =
              'Company workspace not found.';

          } else if (
            error?.error?.message
          ) {

            this.errorMessage =
              error.error.message;

          } else {

            this.errorMessage =
              'Unable to load the company workspace.';
          }

          console.error(
            'WORKSPACE BRANDING ERROR:',
            error
          );

        }

      });

  }

  login(): void {

    console.log(
      'LOGIN SUBMIT HOST:',
      window.location.hostname
    );

    console.log(
      'LOGIN SUBMIT WORKSPACE:',
      this.workspaceSlug
    );

    /*
     * Do NOT block login while branding loads.
     *
     * The backend independently validates:
     * hostname → company → user membership.
     */

    if (
      !this.email.trim() ||
      !this.password.trim()
    ) {

      this.errorMessage =
        'Please enter your email and password.';

      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(
      this.email.trim(),
      this.password
    ).subscribe({

      next: user => {

        this.loading = false;

        switch (user.role) {

          case 'SUPER_ADMIN':

            this.router.navigate(
              ['/super-admin']
            );

            break;

          case 'ADMIN':

            this.router.navigate(
              ['/admin']
            );

            break;

          case 'MANAGEMENT':

            this.router.navigate(
              ['/management']
            );

            break;

          case 'CUSTOMER':

            this.router.navigate(
              ['/customer']
            );

            break;

          case 'DRONE_OPERATOR':

            this.router.navigate(
              ['/drone-operator']
            );

            break;

          case 'GEOLOGIST':

            this.router.navigate(
              ['/geologist']
            );

            break;

          default:

            this.authService.clearSession();

            this.errorMessage =
              `Unsupported user role: ${user.role}`;

            break;

        }

      },

      error: error => {

        this.loading = false;

        if (error?.status === 401) {

          this.errorMessage =
            'Invalid email or password.';

        } else if (
          error?.error?.message
        ) {

          this.errorMessage =
            error.error.message;

        } else {

          this.errorMessage =
            'Unable to connect to the server.';
        }

        console.error(
          'LOGIN ERROR:',
          error
        );

      }

    });

  }

}
