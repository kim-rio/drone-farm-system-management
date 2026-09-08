import { Routes } from '@angular/router';
import { Layout } from './layout/layout';


export const GEOLOGIST_ROUTES: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./geologist-dashboard/geologist-dashboard').then(
            (m) => m.GeologistDashboard
          )
      },
      {
        path: 'survey-history',
        loadComponent: () =>
          import('./survey-history/survey-history').then(
            (m) => m.SurveyHistory
          )
      },
      {
        path: 'anomaly-map-review',
        loadComponent: () =>
          import('./anomally-map-review/anomally-map-review').then(
            (m) => m.AnomallyMapReview
          )
      },
      {
        path: 'ai-reports',
        loadComponent: () =>
          import('./ai-reports/ai-reports').then(
            (m) => m.AiReports
          )
      }
    ]
  }
];
