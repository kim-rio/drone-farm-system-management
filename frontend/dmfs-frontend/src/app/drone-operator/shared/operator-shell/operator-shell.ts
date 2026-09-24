import { CommonModule } from '@angular/common';
import { Component, Input, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { CompanyBrandingResponse, CompanyBrandingService } from '../../../services/company-branding.service';

@Component({
  selector: 'app-operator-shell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './operator-shell.html',
  styleUrl: './operator-shell.scss'
})
export class OperatorShell implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly brandingService = inject(CompanyBrandingService);

  branding: CompanyBrandingResponse = { companyId: 0, companyName: '', logoUrl: null };
  @Input() fixedSidebar = false;
  sidebarOpen = true;

  readonly menuItems = [
    { label: 'Dashboard', route: '/drone-operator' },
    { label: 'My Missions', route: '/drone-operator/missions' },
    { label: 'Field Surveys', route: '/drone-operator/surveys' },
    { label: 'Agricultural Reports', route: '/drone-operator/agriculture-reports' }
  ];

  user = this.auth.getCurrentUser();

  ngOnInit(): void {
    if (this.fixedSidebar) {
      this.sidebarOpen = true;
    }
    const stored = localStorage.getItem('dmfs.operator.sidebarOpen');
    if (!this.fixedSidebar && stored !== null) this.sidebarOpen = stored !== 'false';

    this.brandingService.getBranding().subscribe({
      next: branding => this.branding = branding,
      error: error => console.warn('COMPANY BRANDING LOAD ERROR:', error)
    });
  }

  get initials(): string {
    return `${this.user?.firstName?.[0] || 'O'}${this.user?.lastName?.[0] || ''}`.toUpperCase();
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
    localStorage.setItem('dmfs.operator.sidebarOpen', String(this.sidebarOpen));
  }

  navigate(route: string): void {
    this.router.navigateByUrl(route);
  }

  isActive(route: string): boolean {
    if (route === '/drone-operator') {
      return this.router.url === '/drone-operator' || this.router.url === '/drone-operator/';
    }
    return this.router.url.startsWith(route);
  }

  logout(): void {
    this.auth.logout().subscribe({
      complete: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }
}
