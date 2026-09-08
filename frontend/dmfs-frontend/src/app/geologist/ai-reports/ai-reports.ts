import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GeologistService } from '../../services/geologist.service';

interface AiReport {
  id: string;
  generated: string;
}

@Component({
  selector: 'app-ai-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ai-reports.html',
  styleUrl: './ai-reports.scss'
})
export class AiReports implements OnInit {
  private readonly geologist = inject(GeologistService);
  reports: AiReport[] = [];
  ngOnInit(): void { this.geologist.reports().subscribe({ next: reports => this.reports = reports.map(r => ({ id: `${r.surveyCode} · ${r.reportName}`, generated: `Generated ${new Date(r.generatedAt).toLocaleString()}` })) }); }
}
