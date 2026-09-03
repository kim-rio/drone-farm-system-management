import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const superAdminGuard: CanActivateFn = () => {

  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.getCurrentUser();

  if (!user) {
    return router.createUrlTree(['/login']);
  }

  if (user.role !== 'SUPER_ADMIN') {
    return router.createUrlTree(['/login']);
  }

  return true;
};
