package com.dmfs.geologist.controller;

import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.geologist.dto.ProcessingResponse;
import com.dmfs.geologist.service.MagneticProcessingService;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/drone-operator/surveys")
@PreAuthorize("hasRole('DRONE_OPERATOR')")
public class MagneticProcessingController {
    private final MagneticProcessingService processor; private final UserRepository users;
    public MagneticProcessingController(MagneticProcessingService processor, UserRepository users) { this.processor=processor; this.users=users; }
    @PostMapping(path="/{surveyId}/magnetic-data", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProcessingResponse> upload(@PathVariable Long surveyId, @RequestParam("file") MultipartFile file, Authentication authentication) {
        User user=users.findByEmail(authentication.getName()).orElseThrow(()->new AccessDeniedException("Authenticated operator was not found"));
        return ResponseEntity.status(HttpStatus.CREATED).body(processor.uploadAndProcess(surveyId,file,user));
    }
}
