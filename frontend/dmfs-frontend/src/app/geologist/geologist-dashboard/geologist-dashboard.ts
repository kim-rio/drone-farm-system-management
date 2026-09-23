import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { GeologistService } from '../../services/geologist.service';

interface StatCard {
  value: number;
  label: string;
  sub: string;
  icon: 'clipboard' | 'clock' | 'map' | 'file';
}

interface SurveyCard {
  id: string;
  time: string;
  title: string;
  client: string;
  location: string;
  anomalies: number;
}

@Component({
  selector: 'app-geologist-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './geologist-dashboard.html',
  styleUrl: './geologist-dashboard.scss'
})
export class GeologistDashboard implements OnInit {

  private readonly geologist = inject(GeologistService);

  stats: StatCard[] = [
    {
      value: 0,
      label: 'Pending Map Reviews',
      sub: 'Maps awaiting review',
      icon: 'clipboard'
    },
    {
      value: 0,
      label: 'Completed Surveys',
      sub: 'Surveys completed',
      icon: 'clock'
    },
    {
      value: 0,
      label: 'Approved Maps',
      sub: 'Geologically verified',
      icon: 'map'
    },
    {
      value: 0,
      label: 'AI Reports Ready',
      sub: 'Available for review',
      icon: 'file'
    }
  ];

  surveys: SurveyCard[] = [];

  ngOnInit(): void {

    this.geologist.dashboard().subscribe({
      next: dashboard => {

        this.stats = [
          {
            value: dashboard.pendingMapReviews,
            label: 'Pending Map Reviews',
            sub: 'Maps awaiting review',
            icon: 'clipboard'
          },
          {
            value: dashboard.completedSurveys,
            label: 'Completed Surveys',
            sub: 'Surveys completed',
            icon: 'clock'
          },
          {
            value: dashboard.approvedMaps,
            label: 'Approved Maps',
            sub: 'Geologically verified',
            icon: 'map'
          },
          {
            value: dashboard.aiReportsReady,
            label: 'AI Reports Ready',
            sub: 'Available for review',
            icon: 'file'
          }
        ];
      },

      error: error => {
        console.error(
          'Failed to load geologist dashboard:',
          error
        );
      }
    });

    this.geologist.maps('PENDING').subscribe({

      next: maps => {

        this.surveys = maps
          .slice(0, 6)
          .map(map => ({
            id: String(map.id),

            time: new Date(
              map.generatedAt
            ).toLocaleString(),

            title:
              map.surveyName ||
              map.mapName,

            client:
              map.companyName,

            location:
              '',

            anomalies:
              map.anomalyCount
          }));
      },

      error: error => {

        console.error(
          'Failed to load pending anomaly maps:',
          error
        );
      }
    });
  }
}