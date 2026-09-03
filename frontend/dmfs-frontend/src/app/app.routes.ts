import { Routes } from '@angular/router';
import { superAdminGuard } from './guards/super-admin.guard';

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
    path: 'super-admin',
    canActivate: [superAdminGuard],
    loadComponent: () =>
      import('./super-admin/super-admin')
        .then(m => m.SuperAdmin)
  },
  {
    path: 'super-admin/companies',
    canActivate: [superAdminGuard],
    loadComponent: () =>
      import('./super-admin/companies/companies')
        .then(m => m.Companies)
  },
  {
    path: 'super-admin/companies/register',
    canActivate: [superAdminGuard],
    loadComponent: () =>
      import('./super-admin/companies/register/register')
        .then(m => m.RegisterCompany)
  },
  {
    path: 'super-admin/companies/:id/edit',
    canActivate: [superAdminGuard],
    loadComponent: () =>
      import('./super-admin/companies/edit/edit')
        .then(m => m.EditCompany)
  },
  {
    path: 'super-admin/companies/:id',
    canActivate: [superAdminGuard],
    loadComponent: () =>
      import('./super-admin/companies/company-details/company-details')
        .then(m => m.CompanyDetails)
  },
  {
    path: 'admin',
    loadComponent: () =>
      import('./admin/admin-layout/admin-layout')
        .then(m => m.AdminLayout),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./admin/admin')
            .then(m => m.Admin)
      },
      {
        path: 'staff',
        loadComponent: () =>
          import('./admin/staff/staff')
            .then(m => m.Staff)
      },
      {
        path: 'company',
        loadComponent: () =>
          import('./admin/company/company')
            .then(m => m.AdminCompany)
      },
      {
        path: 'operations',
        loadComponent: () =>
          import('./admin/operations/operations')
            .then(m => m.AdminOperations)
      },
      {
        path: 'operations/service-catalogue',
        loadComponent: () =>
          import('./admin/operations/service-catalogue/service-catalogue')
            .then(m => m.ServiceCataloguePage)
      },
      {
        path: 'reports',
        loadComponent: () =>
          import('./admin/reports/reports')
            .then(m => m.AdminReports)
      },
      {
        path: 'settings',
        loadComponent: () =>
          import('./admin/settings/settings')
            .then(m => m.AdminSettings)
      }
    ]
  },
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
  {
    path: 'management/clients/register',
    loadComponent: () =>
      import('./management/clients/register-clients/register-clients')
        .then(m => m.RegisterClients)
  },
  {
    path: 'management/clients/:clientId/farms/:farmId',
    loadComponent: () =>
      import('./management/clients/client-details/farm-details/farm-details')
        .then(m => m.FarmDetails)
  },
  {
    path: 'management/clients/:id',
    loadComponent: () =>
      import('./management/clients/client-details/client-details')
        .then(m => m.ClientDetails)
  },
  {
    path: 'management/clients',
    loadComponent: () =>
      import('./management/clients/client-list/client-list')
        .then(m => m.ClientList)
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
