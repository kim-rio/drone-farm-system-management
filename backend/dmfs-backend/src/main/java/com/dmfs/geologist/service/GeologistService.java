package com.dmfs.geologist.service;

import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;

import com.dmfs.geologist.dto.*;
import com.dmfs.geologist.entity.MagneticAnomalyMap;
import com.dmfs.geologist.entity.MapReview;
import com.dmfs.geologist.entity.ProcessedMagneticData;

import com.dmfs.geologist.repository.MagneticAnomalyMapRepository;
import com.dmfs.geologist.repository.MapReviewRepository;
import com.dmfs.geologist.repository.ProcessedMagneticDataRepository;
import com.dmfs.geologist.repository.ReportRepository;

import com.dmfs.survey.entity.Surveyy;
import com.dmfs.survey.entity.SurveyStatus;
import com.dmfs.survey.entity.SurveyDataPackage;
import com.dmfs.survey.entity.SurveyDataFile;
import com.dmfs.survey.repository.SurveyyRepository;
import com.dmfs.survey.repository.SurveyDataPackageRepository;
import com.dmfs.survey.repository.SurveyDataFileRepository;
import com.dmfs.survey.service.SurveyDataStorageService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GeologistService {

    private final SurveyyRepository surveys;
    private final MagneticAnomalyMapRepository maps;
    private final MapReviewRepository reviews;
    private final ReportRepository reports;
    private final ProcessedMagneticDataRepository processedData;
    private final UserRepository users;
    private final SurveyDataPackageRepository rawDataPackages;
    private final SurveyDataFileRepository rawDataFiles;
    private final SurveyDataStorageService rawDataStorage;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public GeologistService(
            SurveyyRepository surveys,
            MagneticAnomalyMapRepository maps,
            MapReviewRepository reviews,
            ReportRepository reports,
            ProcessedMagneticDataRepository processedData,
            UserRepository users,
            SurveyDataPackageRepository rawDataPackages,
            SurveyDataFileRepository rawDataFiles,
            SurveyDataStorageService rawDataStorage
    ) {
        this.surveys = surveys;
        this.maps = maps;
        this.reviews = reviews;
        this.reports = reports;
        this.processedData = processedData;
        this.users = users;
        this.rawDataPackages = rawDataPackages;
        this.rawDataFiles = rawDataFiles;
        this.rawDataStorage = rawDataStorage;
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {

        return new DashboardResponse(
                reviews.countByDecisionIgnoreCase("PENDING"),
                surveys.countByStatus(SurveyStatus.COMPLETED),
                reviews.countByDecisionIgnoreCase("APPROVED"),
                reports.count()
        );
    }

    // =========================================================
    // SURVEY HISTORY / SURVEYS AVAILABLE FOR REVIEW
    // =========================================================

    @Transactional(readOnly = true)
    public List<SurveyResponse> surveyHistory() {

        return surveys
                .findByStatusOrderByCreatedAtDesc(
                        SurveyStatus.COMPLETED
                )
                .stream()
                .map(s -> new SurveyResponse(
                        s.getId(),
                        s.getSurveyCode(),
                        s.getSurveyName(),
                        s.getCompany().getName(),

                        /*
                         * SurveyResponse expects String,
                         * while Surveyy stores SurveyStatus.
                         */
                        s.getStatus() == null
                                ? null
                                : s.getStatus().name(),

                        s.getStartedAt(),
                        s.getEndedAt()
                ))
                .toList();
    }

    // =========================================================
    // MAP QUEUE
    //
    // Kept for compatibility, but survey is now the
    // primary review item.
    // =========================================================

    @Transactional(readOnly = true)
    public List<MapResponse> mapQueue(String status) {

        return maps.findAllWithSurvey()
                .stream()
                .map(this::toMapResponse)
                .filter(map ->
                        status == null
                                || status.isBlank()
                                || "ALL".equalsIgnoreCase(status)
                                || map.decision().equalsIgnoreCase(status)
                )
                .toList();
    }

    // =========================================================
    // GET ONE MAP
    // =========================================================

    @Transactional(readOnly = true)
    public MapResponse getMap(Long mapId) {

        MagneticAnomalyMap map =
                maps.findById(mapId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Anomaly map not found: "
                                                + mapId
                                )
                        );

        return toMapResponse(map);
    }

    // =========================================================
    // SURVEY REVIEW WORKSPACE
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, Object> getSurveyReviewData(
            Long surveyId
    ) throws IOException {

        Surveyy survey =
                surveys.findById(surveyId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Survey not found: "
                                                + surveyId
                                )
                        );

        List<MagneticAnomalyMap> surveyMaps =
                maps.findBySurvey_IdOrderByGeneratedAtDesc(
                        surveyId
                );

        MagneticAnomalyMap map =
                surveyMaps.isEmpty()
                        ? null
                        : surveyMaps.get(0);

        Map<String, Object> response =
                new HashMap<>();

        // -----------------------------------------------------
        // Survey metadata
        // -----------------------------------------------------

        Map<String, Object> surveyData =
                new HashMap<>();

        surveyData.put(
                "id",
                survey.getId()
        );

        surveyData.put(
                "surveyCode",
                survey.getSurveyCode()
        );

        surveyData.put(
                "surveyName",
                survey.getSurveyName()
        );

        surveyData.put(
                "description",
                survey.getDescription()
        );

        surveyData.put(
                "companyName",
                survey.getCompany().getName()
        );

        surveyData.put(
                "status",
                survey.getStatus()
        );

        surveyData.put(
                "startedAt",
                survey.getStartedAt()
        );

        surveyData.put(
                "endedAt",
                survey.getEndedAt()
        );

        response.put(
                "survey",
                surveyData
        );

        // -----------------------------------------------------
        // Map
        // -----------------------------------------------------

        if (map != null) {

            response.put(
                    "map",
                    toMapResponse(map)
            );

            try {

                response.put(
                        "anomalies",
                        getAnomalies(map.getId())
                );

            } catch (Exception ex) {

                response.put(
                        "anomalies",
                        emptyGeoJson()
                );
            }

        } else {

            response.put(
                    "map",
                    null
            );

            response.put(
                    "anomalies",
                    emptyGeoJson()
            );
        }

        // -----------------------------------------------------
        // Processed magnetic data
        // -----------------------------------------------------

        ProcessedMagneticData latestProcessed =
                processedData
                        .findFirstBySurvey_IdOrderByProcessedAtDesc(
                                surveyId
                        )
                        .orElse(null);

        if (latestProcessed != null) {

            Map<String, Object> processed =
                    new HashMap<>();

            processed.put(
                    "id",
                    latestProcessed.getId()
            );

            processed.put(
                    "fileName",
                    latestProcessed.getFileName()
            );

            processed.put(
                    "filePath",
                    latestProcessed.getFilePath()
            );

            processed.put(
                    "fileType",
                    latestProcessed.getFileType()
            );

            processed.put(
                    "fileSize",
                    latestProcessed.getFileSize()
            );

            processed.put(
                    "recordCount",
                    latestProcessed.getRecordCount()
            );

            processed.put(
                    "processedAt",
                    latestProcessed.getProcessedAt()
            );

            if (latestProcessed.getSurveyData() != null) {

                processed.put(
                        "surveyDataId",
                        latestProcessed
                                .getSurveyData()
                                .getId()
                );
            }

            response.put(
                    "processedData",
                    processed
            );

        } else {

            response.put(
                    "processedData",
                    null
            );
        }

        // -----------------------------------------------------
        // Raw survey data
        // -----------------------------------------------------

        response.put(
                "rawData",
                buildRawDataPayload(surveyId)
        );

        return response;
    }

    // =========================================================
    // RAW DATA PAYLOAD
    // =========================================================

    private List<Map<String, Object>> buildRawDataPayload(
            Long surveyId
    ) {

        List<SurveyDataPackage> packages =
                rawDataPackages.findBySurveyId(surveyId);

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (SurveyDataPackage pkg : packages) {

            Map<String, Object> pkgView =
                    new HashMap<>();

            pkgView.put(
                    "packageId",
                    pkg.getId()
            );

            pkgView.put(
                    "packageCode",
                    pkg.getPackageCode()
            );

            pkgView.put(
                    "status",
                    pkg.getStatus()
            );

            pkgView.put(
                    "createdAt",
                    pkg.getCreatedAt()
            );

            pkgView.put(
                    "submittedAt",
                    pkg.getSubmittedAt()
            );

            List<Map<String, Object>> fileViews =
                    new ArrayList<>();

            for (SurveyDataFile file : pkg.getFiles()) {

                Map<String, Object> fileView =
                        new HashMap<>();

                fileView.put(
                        "fileId",
                        file.getId()
                );

                fileView.put(
                        "fileName",
                        file.getFileName()
                );

                fileView.put(
                        "fileType",
                        file.getFileType()
                );

                fileView.put(
                        "fileExtension",
                        file.getFileExtension()
                );

                fileView.put(
                        "fileSize",
                        file.getFileSize()
                );

                fileView.put(
                        "validationStatus",
                        file.getValidationStatus()
                );

                fileView.put(
                        "uploadedAt",
                        file.getUploadedAt()
                );

                fileView.put(
                        "downloadUrl",
                        "/api/geologist/survey-data-files/"
                                + file.getId()
                                + "/download"
                );

                fileViews.add(fileView);
            }

            pkgView.put(
                    "files",
                    fileViews
            );

            result.add(pkgView);
        }

        return result;
    }

    // =========================================================
    // DOWNLOAD RAW FILE
    // =========================================================

    @Transactional(readOnly = true)
    public SurveyDataFile getRawDataFile(
            Long fileId
    ) {

        return rawDataFiles.findById(fileId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Survey data file not found: "
                                        + fileId
                        )
                );
    }

    public Path resolveRawDataFilePath(
            SurveyDataFile file
    ) {

        return rawDataStorage.resolve(
                file.getStorageKey()
        );
    }

    // =========================================================
    // EMPTY GEOJSON
    // =========================================================

    private Map<String, Object> emptyGeoJson() {

        Map<String, Object> geoJson =
                new HashMap<>();

        geoJson.put(
                "type",
                "FeatureCollection"
        );

        geoJson.put(
                "features",
                List.of()
        );

        return geoJson;
    }

    // =========================================================
    // ANOMALY GEOJSON
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, Object> getAnomalies(
            Long mapId
    ) throws IOException {

        MagneticAnomalyMap map =
                maps.findById(mapId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Anomaly map not found: "
                                                + mapId
                                )
                        );

        Path geoJsonPath =
                resolveGeoJsonPath(
                        map.getFilePath()
                );

        if (geoJsonPath == null
                || !Files.isRegularFile(geoJsonPath)) {

            throw new IllegalArgumentException(
                    "Anomaly GeoJSON file is unavailable for map "
                            + mapId
            );
        }

        return objectMapper.readValue(
                Files.readString(geoJsonPath),
                new TypeReference<Map<String, Object>>() {}
        );
    }

    // =========================================================
    // RESOLVE GEOJSON PATH
    // =========================================================

    private Path resolveGeoJsonPath(
            String filePath
    ) {

        if (filePath == null
                || filePath.isBlank()) {

            return null;
        }

        Path original =
                Paths.get(filePath);

        String lower =
                original.toString()
                        .toLowerCase(Locale.ROOT);

        // -----------------------------------------------------
        // 1. Already GeoJSON/JSON
        // -----------------------------------------------------

        if (lower.endsWith(".geojson")
                || lower.endsWith(".json")) {

            return Files.isRegularFile(original)
                    ? original
                    : null;
        }

        String originalString =
                original.toString();

        int dotIndex =
                originalString.lastIndexOf('.');

        // -----------------------------------------------------
        // 2. Same filename -> .geojson
        // -----------------------------------------------------

        if (dotIndex > 0) {

            Path candidate =
                    Paths.get(
                            originalString.substring(
                                    0,
                                    dotIndex
                            ) + ".geojson"
                    );

            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        // -----------------------------------------------------
        // 3. *_anomaly.* -> *_anomalies.geojson
        // -----------------------------------------------------

        String fileName =
                original.getFileName()
                        .toString();

        if (fileName.contains("_anomaly.")) {

            String geoJsonName =
                    fileName.replace(
                            "_anomaly.",
                            "_anomalies.geojson"
                    );

            Path candidate =
                    original.resolveSibling(
                            geoJsonName
                    );

            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        // -----------------------------------------------------
        // 4. Generic -> *_anomalies.geojson
        // -----------------------------------------------------

        if (dotIndex > 0) {

            Path candidate =
                    Paths.get(
                            originalString.substring(
                                    0,
                                    dotIndex
                            ) + "_anomalies.geojson"
                    );

            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    // =========================================================
    // REVIEW MAP
    // =========================================================

    @Transactional
    public MapResponse reviewMap(
            Long mapId,
            ReviewRequest request,
            String userEmail
    ) {

        MagneticAnomalyMap map =
                maps.findById(mapId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Anomaly map not found: "
                                                + mapId
                                )
                        );

        User geologist =
                users.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "Authenticated geologist "
                                                + "was not found"
                                )
                        );

        MapReview review =
                reviews
                        .findFirstByMapIdOrderByReviewedAtDesc(
                                mapId
                        )
                        .orElseGet(
                                MapReview::new
                        );

        review.setMap(map);
        review.setGeologist(geologist);

        review.setDecision(
                request.decision()
                        .toUpperCase(
                                Locale.ROOT
                        )
        );

        review.setComment(
                request.comment()
        );

        review.setReviewedAt(
                OffsetDateTime.now()
        );

        reviews.save(review);

        return toMapResponse(map);
    }

    // =========================================================
    // REPORTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ReportResponse> reportList() {

        return reports.findAllWithSurvey()
                .stream()
                .map(r -> new ReportResponse(
                        r.getId(),
                        r.getSurvey().getId(),
                        r.getSurvey().getSurveyCode(),
                        r.getReportName(),
                        r.getFilePath(),
                        r.getFileType(),
                        r.getFileSize(),
                        r.getGeneratedAt()
                ))
                .toList();
    }

    // =========================================================
    // MAP RESPONSE
    // =========================================================

    private MapResponse toMapResponse(
            MagneticAnomalyMap map
    ) {

        Long surveyId =
                map.getSurvey().getId();

        Surveyy survey =
                surveys.findById(surveyId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Survey not found: "
                                                + surveyId
                                )
                        );

        MapReview review =
                reviews
                        .findFirstByMapIdOrderByReviewedAtDesc(
                                map.getId()
                        )
                        .orElse(null);

        return new MapResponse(
                map.getId(),
                survey.getId(),
                survey.getSurveyCode(),
                survey.getSurveyName(),
                survey.getCompany().getName(),
                map.getMapName(),
                map.getFilePath(),

                map.getAnomalyCount() == null
                        ? 0
                        : map.getAnomalyCount(),

                review == null
                        ? "PENDING"
                        : review.getDecision(),

                review == null
                        ? null
                        : review.getComment(),

                map.getGeneratedAt(),

                review == null
                        ? null
                        : review.getReviewedAt()
        );
    }
}
