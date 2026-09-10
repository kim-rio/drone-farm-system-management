package com.dmfs.survey.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.company.entity.SubscriberCompany;
import com.dmfs.survey.dto.SurveyDataResponse;
import com.dmfs.survey.entity.Surveyy;
import com.dmfs.survey.entity.SurveyyData;
import com.dmfs.survey.repository.SurveyyDataRepository;
import com.dmfs.survey.repository.SurveyyRepository;

@Service
public class SurveyDataService {

    private final SurveyyDataRepository surveyDataRepository;
    private final SurveyyRepository surveyRepository;
    private final UserRepository userRepository;


    public SurveyDataService(
            SurveyyDataRepository surveyDataRepository,
            SurveyyRepository surveyRepository,
            UserRepository userRepository
    ) {
        this.surveyDataRepository = surveyDataRepository;
        this.surveyRepository = surveyRepository;
        this.userRepository = userRepository;
    }


    // =========================================================
    // GET DATA FOR SURVEY
    // =========================================================

    @Transactional(readOnly = true)
    public List<SurveyDataResponse> getSurveyData(
            Long surveyId
    ) {

        SubscriberCompany company =
                getCurrentUserCompany();

        Surveyy survey =
                surveyRepository
                        .findByIdAndCompany(
                                surveyId,
                                company
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Survey not found"
                                )
                        );


        return surveyDataRepository
                .findBySurveyAndCompanyOrderByUploadedAtDesc(
                        survey,
                        company
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // GET ONE DATA RECORD
    // =========================================================

    @Transactional(readOnly = true)
    public SurveyDataResponse getSurveyDataById(
            Long id
    ) {

        SubscriberCompany company =
                getCurrentUserCompany();

        SurveyyData data =
                surveyDataRepository
                        .findByIdAndCompany(
                                id,
                                company
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Survey data not found"
                                )
                        );

        return toResponse(data);
    }


    // =========================================================
    // REGISTER UPLOADED FILE
    // =========================================================

    @Transactional
    public SurveyDataResponse registerUpload(
            Long surveyId,
            String fileName,
            String filePath,
            String fileType,
            Long fileSize,
            Long recordCount
    ) {

        SubscriberCompany company =
                getCurrentUserCompany();


        Surveyy survey =
                surveyRepository
                        .findByIdAndCompany(
                                surveyId,
                                company
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Survey not found"
                                )
                        );


        User uploadedBy =
                getCurrentUser();


        SurveyyData surveyData =
                new SurveyyData();

        surveyData.setSurvey(
                survey
        );

        surveyData.setCompany(
                company
        );

        surveyData.setFileName(
                fileName
        );

        surveyData.setFilePath(
                filePath
        );

        surveyData.setFileType(
                fileType
        );

        surveyData.setFileSize(
                fileSize
        );

        surveyData.setRecordCount(
                recordCount
        );

        surveyData.setUploadedBy(
                uploadedBy
        );


        return toResponse(
                surveyDataRepository.save(
                        surveyData
                )
        );
    }


    // =========================================================
    // DELETE DATA RECORD
    // =========================================================

    @Transactional
    public void deleteSurveyData(Long id) {

        SubscriberCompany company =
                getCurrentUserCompany();


        SurveyyData data =
                surveyDataRepository
                        .findByIdAndCompany(
                                id,
                                company
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Survey data not found"
                                )
                        );


        surveyDataRepository.delete(data);
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null
                || authentication.getName() == null) {

            throw new RuntimeException(
                    "Authenticated user not found"
            );
        }


        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );
    }


    // =========================================================
    // CURRENT COMPANY
    // =========================================================

    private SubscriberCompany getCurrentUserCompany() {

        User user =
                getCurrentUser();


        if (user.getCompany() == null) {

            throw new RuntimeException(
                    "User is not assigned to a company"
            );
        }


        return user.getCompany();
    }


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    private SurveyDataResponse toResponse(
            SurveyyData data
    ) {

        SurveyDataResponse response =
                new SurveyDataResponse();


        response.setId(
                data.getId()
        );

        response.setSurveyId(
                data.getSurvey().getId()
        );

        response.setSurveyCode(
                data.getSurvey().getSurveyCode()
        );

        response.setFileName(
                data.getFileName()
        );

        response.setFilePath(
                data.getFilePath()
        );

        response.setFileType(
                data.getFileType()
        );

        response.setFileSize(
                data.getFileSize()
        );

        response.setRecordCount(
                data.getRecordCount()
        );

        response.setUploadedById(
                data.getUploadedBy().getId()
        );

        response.setUploadedByName(
                data.getUploadedBy().getFirstName()
                        + " "
                        + data.getUploadedBy().getLastName()
        );

        response.setUploadedAt(
                data.getUploadedAt()
        );


        return response;
    }
}