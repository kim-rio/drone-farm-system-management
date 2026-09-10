package com.dmfs.survey.controller;

import com.dmfs.survey.dto.CreateSurveyRequest;
import com.dmfs.survey.dto.SurveyResponse;
import com.dmfs.survey.dto.UpdateSurveyRequest;
import com.dmfs.survey.service.SurveyService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(
            SurveyService surveyService
    ) {
        this.surveyService = surveyService;
    }

    // =========================================================
    // GET ALL SURVEYS
    // =========================================================

    @GetMapping
    public List<SurveyResponse> getSurveys() {

        return surveyService.getSurveys();
    }

    @GetMapping("/my")
    public List<SurveyResponse> getMySurveys() {
        return surveyService.getMySurveys();
    }

    // =========================================================
    // GET SURVEY BY ID
    // =========================================================

    @GetMapping("/{id}")
    public SurveyResponse getSurvey(
            @PathVariable Long id
    ) {

        return surveyService.getSurvey(id);
    }

    // =========================================================
    // CREATE SURVEY
    // =========================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SurveyResponse createSurvey(
            @Valid @RequestBody CreateSurveyRequest request
    ) {

        return surveyService.createSurvey(request);
    }

    // =========================================================
    // UPDATE SURVEY
    // =========================================================

    @PutMapping("/{id}")
    public SurveyResponse updateSurvey(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSurveyRequest request
    ) {

        return surveyService.updateSurvey(
                id,
                request
        );
    }

    // =========================================================
    // START SURVEY
    // =========================================================

    @PatchMapping("/{id}/start")
    public SurveyResponse startSurvey(
            @PathVariable Long id
    ) {

        return surveyService.startSurvey(id);
    }

    // =========================================================
    // COMPLETE SURVEY
    // =========================================================

    @PatchMapping("/{id}/complete")
    public SurveyResponse completeSurvey(
            @PathVariable Long id
    ) {

        return surveyService.completeSurvey(id);
    }

    // =========================================================
    // CANCEL SURVEY
    // =========================================================

    @PatchMapping("/{id}/cancel")
    public SurveyResponse cancelSurvey(
            @PathVariable Long id
    ) {

        return surveyService.cancelSurvey(id);
    }
}
