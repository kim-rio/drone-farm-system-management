import { Routes } from '@angular/router';

export const routes: Routes = [

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login')
        .then(m => m.Login)
  },

  {
    path: 'management',
    loadComponent: () =>
      import('./management/management')
        .then(m => m.Management)
  },

  {
    path: '**',
    redirectTo: 'login'
  }

];
