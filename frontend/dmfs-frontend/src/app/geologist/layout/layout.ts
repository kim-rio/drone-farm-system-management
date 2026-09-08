import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

interface NavItem {
  label: string;
  path: string;
  icon: string;
  badge?: boolean;
}

const MOBILE_BREAKPOINT = 900;

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.html',
  styleUrl: './layout.scss'
})
export class Layout {
  navItems: NavItem[] = [
    { label: 'Dashboard', path: 'dashboard', icon: 'grid' },
    { label: 'Survey History', path: 'survey-history', icon: 'clock' },
    { label: 'Anomaly Map Review', path: 'anomaly-map-review', icon: 'map', badge: true },
    { label: 'AI Reports', path: 'ai-reports', icon: 'file' }
  ];

  isMobile = window.innerWidth < MOBILE_BREAKPOINT;

  // Desktop: sidebar is open by default, user can collapse it to an icon rail.
  desktopCollapsed = false;

  // Mobile: sidebar is closed by default, opens as an overlay drawer.
  mobileOpen = false;

  @HostListener('window:resize')
  onResize(): void {
    this.isMobile = window.innerWidth < MOBILE_BREAKPOINT;
    if (!this.isMobile) {
      this.mobileOpen = false;
    }
  }

  toggleSidebar(): void {
    if (this.isMobile) {
      this.mobileOpen = !this.mobileOpen;
    } else {
      this.desktopCollapsed = !this.desktopCollapsed;
    }
  }

  closeMobileNav(): void {
    if (this.isMobile) {
      this.mobileOpen = false;
    }
  }
}
