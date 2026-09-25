package com.dmfs.geologist.controller;

import com.dmfs.geologist.dto.*;
import com.dmfs.geologist.entity.MagneticAnomalyMap;
import com.dmfs.geologist.repository.MagneticAnomalyMapRepository;
import com.dmfs.geologist.service.GeologistService;
import com.dmfs.survey.entity.SurveyDataFile;

import jakarta.validation.Valid;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/geologist")
@PreAuthorize("hasRole('GEOLOGIST')")
public class GeologistController {

    private final GeologistService service;
    private final MagneticAnomalyMapRepository maps;

    public GeologistController(
            GeologistService service,
            MagneticAnomalyMapRepository maps
    ) {
        this.service = service;
        this.maps = maps;
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return service.dashboard();
    }

    // =========================================================
    // SURVEYS
    // =========================================================

    @GetMapping("/surveys")
    public List<SurveyResponse> surveys() {
        return service.surveyHistory();
    }

    // =========================================================
    // SURVEY REVIEW WORKSPACE
    // =========================================================

    @GetMapping("/surveys/{surveyId}/review-data")
    public Map<String, Object> surveyReviewData(
            @PathVariable Long surveyId
    ) throws IOException {

        return service.getSurveyReviewData(
                surveyId
        );
    }

    // =========================================================
    // MAPS
    // =========================================================

    @GetMapping("/maps")
    public List<MapResponse> maps(
            @RequestParam(required = false) String status
    ) {
        return service.mapQueue(status);
    }

    @GetMapping("/maps/{mapId}")
    public MapResponse map(
            @PathVariable Long mapId
    ) {
        return service.getMap(mapId);
    }

    // =========================================================
    // ANOMALIES
    // =========================================================

    @GetMapping("/maps/{mapId}/anomalies")
    public ResponseEntity<Map<String, Object>> anomalies(
            @PathVariable Long mapId
    ) throws IOException {

        return ResponseEntity.ok(
                service.getAnomalies(mapId)
        );
    }

    // =========================================================
    // REVIEW
    // =========================================================

    @PostMapping("/maps/{mapId}/review")
    public MapResponse review(
            @PathVariable Long mapId,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication
    ) {

        return service.reviewMap(
                mapId,
                request,
                authentication.getName()
        );
    }

    // =========================================================
    // REPORTS
    // =========================================================

    @GetMapping("/reports")
    public List<ReportResponse> reports() {
        return service.reportList();
    }

    // =========================================================
    // MAP FILE
    // =========================================================

    @GetMapping("/maps/{mapId}/file")
    public ResponseEntity<InputStreamResource> mapFile(
            @PathVariable Long mapId
    ) throws IOException {

        MagneticAnomalyMap map =
                maps.findById(mapId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Anomaly map not found: "
                                                + mapId
                                )
                        );

        Path path =
                Paths.get(
                        map.getFilePath()
                );

        if (!Files.isRegularFile(path)) {

            throw new IllegalArgumentException(
                    "Anomaly map file is unavailable"
            );
        }

        MediaType mediaType =
                MediaType.APPLICATION_OCTET_STREAM;

        if (map.getFileType() != null &&
                !map.getFileType().isBlank()) {

            try {

                mediaType =
                        MediaType.parseMediaType(
                                map.getFileType()
                        );

            } catch (IllegalArgumentException ignored) {
                // Keep application/octet-stream
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(
                        new InputStreamResource(
                                Files.newInputStream(path)
                        )
                );
    }

    // =========================================================
    // RAW SURVEY DATA FILE (operator's original upload)
    // =========================================================

    @GetMapping("/survey-data-files/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadRawFile(
            @PathVariable Long fileId
    ) throws IOException {

        SurveyDataFile file = service.getRawDataFile(fileId);
        Path path = service.resolveRawDataFilePath(file);

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(
                    "Raw survey data file is unavailable"
            );
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\""
                )
                .body(
                        new InputStreamResource(
                                Files.newInputStream(path)
                        )
                );
    }
}