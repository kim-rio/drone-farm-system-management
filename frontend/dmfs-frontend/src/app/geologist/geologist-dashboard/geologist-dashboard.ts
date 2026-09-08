import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { GeologistService } from '../../services/geologist.service';

interface StatCard {
  value: number;
  label: string;
  sub: string;
  tone: 'copper' | 'ochre' | 'teal' | 'plum';
  icon: 'clipboard' | 'clock' | 'map' | 'file';
}

interface SurveyCard {
  id: string;
  time: string;
  title: string;
  client: string;
  location: string;
  anomalies: number;
  tone: 'danger' | 'ochre' | 'teal';
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
    { value: 6, label: 'Pending Map Reviews', sub: 'Maps awaiting review', tone: 'copper', icon: 'clipboard' },
    { value: 28, label: 'Completed Surveys', sub: 'Surveys completed', tone: 'ochre', icon: 'clock' },
    { value: 18, label: 'Approved Maps', sub: 'Geologically verified', tone: 'teal', icon: 'map' },
    { value: 12, label: 'AI Reports Ready', sub: 'Available for review', tone: 'plum', icon: 'file' }
  ];

  surveys: SurveyCard[] = [];
  ngOnInit(): void {
    this.geologist.dashboard().subscribe({ next: d => this.stats = [
      { value: d.pendingMapReviews, label: 'Pending Map Reviews', sub: 'Maps awaiting review', tone: 'copper', icon: 'clipboard' },
      { value: d.completedSurveys, label: 'Completed Surveys', sub: 'Surveys completed', tone: 'ochre', icon: 'clock' },
      { value: d.approvedMaps, label: 'Approved Maps', sub: 'Geologically verified', tone: 'teal', icon: 'map' },
      { value: d.aiReportsReady, label: 'AI Reports Ready', sub: 'Available for review', tone: 'plum', icon: 'file' }
    ] });
    this.geologist.maps('PENDING').subscribe({ next: maps => this.surveys = maps.slice(0, 6).map((m, index) => ({ id: String(m.id), time: new Date(m.generatedAt).toLocaleString(), title: m.surveyName || m.mapName, client: m.companyName, location: '', anomalies: m.anomalyCount, tone: index % 3 === 0 ? 'danger' : index % 3 === 1 ? 'ochre' : 'teal' })) });
  }
}
