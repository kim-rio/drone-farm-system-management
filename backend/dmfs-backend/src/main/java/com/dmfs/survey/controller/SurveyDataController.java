package com.dmfs.survey.controller;

import com.dmfs.survey.dto.SurveyDataResponse;
import com.dmfs.survey.service.SurveyDataService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surveys/{surveyId}/data")
public class SurveyDataController {

    private final SurveyDataService surveyDataService;

    public SurveyDataController(
            SurveyDataService surveyDataService
    ) {
        this.surveyDataService = surveyDataService;
    }


    // =========================================================
    // GET ALL DATA FILES FOR A SURVEY
    // =========================================================

    @GetMapping
    public List<SurveyDataResponse> getSurveyData(
            @PathVariable Long surveyId
    ) {

        return surveyDataService.getSurveyData(
                surveyId
        );
    }


    // =========================================================
    // GET DATA FILE BY ID
    // =========================================================

    @GetMapping("/{dataId}")
    public SurveyDataResponse getSurveyDataById(
            @PathVariable Long surveyId,
            @PathVariable Long dataId
    ) {

        /*
         * The service currently identifies SurveyData
         * by its own ID and company.
         *
         * surveyId is retained in the URL because the
         * resource belongs to a survey.
         */
        return surveyDataService.getSurveyDataById(
                dataId
        );
    }


    // =========================================================
    // DELETE SURVEY DATA
    // =========================================================

    @DeleteMapping("/{dataId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSurveyData(
            @PathVariable Long surveyId,
            @PathVariable Long dataId
    ) {

        surveyDataService.deleteSurveyData(
                dataId
        );
    }
}