package com.dmfs.agriculture.controller;

import com.dmfs.agriculture.dto.AgricultureReportRequest;
import com.dmfs.agriculture.dto.AgricultureReportResponse;
import com.dmfs.agriculture.service.AgricultureReportService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/agriculture-reports")
public class AgricultureReportController {
    private final AgricultureReportService service;
    public AgricultureReportController(AgricultureReportService service){this.service=service;}

    @GetMapping public List<AgricultureReportResponse> list(){return service.list();}
    @GetMapping("/{id}") public AgricultureReportResponse get(@PathVariable Long id){return service.get(id);}
    @GetMapping("/mission/{missionId}") public AgricultureReportResponse getForMission(@PathVariable Long missionId){return service.getForMission(missionId);}

    @PostMapping(value="/mission/{missionId}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public AgricultureReportResponse save(
            @PathVariable Long missionId,
            @RequestPart("report") AgricultureReportRequest request,
            @RequestPart(value="photo1", required=false) MultipartFile photo1,
            @RequestPart(value="photo2", required=false) MultipartFile photo2) throws IOException {
        return service.save(missionId, request, photo1, photo2);
    }

    @GetMapping("/{id}/photos/{slot}")
    public ResponseEntity<Resource> photo(@PathVariable Long id, @PathVariable int slot) {
        if(slot!=1 && slot!=2) throw new IllegalArgumentException("Photo slot must be 1 or 2");
        Resource resource=service.photo(id,slot);
        String type=MediaType.IMAGE_JPEG_VALUE;
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(type)).body(resource);
    }
}
