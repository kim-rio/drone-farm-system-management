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

  {
    path: 'management/clients',
    canActivate: [roleGuard(['MANAGEMENT'])],
    loadComponent: () =>
      import('./management/clients/client-list/client-list')
        .then(m => m.ClientList)
  },

  {
    path: 'management/clients/register',
    canActivate: [roleGuard(['MANAGEMENT'])],
    loadComponent: () =>
      import('./management/clients/register-clients/register-clients')
        .then(m => m.RegisterClients)
  },

  {
    path: 'management/clients/:clientId/farms/:farmId',
    canActivate: [roleGuard(['MANAGEMENT'])],
    loadComponent: () =>
      import('./management/clients/client-details/farm-details/farm-details')
        .then(m => m.FarmDetails)
  },

  {
    path: 'management/clients/:id',
    canActivate: [roleGuard(['MANAGEMENT'])],
    loadComponent: () =>
      import('./management/clients/client-details/client-details')
        .then(m => m.ClientDetails)
  },


  { path: 'management/farms', component: FarmList, canActivate: [roleGuard(['MANAGEMENT'])] },

    {
    path: 'management/service-requests',
    canActivate: [roleGuard(['MANAGEMENT'])],
    loadComponent: () =>
      import('./management/service-requests/service-request-list/service-request-list')
        .then(m => m.ServiceRequestList)
  },

  {
    path: 'management/service-requests/new',
    canActivate: [roleGuard(['MANAGEMENT'])],
    loadComponent: () =>
      import('./management/service-requests/create-service-request/create-service-request')
        .then(m => m.CreateServiceRequest)
  },

  {
    path: 'management/service-requests/:id',
    canActivate: [roleGuard(['MANAGEMENT'])],
    loadComponent: () =>
      import('./management/service-requests/service-request-details/service-request-details')
        .then(m => m.ServiceRequestDetails)
  },
  {
  path: 'management/missions',
  canActivate: [roleGuard(['MANAGEMENT'])],
  loadComponent: () =>
    import('./management/missions/mission-list/mission-list')
      .then(m => m.MissionList)
},

{
  path: 'management/missions/new',
  canActivate: [roleGuard(['MANAGEMENT'])],
  loadComponent: () =>
    import('./management/missions/create-mission/create-mission')
      .then(m => m.CreateMission)
},

{
  path: 'management/missions/:id',
  canActivate: [roleGuard(['MANAGEMENT'])],
  loadComponent: () =>
    import('./management/missions/mission-details/mission-details')
      .then(m => m.MissionDetails)
},


  // ==========================================================
  // DRONE OPERATOR
  // ==========================================================

  {
    path: 'drone-operator',
    pathMatch: 'full',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    loadComponent: () => import('./drone-operator/operator-workspace').then(m => m.OperatorWorkspace),
    data: { page: 'dashboard' }
  },
  {
    path: 'drone-operator/missions',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    loadComponent: () => import('./drone-operator/operator-workspace').then(m => m.OperatorWorkspace),
    data: { page: 'missions' }
  },
  {
    path: 'drone-operator/missions/:id',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    loadComponent: () => import('./drone-operator/operator-workspace').then(m => m.OperatorWorkspace),
    data: { page: 'detail' }
  },
  {
    path: 'drone-operator/surveys',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    loadComponent: () => import('./drone-operator/operator-workspace').then(m => m.OperatorWorkspace),
    data: { page: 'surveys' }
  },
  {
    path: 'drone-operator/data',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    loadComponent: () => import('./drone-operator/operator-workspace').then(m => m.OperatorWorkspace),
    data: { page: 'data' }
  },

  {
    path: 'drone-operator',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    component: RoleWorkspace,
    data: {
      role: 'DRONE_OPERATOR',
      title: 'Drone Operations Dashboard',
      subtitle: 'Manage assigned field operations, drones and survey activities.',
      menuItems: [
        {
          label: 'Dashboard',
          description: 'Overview of your operational workload.',
          icon: '�',
          route: '/drone-operator'
        },
        {
          label: 'Operations',
          description: 'View and manage assigned field operations.',
          icon: 'O',
          route: '/drone-operator/operations'
        },
        {
          label: 'Drones',
          description: 'View available and assigned drones.',
          icon: 'D',
          route: '/drone-operator/drones'
        },
        {
          label: 'Surveys',
          description: 'Manage survey execution and status.',
          icon: 'S',
          route: '/drone-operator/surveys'
        },
        {
          label: 'Survey Data',
          description: 'Upload and manage collected survey data.',
          icon: '?',
          route: '/drone-operator/survey-data'
        }
      ]
    }
  },

  {
    path: 'drone-operator/operations',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    loadComponent: () => import('./drone-operator/operations/operations').then(m => m.OperationsComponent)
  },
  {
    path: 'drone-operator/surveys',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    loadComponent: () => import('./drone-operator/surveys/surveys').then(m => m.SurveysComponent)
  },
  {
    path: 'drone-operator/survey-data',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    loadComponent: () => import('./drone-operator/survey-data/survey-data').then(m => m.SurveyDataComponent)
  },

  {
    path: 'drone-operator/operations',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    component: RoleWorkspace,
    data: {
      role: 'DRONE_OPERATOR',
      title: 'Field Operations',
      subtitle: 'Review and manage assigned operational work.',
      menuItems: [
        {
          label: 'Dashboard',
          description: 'Return to your dashboard.',
          icon: '�',
          route: '/drone-operator'
        },
        {
          label: 'Operations',
          description: 'Assigned field operations.',
          icon: 'O',
          route: '/drone-operator/operations'
        },
        {
          label: 'Drones',
          description: 'Available drones.',
          icon: 'D',
          route: '/drone-operator/drones'
        },
        {
          label: 'Surveys',
          description: 'Survey execution.',
          icon: 'S',
          route: '/drone-operator/surveys'
        }
      ]
    }
  },

  {
    path: 'drone-operator/drones',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    component: RoleWorkspace,
    data: {
      role: 'DRONE_OPERATOR',
      title: 'Drone Management',
      subtitle: 'View and manage drones assigned to field operations.',
      menuItems: [
        {
          label: 'Dashboard',
          description: 'Return to dashboard.',
          icon: '�',
          route: '/drone-operator'
        },
        {
          label: 'Operations',
          description: 'Assigned operations.',
          icon: 'O',
          route: '/drone-operator/operations'
        },
        {
          label: 'Drones',
          description: 'Manage operational drones.',
          icon: 'D',
          route: '/drone-operator/drones'
        },
        {
          label: 'Surveys',
          description: 'Manage surveys.',
          icon: 'S',
          route: '/drone-operator/surveys'
        }
      ]
    }
  },

  {
    path: 'drone-operator/surveys',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    component: RoleWorkspace,
    data: {
      role: 'DRONE_OPERATOR',
      title: 'Survey Operations',
      subtitle: 'Execute surveys and update field survey progress.',
      menuItems: [
        {
          label: 'Dashboard',
          description: 'Return to dashboard.',
          icon: '�',
          route: '/drone-operator'
        },
        {
          label: 'Operations',
          description: 'Assigned operations.',
          icon: 'O',
          route: '/drone-operator/operations'
        },
        {
          label: 'Drones',
          description: 'Operational drones.',
          icon: 'D',
          route: '/drone-operator/drones'
        },
        {
          label: 'Surveys',
          description: 'Execute surveys.',
          icon: 'S',
          route: '/drone-operator/surveys'
        },
        {
          label: 'Survey Data',
          description: 'Manage collected data.',
          icon: '?',
          route: '/drone-operator/survey-data'
        }
      ]
    }
  },

  {
    path: 'drone-operator/survey-data',
    canActivate: [roleGuard(['DRONE_OPERATOR'])],
    component: RoleWorkspace,
    data: {
      role: 'DRONE_OPERATOR',
      title: 'Survey Data',
      subtitle: 'Upload and manage data collected during field operations.',
      menuItems: [
        {
          label: 'Dashboard',
          description: 'Return to dashboard.',
          icon: '�',
          route: '/drone-operator'
        },
        {
          label: 'Operations',
          description: 'Assigned operations.',
          icon: 'O',
          route: '/drone-operator/operations'
        },
        {
          label: 'Surveys',
          description: 'Survey execution.',
          icon: 'S',
          route: '/drone-operator/surveys'
        },
        {
          label: 'Survey Data',
          description: 'Collected survey data.',
          icon: '?',
          route: '/drone-operator/survey-data'
        }
      ]
    }
  },

  {
  path: 'geologist',
  canActivate: [roleGuard(['GEOLOGIST'])],
  loadChildren: () =>
    import('./geologist/geologist.routes')
      .then(m => m.GEOLOGIST_ROUTES)
  },
  


  // ==========================================================
  // FALLBACK
  // ==========================================================

  {
    path: '**',
    redirectTo: 'login'
  }

];

