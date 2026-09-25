import {
  Component,
  OnInit,
  AfterViewInit,
  OnDestroy
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import * as L from 'leaflet';

import {
  AnomalyMap,
  AnomalyGeoJson,
  GeologistService,
  SurveyReviewData
} from '../../services/geologist.service';

type Status =
  | 'PENDING'
  | 'REPROCESSED'
  | 'APPROVED'
  | 'REJECTED';

/** The four filter labels the geologist sees. COMPLETED maps to the
 * backend's APPROVED decision -- there's no separate "completed" decision
 * state, approving a map *is* completing its review. */
type StatusFilterLabel = 'ALL' | 'PENDING' | 'COMPLETED' | 'REJECTED';

const FILTER_TO_BACKEND_STATUS: Record<StatusFilterLabel, string | undefined> = {
  ALL: undefined,
  PENDING: 'PENDING',
  COMPLETED: 'APPROVED',
  REJECTED: 'REJECTED'
};

interface AnomalySurvey extends AnomalyMap {
  title: string;
  client: string;
  date: string;
  status: Status;
}

interface AnomalyCandidate {
  id: number;

  easting: number;
  northing: number;

  peak_residual_nT: number;
  max_analytic_signal: number;

  area_m2: number;
  equivalent_radius_m: number;
  estimated_depth_m: number;

  pixel_x: number;
  pixel_y: number;

  interpretation_status: string;
  requires_geologist_review: boolean;
}

@Component({
  selector: 'app-anomally-map-review',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './anomally-map-review.html',
  styleUrl: './anomally-map-review.scss'
})
export class AnomallyMapReview
  implements OnInit, AfterViewInit, OnDestroy {

  surveys: AnomalySurvey[] = [];

  statusOptions: StatusFilterLabel[] = [
    'ALL',
    'PENDING',
    'COMPLETED',
    'REJECTED'
  ];

  statusFilter: StatusFilterLabel = 'ALL';

  searchTerm = '';

  selected: AnomalySurvey | null = null;

  /** Everything the detail view needs -- map, anomalies, processed + raw data. */
  reviewData: SurveyReviewData | null = null;
  reviewDataLoading = false;
  reviewDataError = '';

  reviewComment = '';
  submittingReview = false;

  // =========================================================
  // LEAFLET
  // =========================================================

  private map: L.Map | null = null;

  private anomalyLayer:
    L.LayerGroup | null = null;

  selectedCandidate:
    AnomalyCandidate | null = null;

  mapLoading = false;

  mapError = '';

  private viewReady = false;

  constructor(
    private geologist: GeologistService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {

    this.route.queryParamMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.openSurveyById(Number(id));
      } else {
        this.selected = null;
        this.reviewData = null;
        this.destroyMap();
      }
    });

    this.loadMaps();
  }

  ngAfterViewInit(): void {
    this.viewReady = true;

    if (this.selected) {
      setTimeout(() => this.initializeMap());
    }
  }

  ngOnDestroy(): void {
    this.destroyMap();
  }

  // =========================================================
  // FILTERING (search only -- status filtering happens
  // server-side via loadMaps(), see onStatusFilterChange())
  // =========================================================

  get filteredSurveys(): AnomalySurvey[] {
    const term = this.searchTerm.trim().toLowerCase();

    if (!term) {
      return this.surveys;
    }

    return this.surveys.filter((survey) =>
      survey.surveyCode.toLowerCase().includes(term) ||
      survey.companyName.toLowerCase().includes(term) ||
      survey.surveyName.toLowerCase().includes(term)
    );
  }

  onStatusFilterChange(): void {
    this.loadMaps();
  }

  // =========================================================
  // OPEN / BACK
  // =========================================================

  openSurvey(survey: AnomalySurvey): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { id: survey.surveyId }
    });
  }

  backToList(): void {
    this.reviewComment = '';
    this.selectedCandidate = null;
    this.destroyMap();

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {}
    });
  }

  // =========================================================
  // REVIEW
  // =========================================================

  approve(): void {
    this.submitReview('APPROVED');
  }

  reject(): void {
    this.submitReview('REJECTED');
  }

  // =========================================================
  // FILE URLS
  // =========================================================

  mapFileUrl(mapId: number): string {
    return this.geologist.mapFileUrl(mapId);
  }

  rawFileUrl(fileId: number): string {
    return this.geologist.rawDataFileUrl(fileId);
  }

  // =========================================================
  // LOAD MAPS (left-hand list)
  // =========================================================

  private loadMaps(): void {
    const backendStatus = FILTER_TO_BACKEND_STATUS[this.statusFilter];

    this.geologist.maps(backendStatus).subscribe({
      next: (maps) => {
        this.surveys = maps.map((map) => this.toAnomalySurvey(map));

        // keep the open detail view's summary card in sync if it's
        // still present in the freshly filtered list
        if (this.selected) {
          const stillPresent = this.surveys.find(
            (s) => s.surveyId === this.selected!.surveyId
          );
          if (stillPresent) {
            this.selected = stillPresent;
          }
        }
      },

      error: (error) => {
        console.error('Failed to load anomaly maps:', error);
      }
    });
  }

  private toAnomalySurvey(map: AnomalyMap): AnomalySurvey {
    return {
      ...map,
      title: map.surveyName || map.mapName,
      client: map.companyName,
      date: new Date(map.generatedAt).toLocaleDateString(),
      status: map.decision as Status
    };
  }

  // =========================================================
  // OPEN SURVEY BY ID (from the ?id= query param) --
  // fetches the whole review bundle in one call.
  // =========================================================

  private openSurveyById(surveyId: number): void {
    this.reviewDataLoading = true;
    this.reviewDataError = '';

    this.geologist.surveyReviewData(surveyId).subscribe({
      next: (data) => {
        this.reviewData = data;
        this.reviewDataLoading = false;

        this.selected = data.map
          ? this.toAnomalySurvey(data.map)
          : {
              // No anomaly map exists yet for this survey -- still show
              // survey metadata so the geologist knows why the map/review
              // panels are empty.
              id: 0,
              surveyId: data.survey.id,
              surveyCode: data.survey.surveyCode,
              surveyName: data.survey.surveyName,
              companyName: data.survey.companyName,
              mapName: '',
              filePath: '',
              anomalyCount: 0,
              decision: 'PENDING',
              reviewComment: undefined,
              generatedAt: data.survey.startedAt,
              reviewedAt: undefined,
              title: data.survey.surveyName,
              client: data.survey.companyName,
              date: new Date(data.survey.startedAt).toLocaleDateString(),
              status: 'PENDING'
            };

        this.reviewComment = data.map?.reviewComment ?? '';

        if (this.viewReady) {
          setTimeout(() => this.initializeMap());
        }
      },

      error: (error) => {
        console.error('Failed to load survey review data:', error);
        this.reviewDataLoading = false;
        this.reviewDataError = 'Unable to load this survey.';
      }
    });
  }

  // =========================================================
  // INITIALIZE LEAFLET
  // =========================================================

  private initializeMap(): void {
    if (!this.selected) {
      return;
    }

    const element = document.getElementById('anomaly-map');
    if (!element) {
      return;
    }

    this.destroyMap();

    this.mapLoading = true;
    this.mapError = '';

    this.map = L.map(element, { zoomControl: true });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(this.map);

    this.anomalyLayer = L.layerGroup().addTo(this.map);

    if (this.reviewData?.anomalies) {
      this.renderAnomalies(this.reviewData.anomalies);
      this.mapLoading = false;
    } else {
      this.mapLoading = false;
      this.map.setView([0, 0], 2);
    }
  }

  // =========================================================
  // RENDER ANOMALIES
  // =========================================================

  private renderAnomalies(geojson: AnomalyGeoJson): void {
    if (!this.map || !this.anomalyLayer) {
      return;
    }

    this.anomalyLayer.clearLayers();

    const bounds = L.latLngBounds([]);

    for (const feature of geojson.features) {
      const [longitude, latitude] = feature.geometry.coordinates;
      const candidate = feature.properties as unknown as AnomalyCandidate;

      const marker = L.circleMarker([latitude, longitude], {
        radius: 8,
        color: '#000000',
        weight: 2,
        fillColor: '#16a34a',
        fillOpacity: 0.9
      });

      marker.bindPopup(this.createCandidatePopup(candidate));

      marker.on('click', () => {
        this.selectedCandidate = candidate;
      });

      marker.addTo(this.anomalyLayer);
      bounds.extend([latitude, longitude]);
    }

    if (geojson.features.length > 0 && bounds.isValid()) {
      this.map.fitBounds(bounds, { padding: [40, 40] });
    } else {
      this.map.setView([0, 0], 2);
    }
  }

  // =========================================================
  // POPUP
  // =========================================================

  private createCandidatePopup(candidate: AnomalyCandidate): string {
    return `
      <div style="min-width: 190px; font-family: Arial, sans-serif; color: #000;">
        <div style="font-size: 13px; font-weight: 700; margin-bottom: 8px;">
          Anomaly Candidate #${candidate.id}
        </div>
        <div style="font-size: 12px; margin-bottom: 4px;">
          Peak residual: <strong>${candidate.peak_residual_nT?.toFixed(2)} nT</strong>
        </div>
        <div style="font-size: 12px; margin-bottom: 4px;">
          Analytic signal: <strong>${candidate.max_analytic_signal?.toFixed(2)}</strong>
        </div>
        <div style="font-size: 12px; margin-bottom: 4px;">
          Area: <strong>${candidate.area_m2?.toFixed(2)} m²</strong>
        </div>
        <div style="font-size: 12px; margin-bottom: 4px;">
          Estimated depth: <strong>${candidate.estimated_depth_m?.toFixed(2)} m</strong>
        </div>
      </div>
    `;
  }

  // =========================================================
  // DESTROY MAP
  // =========================================================

  private destroyMap(): void {
    if (this.map) {
      this.map.remove();
      this.map = null;
    }

    this.anomalyLayer = null;
    this.selectedCandidate = null;
  }

  // =========================================================
  // REVIEW SUBMISSION
  // =========================================================

  private submitReview(decision: 'APPROVED' | 'REJECTED'): void {
    if (!this.selected || !this.selected.id) {
      return;
    }

    this.submittingReview = true;

    this.geologist.review(this.selected.id, decision, this.reviewComment).subscribe({
      next: (updated) => {
        this.submittingReview = false;

        const survey = this.toAnomalySurvey(updated);

        const index = this.surveys.findIndex((s) => s.id === updated.id);
        if (index >= 0) {
          this.surveys[index] = survey;
        }

        this.selected = survey;

        if (this.reviewData) {
          this.reviewData = { ...this.reviewData, map: updated };
        }
      },

      error: (error) => {
        console.error('Failed to submit review:', error);
        this.submittingReview = false;
      }
    });
  }
}
