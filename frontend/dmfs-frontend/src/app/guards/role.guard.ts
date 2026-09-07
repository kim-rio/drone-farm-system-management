import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard = (
  allowedRoles: string[]
): CanActivateFn => {

  return () => {

    const authService = inject(AuthService);
    const router = inject(Router);

    const user = authService.getCurrentUser();

    if (!user) {
      return router.createUrlTree(['/login']);
    }

    if (allowedRoles.includes(user.role)) {
      return true;
    }

    switch (user.role) {

      case 'SUPER_ADMIN':
        return router.createUrlTree(['/super-admin']);

      case 'ADMIN':
        return router.createUrlTree(['/admin']);

      case 'MANAGEMENT':
        return router.createUrlTree(['/management']);

      case 'DRONE_OPERATOR':
        return router.createUrlTree(['/drone-operator']);

      case 'GEOLOGIST':
        return router.createUrlTree(['/geologist']);

      default:
        return router.createUrlTree(['/login']);
    }
  };
};
