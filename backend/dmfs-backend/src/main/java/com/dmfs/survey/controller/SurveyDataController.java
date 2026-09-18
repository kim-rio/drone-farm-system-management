package com.dmfs.survey.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dmfs.survey.dto.SurveyDataPackageResponse;
import com.dmfs.survey.service.SurveyDataPackageService;

@RestController
@RequestMapping("/api/surveys")
public class SurveyDataController {

    private final SurveyDataPackageService packageService;

    public SurveyDataController(
            SurveyDataPackageService packageService
    ) {
        this.packageService = packageService;
    }

    /**
     * Upload survey input data for a survey.
     *
     * Accepted input files:
     * .uav - required
     * .xyz - optional
     * .kml - optional
     * .bna - optional
     *
     * Only the drone operator assigned to the survey
     * is authorized to upload its survey data.
     */
    @PreAuthorize("hasRole('DRONE_OPERATOR')")
    @PostMapping(
            value = "/{surveyId}/data-packages",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SurveyDataPackageResponse> uploadSurveyData(
            @PathVariable Long surveyId,
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication
    ) {

        String authenticatedEmail = authentication.getName();

        SurveyDataPackageResponse response =
                packageService.createPackage(
                        surveyId,
                        files,
                        authenticatedEmail
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Submit a validated survey data package.
     *
     * Only the drone operator assigned to the survey
     * can submit the package.
     */
    @PreAuthorize("hasRole('DRONE_OPERATOR')")
    @PostMapping(
            value = "/data-packages/{packageId}/submit",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SurveyDataPackageResponse> submitSurveyDataPackage(
            @PathVariable Long packageId,
            Authentication authentication
    ) {

        String authenticatedEmail = authentication.getName();

        SurveyDataPackageResponse response =
                packageService.submitPackage(
                        packageId,
                        authenticatedEmail
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}