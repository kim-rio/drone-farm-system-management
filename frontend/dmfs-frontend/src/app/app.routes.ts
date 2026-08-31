import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  // ==============================
  // LOGIN
  // ==============================

  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login')
        .then(m => m.Login)
  },

  // ==============================
  // MANAGEMENT DASHBOARD
  // ==============================

  {
    path: 'management',
    loadComponent: () =>
      import('./management/management')
        .then(m => m.Management)
  },

  // ==============================
  // CLIENT LIST
  // ==============================

  {
    path: 'management/clients',
    loadComponent: () =>
      import('./management/clients/client-list/client-list')
        .then(m => m.ClientList)
  },

  // ==============================
  // REGISTER CLIENT
  // ==============================

  {
    path: 'management/clients/register',
    loadComponent: () =>
      import('./management/clients/register-clients/register-clients')
        .then(m => m.RegisterClients)
  },

  // ==============================
  // FARM DETAILS
  // Put this BEFORE client :id
  // ==============================

  {
    path: 'management/clients/:clientId/farms/:farmId',
    loadComponent: () =>
      import('./management/clients/client-details/farm-details/farm-details')
        .then(m => m.FarmDetails)
  },

  // ==============================
  // CLIENT DETAILS
  // ==============================

  {
    path: 'management/clients/:id',
    loadComponent: () =>
      import('./management/clients/client-details/client-details')
        .then(m => m.ClientDetails)
  },

  // ==============================
  // FALLBACK
  // ==============================

  {
    path: '**',
    redirectTo: 'login'
  }
];