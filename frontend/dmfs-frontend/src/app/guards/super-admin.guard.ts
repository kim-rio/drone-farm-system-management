import { CanActivateFn } from '@angular/router';
import { roleGuard } from './role.guard';

export const superAdminGuard: CanActivateFn =
  roleGuard(['SUPER_ADMIN']);
