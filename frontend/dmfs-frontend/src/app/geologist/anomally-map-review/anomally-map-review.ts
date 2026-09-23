import {
  Component,
  OnInit,
  AfterViewInit,
  OnDestroy,
  inject
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
  GeologistService
} from '../../services/geologist.service';

type Status =
  | 'PENDING'
  | 'REPROCESSED'
  | 'APPROVED'
  | 'REJECTED';

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

interface AnomalyGeoJsonFeature {
  type: 'Feature';

  geometry: {
    type: 'Point';
    coordinates: [number, number];
  };

  properties: AnomalyCandidate;
}

interface AnomalyGeoJson {
  type: 'FeatureCollection';

  features: AnomalyGeoJsonFeature[];
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

  private readonly geologist =
    inject(GeologistService);

  surveys: AnomalySurvey[] = [];

  statusOptions:
    Array<'ALL' | Status> = [
      'ALL',
      'PENDING',
      'REPROCESSED',
      'APPROVED',
      'REJECTED'
    ];

  statusFilter:
    'ALL' | Status = 'ALL';

  searchTerm = '';

  selected:
    AnomalySurvey | null = null;

  reviewComment = '';

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

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {

    this.route.queryParamMap.subscribe(
      (params) => {

        const id =
          params.get('id');

        this.selected =
          id
            ? this.surveys.find(
                survey =>
                  survey.id === Number(id)
              ) ?? null
            : null;

        if (
          this.selected &&
          this.viewReady
        ) {

          setTimeout(() => {
            this.initializeMap();
          });
        }
      }
    );

    this.loadMaps();
  }

  ngAfterViewInit(): void {

    this.viewReady = true;

    if (this.selected) {

      setTimeout(() => {
        this.initializeMap();
      });
    }
  }

  ngOnDestroy(): void {

    if (this.map) {

      this.map.remove();

      this.map = null;
    }
  }

  // =========================================================
  // FILTERING
  // =========================================================

  get filteredSurveys():
    AnomalySurvey[] {

    const term =
      this.searchTerm
        .trim()
        .toLowerCase();

    return this.surveys.filter(
      survey => {

        const matchesStatus =
          this.statusFilter === 'ALL'
          ||
          survey.status ===
            this.statusFilter;

        const matchesTerm =
          !term
          ||
          survey.surveyCode
            .toLowerCase()
            .includes(term)
          ||
          survey.companyName
            .toLowerCase()
            .includes(term)
          ||
          survey.surveyName
            .toLowerCase()
            .includes(term);

        return (
          matchesStatus
          &&
          matchesTerm
        );
      }
    );
  }

  // =========================================================
  // OPEN SURVEY
  // =========================================================

  openSurvey(
    survey: AnomalySurvey
  ): void {

    this.router.navigate(
      [],
      {
        relativeTo: this.route,
        queryParams: {
          id: survey.id
        }
      }
    );
  }

  // =========================================================
  // BACK
  // =========================================================

  backToList(): void {

    this.reviewComment = '';

    this.selectedCandidate = null;

    this.destroyMap();

    this.router.navigate(
      [],
      {
        relativeTo: this.route,
        queryParams: {}
      }
    );
  }

  // =========================================================
  // REVIEW
  // =========================================================

  approve(): void {

    this.submitReview(
      'APPROVED'
    );
  }

  reject(): void {

    this.submitReview(
      'REJECTED'
    );
  }

  // =========================================================
  // MAP IMAGE URL
  // =========================================================

  mapFileUrl(
    mapId: number
  ): string {

    return this.geologist.mapFileUrl(
      mapId
    );
  }

  // =========================================================
  // LOAD MAPS
  // =========================================================

  private loadMaps(): void {

    this.geologist.maps()
      .subscribe({

        next: maps => {

          this.surveys =
            maps.map(
              map => ({

                ...map,

                title:
                  map.surveyName
                  ||
                  map.mapName,

                client:
                  map.companyName,

                date:
                  new Date(
                    map.generatedAt
                  ).toLocaleDateString(),

                status:
                  map.decision as Status

              })
            );

          const id =
            this.route
              .snapshot
              .queryParamMap
              .get('id');

          this.selected =
            id
              ? this.surveys.find(
                  survey =>
                    survey.id ===
                    Number(id)
                ) ?? null
              : null;

          if (
            this.selected &&
            this.viewReady
          ) {

            setTimeout(() => {
              this.initializeMap();
            });
          }
        },

        error: error => {

          console.error(
            'Failed to load anomaly maps:',
            error
          );
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

    const element =
      document.getElementById(
        'anomaly-map'
      );

    if (!element) {
      return;
    }

    this.destroyMap();

    this.mapLoading = true;

    this.mapError = '';

    // -------------------------------------------------------
    // Create map
    // -------------------------------------------------------

    this.map =
      L.map(
        element,
        {
          zoomControl: true
        }
      );

    // -------------------------------------------------------
    // OpenStreetMap base layer
    // -------------------------------------------------------

    L.tileLayer(
      'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        attribution:
          '&copy; OpenStreetMap contributors',

        maxZoom: 19
      }
    ).addTo(
      this.map
    );

    // -------------------------------------------------------
    // Empty layer for anomaly candidates
    // -------------------------------------------------------

    this.anomalyLayer =
      L.layerGroup().addTo(
        this.map
      );

    // -------------------------------------------------------
    // Load anomaly GeoJSON
    // -------------------------------------------------------

    this.loadAnomalyGeoJson();
  }

  // =========================================================
  // LOAD GEOJSON
  // =========================================================

  private loadAnomalyGeoJson(): void {

    if (
      !this.map ||
      !this.selected
    ) {
      return;
    }

    this.mapLoading = true;

    /*
     * Temporary development URL.
     *
     * This will be replaced with the Spring Boot
     * geologist GeoJSON endpoint once that endpoint
     * is connected to the generated anomaly product.
     */

    const url =
      this.anomalyGeoJsonUrl(
        this.selected.id
      );

    fetch(url)
      .then(response => {

        if (!response.ok) {

          throw new Error(
            `Unable to load anomaly GeoJSON (${response.status})`
          );
        }

        return response.json();
      })
      .then(
        (geojson: AnomalyGeoJson) => {

          this.renderAnomalies(
            geojson
          );

          this.mapLoading = false;
        }
      )
      .catch(error => {

        console.error(
          'Anomaly GeoJSON error:',
          error
        );

        this.mapLoading = false;

        this.mapError =
          'Unable to load anomaly candidates.';
      });
  }

  // =========================================================
  // GEOJSON URL
  // =========================================================

  private anomalyGeoJsonUrl(
    mapId: number
  ): string {

    /*
     * This endpoint is the next backend connection.
     *
     * Expected final endpoint:
     *
     * GET /api/geologist/maps/{mapId}/anomalies
     *
     * It should return the GeoJSON generated by
     * anomaly.py.
     */

    return `/api/geologist/maps/${mapId}/anomalies`;
  }

  // =========================================================
  // RENDER ANOMALIES
  // =========================================================

  private renderAnomalies(
    geojson: AnomalyGeoJson
  ): void {

    if (
      !this.map ||
      !this.anomalyLayer
    ) {
      return;
    }

    this.anomalyLayer.clearLayers();

    const bounds =
      L.latLngBounds([]);

    for (
      const feature
      of geojson.features
    ) {

      const [
        longitude,
        latitude
      ] = feature.geometry.coordinates;

      const candidate =
        feature.properties;

      // -----------------------------------------------------
      // Green anomaly marker
      // -----------------------------------------------------

      const marker =
        L.circleMarker(
          [
            latitude,
            longitude
          ],
          {
            radius: 8,

            color: '#000000',

            weight: 2,

            fillColor: '#16a34a',

            fillOpacity: 0.9
          }
        );

      // -----------------------------------------------------
      // Candidate popup
      // -----------------------------------------------------

      marker.bindPopup(
        this.createCandidatePopup(
          candidate
        )
      );

      // -----------------------------------------------------
      // Candidate click
      // -----------------------------------------------------

      marker.on(
        'click',
        () => {

          this.selectedCandidate =
            candidate;
        }
      );

      marker.addTo(
        this.anomalyLayer
      );

      bounds.extend(
        [
          latitude,
          longitude
        ]
      );
    }

    // -------------------------------------------------------
    // Zoom map to anomalies
    // -------------------------------------------------------

    if (
      geojson.features.length > 0
      &&
      bounds.isValid()
    ) {

      this.map.fitBounds(
        bounds,
        {
          padding: [
            40,
            40
          ]
        }
      );

    } else {

      // Default world view if there
      // are no candidates.

      this.map.setView(
        [
          0,
          0
        ],
        2
      );
    }
  }

  // =========================================================
  // POPUP
  // =========================================================

  private createCandidatePopup(
    candidate: AnomalyCandidate
  ): string {

    return `
      <div style="
        min-width: 190px;
        font-family: Arial, sans-serif;
        color: #000;
      ">

        <div style="
          font-size: 13px;
          font-weight: 700;
          margin-bottom: 8px;
        ">
          Anomaly Candidate #${candidate.id}
        </div>

        <div style="
          font-size: 12px;
          margin-bottom: 4px;
        ">
          Peak residual:
          <strong>
            ${candidate.peak_residual_nT.toFixed(2)} nT
          </strong>
        </div>

        <div style="
          font-size: 12px;
          margin-bottom: 4px;
        ">
          Analytic signal:
          <strong>
            ${candidate.max_analytic_signal.toFixed(2)}
          </strong>
        </div>

        <div style="
          font-size: 12px;
          margin-bottom: 4px;
        ">
          Area:
          <strong>
            ${candidate.area_m2.toFixed(2)} m²
          </strong>
        </div>

        <div style="
          font-size: 12px;
          margin-bottom: 4px;
        ">
          Estimated depth:
          <strong>
            ${candidate.estimated_depth_m.toFixed(2)} m
          </strong>
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

  private submitReview(
    decision:
      'APPROVED'
      | 'REJECTED'
  ): void {

    if (!this.selected) {
      return;
    }

    this.geologist
      .review(
        this.selected.id,
        decision,
        this.reviewComment
      )
      .subscribe({

        next: updated => {

          const index =
            this.surveys.findIndex(
              survey =>
                survey.id ===
                updated.id
            );

          const survey =
            {
              ...updated,

              title:
                updated.surveyName
                ||
                updated.mapName,

              client:
                updated.companyName,

              date:
                new Date(
                  updated.generatedAt
                ).toLocaleDateString(),

              status:
                updated.decision as Status
            };

          if (index >= 0) {

            this.surveys[index] =
              survey;
          }

          this.selected =
            survey;

          this.reviewComment = '';

          this.initializeMap();
        },

        error: error => {

          console.error(
            'Failed to submit review:',
            error
          );
        }
      });
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}
}