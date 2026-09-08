package com.dmfs.geologist.service;

import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.geologist.dto.*;
import com.dmfs.geologist.entity.*;
import com.dmfs.geologist.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class GeologistService {
    private final SurveyRepository surveys;
    private final MagneticAnomalyMapRepository maps;
    private final MapReviewRepository reviews;
    private final ReportRepository reports;
    private final UserRepository users;

    public GeologistService(SurveyRepository surveys, MagneticAnomalyMapRepository maps, MapReviewRepository reviews, ReportRepository reports, UserRepository users) {
        this.surveys = surveys; this.maps = maps; this.reviews = reviews; this.reports = reports; this.users = users;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        return new DashboardResponse(reviews.countByDecisionIgnoreCase("PENDING"), surveys.countByStatusIgnoreCase("COMPLETED"), reviews.countByDecisionIgnoreCase("APPROVED"), reports.count());
    }

    @Transactional(readOnly = true)
    public List<SurveyResponse> surveyHistory() {
        return surveys.findAllWithCompany().stream().map(s -> new SurveyResponse(s.getId(), s.getSurveyCode(), s.getSurveyName(), s.getCompany().getName(), s.getStatus(), s.getStartedAt(), s.getEndedAt())).toList();
    }

    @Transactional(readOnly = true)
    public List<MapResponse> mapQueue(String status) {
        return maps.findAllWithSurvey().stream().map(this::toMapResponse)
                .filter(map -> status == null || status.isBlank() || "ALL".equalsIgnoreCase(status) || map.decision().equalsIgnoreCase(status))
                .toList();
    }

    @Transactional
    public MapResponse reviewMap(Long mapId, ReviewRequest request, String userEmail) {
        MagneticAnomalyMap map = maps.findById(mapId).orElseThrow(() -> new IllegalArgumentException("Anomaly map not found: " + mapId));
        User geologist = users.findByEmail(userEmail).orElseThrow(() -> new AccessDeniedException("Authenticated geologist was not found"));
        MapReview review = reviews.findFirstByMapIdOrderByReviewedAtDesc(mapId).orElseGet(MapReview::new);
        review.setMap(map); review.setGeologist(geologist);
        review.setDecision(request.decision().toUpperCase(Locale.ROOT));
        review.setComment(request.comment()); review.setReviewedAt(OffsetDateTime.now());
        reviews.save(review);
        return toMapResponse(map);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> reportList() {
        return reports.findAllWithSurvey().stream().map(r -> new ReportResponse(r.getId(), r.getSurvey().getId(), r.getSurvey().getSurveyCode(), r.getReportName(), r.getFilePath(), r.getFileType(), r.getFileSize(), r.getGeneratedAt())).toList();
    }

    private MapResponse toMapResponse(MagneticAnomalyMap map) {
        MapReview review = reviews.findFirstByMapIdOrderByReviewedAtDesc(map.getId()).orElse(null);
        Survey survey = map.getSurvey();
        return new MapResponse(map.getId(), survey.getId(), survey.getSurveyCode(), survey.getSurveyName(), survey.getCompany().getName(), map.getMapName(), map.getFilePath(), map.getAnomalyCount(), review == null ? "PENDING" : review.getDecision(), review == null ? null : review.getComment(), map.getGeneratedAt(), review == null ? null : review.getReviewedAt());
    }
}
