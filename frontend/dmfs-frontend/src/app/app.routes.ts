import { Routes } from '@angular/router';

import { superAdminGuard } from './guards/super-admin.guard';
import { roleGuard } from './guards/role.guard';
import { RoleWorkspace } from './shared/role-workspace/role-workspace';

import { FarmList } from './management/farms/farm-list';

export const routes: Routes = [

  // ==========================================================
  // AUTH
  // ==========================================================

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


  // ==========================================================
  // SUPER ADMIN
  // ==========================================================

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


  // ==========================================================
  // ADMIN
  // ==========================================================

  {
    path: 'admin',
    canActivate: [roleGuard(['ADMIN'])],
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
        path: 'services',
        loadComponent: () =>
          import('./admin/services/service-catalogue')
            .then(m => m.ServiceCataloguePage)
      },
    ]
  },


  // ==========================================================
  // MANAGEMENT
  // ==========================================================

  {
    path: 'management',
    canActivate: [roleGuard(['MANAGEMENT'])],
    loadComponent: () =>
      import('./management/management')
        .then(m => m.Management),
    children: [
      { path: 'clients', loadComponent: () => import('./management/clients/client-list/client-list').then(m => m.ClientList) },
      { path: 'clients/register', loadComponent: () => import('./management/clients/register-clients/register-clients').then(m => m.RegisterClients) },
      { path: 'clients/:clientId/farms/:farmId', loadComponent: () => import('./management/clients/client-details/farm-details/farm-details').then(m => m.FarmDetails) },
      { path: 'clients/:id', loadComponent: () => import('./management/clients/client-details/client-details').then(m => m.ClientDetails) },
      { path: 'farms', component: FarmList },
      { path: 'service-requests', loadComponent: () => import('./management/service-requests/service-request-list/service-request-list').then(m => m.ServiceRequestList) },
      { path: 'service-requests/new', loadComponent: () => import('./management/service-requests/create-service-request/create-service-request').then(m => m.CreateServiceRequest) },
      { path: 'service-requests/:id', loadComponent: () => import('./management/service-requests/service-request-details/service-request-details').then(m => m.ServiceRequestDetails) },
      { path: 'missions', loadComponent: () => import('./management/missions/mission-list/mission-list').then(m => m.MissionList) },
      { path: 'missions/new', loadComponent: () => import('./management/missions/create-mission/create-mission').then(m => m.CreateMission) },
      { path: 'missions/:id', loadComponent: () => import('./management/missions/mission-details/mission-details').then(m => m.MissionDetails) }
    ]
  },


  // ==========================================================
// DRONE OPERATOR
// ==========================================================

{
  path: 'drone-operator',
  canActivate: [roleGuard(['DRONE_OPERATOR'])],
  loadComponent: () =>
    import('./drone-operator/operator-workspace')
      .then(m => m.OperatorWorkspace),
  data: {
    page: 'dashboard'
  }
},

{
  path: 'drone-operator/missions',
  canActivate: [roleGuard(['DRONE_OPERATOR'])],
  loadComponent: () =>
    import('./drone-operator/operator-workspace')
      .then(m => m.OperatorWorkspace),
  data: {
    page: 'missions'
  }
},

{
  path: 'drone-operator/missions/:id',
  canActivate: [roleGuard(['DRONE_OPERATOR'])],
  loadComponent: () =>
    import('./drone-operator/operator-workspace')
      .then(m => m.OperatorWorkspace),
  data: {
    page: 'detail'
  }
},

{
  path: 'drone-operator/surveys',
  canActivate: [roleGuard(['DRONE_OPERATOR'])],
  loadComponent: () =>
    import('./drone-operator/operator-workspace')
      .then(m => m.OperatorWorkspace),
  data: {
    page: 'surveys'
  }
},

// Individual survey data package
{
  path: 'drone-operator/surveys/:id/data',
  canActivate: [roleGuard(['DRONE_OPERATOR'])],
  loadComponent: () =>
    import('./drone-operator/surveys/survey-data/survey-data')
      .then(m => m.SurveyData)
},

// Individual Field Survey
{
  path: 'drone-operator/surveys/:id',
  canActivate: [roleGuard(['DRONE_OPERATOR'])],
  loadComponent: () =>
    import('./drone-operator/surveys/survey-details/survey-details')
      .then(m => m.SurveyDetails)
},

{
  path: 'drone-operator/operations',
  canActivate: [roleGuard(['DRONE_OPERATOR'])],
  loadComponent: () =>
    import('./drone-operator/operations/operations')
      .then(m => m.OperationsComponent)
},


  {
  path: 'geologist',
  canActivate: [roleGuard(['GEOLOGIST'])],
  loadChildren: () =>
    import('./geologist/geologist.routes')
      .then(m => m.GEOLOGIST_ROUTES)
  },
  


  // ==========================================================
  // ==========================================================
  // CUSTOMER PORTAL
  // ==========================================================

  // PUBLIC: client must activate before logging in.
  {
    path: 'customer/activate',
    loadComponent: () =>
      import('./customer/activate/activate')
        .then(m => m.Activate)
  },

  // PROTECTED: only activated CUSTOMER users can access these.
  {
    path: 'customer/payments/return',
    canActivate: [roleGuard(['CUSTOMER'])],
    loadComponent: () =>
      import('./customer/payment-return/payment-return')
        .then(m => m.PaymentReturn)
  },

  {
    path: 'customer',
    canActivate: [roleGuard(['CUSTOMER'])],
    loadComponent: () =>
      import('./customer/customer-dashboard/customer-dashboard')
        .then(m => m.CustomerDashboard)
  },
  // FALLBACK
  // ==========================================================

  {
    path: '**',
    redirectTo: 'login'
  }

];
