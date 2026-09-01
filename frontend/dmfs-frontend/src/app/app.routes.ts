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
   
   {
  path: 'management/service-catalogue',
  loadComponent: () =>
    import('./management/service-catalogue/service-catalogue')
      .then(m => m.ServiceCatalogueComponent)
  },

   {
  path: 'management/service-requests/new',
  loadComponent: () =>
    import('./management/service-requests/create-service-request/create-service-request')
      .then(m => m.CreateServiceRequest)
  },

  {
  path: 'management/service-requests/:id',
  loadComponent: () =>
    import('./management/service-requests/service-request-details/service-request-details')
      .then(m => m.ServiceRequestDetails)
  },
  {
  path: 'management/service-requests',
  loadComponent: () =>
    import('./management/service-requests/service-request-list/service-request-list')
      .then(m => m.ServiceRequestList)
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