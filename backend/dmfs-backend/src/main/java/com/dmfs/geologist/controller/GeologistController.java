package com.dmfs.geologist.controller;

import com.dmfs.geologist.dto.*;
import com.dmfs.geologist.service.GeologistService;
import com.dmfs.geologist.entity.MagneticAnomalyMap;
import com.dmfs.geologist.repository.MagneticAnomalyMapRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

@RestController
@RequestMapping("/api/geologist")
@PreAuthorize("hasRole('GEOLOGIST')")
public class GeologistController {
    private final GeologistService service;
    private final MagneticAnomalyMapRepository maps;
    public GeologistController(GeologistService service, MagneticAnomalyMapRepository maps) { this.service = service; this.maps = maps; }

    @GetMapping("/dashboard") public DashboardResponse dashboard() { return service.dashboard(); }
    @GetMapping("/surveys") public List<SurveyResponse> surveys() { return service.surveyHistory(); }
    @GetMapping("/maps") public List<MapResponse> maps(@RequestParam(required = false) String status) { return service.mapQueue(status); }
    @PostMapping("/maps/{mapId}/review") public MapResponse review(@PathVariable Long mapId, @Valid @RequestBody ReviewRequest request, Authentication authentication) { return service.reviewMap(mapId, request, authentication.getName()); }
    @GetMapping("/reports") public List<ReportResponse> reports() { return service.reportList(); }
    @GetMapping("/maps/{mapId}/file")
    public ResponseEntity<InputStreamResource> mapFile(@PathVariable Long mapId) throws IOException {
        MagneticAnomalyMap map = maps.findById(mapId).orElseThrow(() -> new IllegalArgumentException("Anomaly map not found: " + mapId));
        Path path = Paths.get(map.getFilePath());
        if (!Files.isRegularFile(path)) throw new IllegalArgumentException("Anomaly map file is unavailable");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(map.getFileType())).body(new InputStreamResource(Files.newInputStream(path)));
    }
}
