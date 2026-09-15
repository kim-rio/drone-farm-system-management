package com.dmfs.survey.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dmfs.survey.dto.SurveyDataResponse;
import com.dmfs.survey.service.SurveyDataService;

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
// UPLOAD SURVEY DATA FILE
// =========================================================

@PostMapping(
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
)
@ResponseStatus(HttpStatus.CREATED)
public SurveyDataResponse uploadSurveyData(
        @PathVariable Long surveyId,
        @RequestParam("file") MultipartFile file
) {

    return surveyDataService.uploadSurveyData(
            surveyId,
            file
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