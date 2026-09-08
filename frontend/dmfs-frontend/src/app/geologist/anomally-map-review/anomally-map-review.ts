import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AnomalyMap, GeologistService } from '../../services/geologist.service';

type Status = 'PENDING' | 'REPROCESSED' | 'APPROVED' | 'REJECTED';

interface AnomalySurvey extends AnomalyMap {
  title: string;
  client: string;
  date: string;
  status: Status;
}

@Component({
  selector: 'app-anomally-map-review',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './anomally-map-review.html',
  styleUrl: './anomally-map-review.scss'
})
export class AnomallyMapReview implements OnInit {
  private readonly geologist = inject(GeologistService);
  surveys: AnomalySurvey[] = [];

  statusOptions: Array<'ALL' | Status> = ['ALL', 'PENDING', 'REPROCESSED', 'APPROVED', 'REJECTED'];
  statusFilter: 'ALL' | Status = 'ALL';
  searchTerm = '';

  selected: AnomalySurvey | null = null;
  reviewComment = '';

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const id = params.get('id');
      this.selected = id ? this.surveys.find((s) => s.id === Number(id)) ?? null : null;
    });
    this.loadMaps();
  }

  get filteredSurveys(): AnomalySurvey[] {
    const term = this.searchTerm.trim().toLowerCase();
    return this.surveys.filter((s) => {
      const matchesStatus = this.statusFilter === 'ALL' || s.status === this.statusFilter;
      const matchesTerm =
        !term ||
        s.surveyCode.toLowerCase().includes(term) ||
        s.companyName.toLowerCase().includes(term) ||
        s.surveyName.toLowerCase().includes(term);
      return matchesStatus && matchesTerm;
    });
  }

  openSurvey(survey: AnomalySurvey): void {
    this.router.navigate([], { relativeTo: this.route, queryParams: { id: survey.id } });
  }

  backToList(): void {
    this.reviewComment = '';
    this.router.navigate([], { relativeTo: this.route, queryParams: {} });
  }

  approve(): void {
    this.submitReview('APPROVED');
  }

  reject(): void {
    this.submitReview('REJECTED');
  }

  mapFileUrl(mapId: number): string { return this.geologist.mapFileUrl(mapId); }

  private loadMaps(): void {
    this.geologist.maps().subscribe({ next: maps => {
      this.surveys = maps.map(map => ({ ...map, title: map.surveyName || map.mapName, client: map.companyName, date: new Date(map.generatedAt).toLocaleDateString(), status: map.decision as Status }));
      const id = this.route.snapshot.queryParamMap.get('id');
      this.selected = id ? this.surveys.find(s => s.id === Number(id)) ?? null : null;
    }});
  }

  private submitReview(decision: 'APPROVED' | 'REJECTED'): void {
    if (!this.selected) return;
    this.geologist.review(this.selected.id, decision, this.reviewComment).subscribe({ next: updated => {
      const index = this.surveys.findIndex(s => s.id === updated.id);
      const survey = { ...updated, title: updated.surveyName || updated.mapName, client: updated.companyName, date: new Date(updated.generatedAt).toLocaleDateString(), status: updated.decision as Status };
      if (index >= 0) this.surveys[index] = survey;
      this.selected = survey;
    }});
  }
}
