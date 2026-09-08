import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GeologistService } from '../../services/geologist.service';

interface SurveyRecord {
  id: string;
  title: string;
  client: string;
  location: string;
  status: 'Completed' | 'Awaiting review';
}

@Component({
  selector: 'app-survey-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './survey-history.html',
  styleUrl: './survey-history.scss'
})
export class SurveyHistory implements OnInit {
  private readonly geologist = inject(GeologistService);
  records: SurveyRecord[] = [];
  ngOnInit(): void { this.geologist.surveys().subscribe({ next: surveys => this.records = surveys.map(s => ({ id: s.surveyCode, title: s.surveyName, client: s.companyName, location: new Date(s.startedAt).toLocaleDateString(), status: s.status === 'COMPLETED' ? 'Completed' : 'Awaiting review' })) }); }
}
