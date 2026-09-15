package com.dmfs.survey.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Value("${dmfs.upload.directory:uploads/surveys}")
    private String uploadDirectory;


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
    // UPLOAD FILE
    // =========================================================

    @Transactional
    public SurveyDataResponse uploadSurveyData(
            Long surveyId,
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {

            throw new RuntimeException(
                    "No survey data file was provided"
            );
        }


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


        /*
         * We currently accept CSV files because
         * the operator frontend is built around
         * the raw magnetometer CSV format.
         */
        String originalFileName =
                file.getOriginalFilename();

        if (originalFileName == null
                || originalFileName.isBlank()) {

            throw new RuntimeException(
                    "Uploaded file has no name"
            );
        }


        String lowerName =
                originalFileName.toLowerCase();


        if (!lowerName.endsWith(".csv")) {

            throw new RuntimeException(
                    "Only CSV survey data files are supported"
            );
        }


        try {

            /*
             * Create upload directory.
             */
            Path directory =
                    Paths.get(uploadDirectory)
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(directory);


            /*
             * Generate a unique stored filename.
             *
             * We do NOT trust the original filename
             * as the physical storage filename.
             */
            String storedFileName =
                    UUID.randomUUID()
                            + ".csv";


            Path target =
                    directory.resolve(
                            storedFileName
                    ).normalize();


            /*
             * Make sure the resolved path is still
             * inside our upload directory.
             */
            if (!target.startsWith(directory)) {

                throw new RuntimeException(
                        "Invalid file storage path"
                );
            }


            /*
             * Save physical file.
             */
            Files.copy(
                    file.getInputStream(),
                    target
            );


            /*
             * Count CSV records.
             *
             * For the current raw-data format,
             * the first non-empty line is treated
             * as the header.
             */
            long recordCount =
                    countCsvRecords(target);


            /*
             * Register metadata in survey_data.
             */
            SurveyyData surveyData =
                    new SurveyyData();


            surveyData.setSurvey(
                    survey
            );


            surveyData.setCompany(
                    company
            );


            surveyData.setFileName(
                    originalFileName
            );


            surveyData.setFilePath(
                    target.toString()
            );


            surveyData.setFileType(
                    "CSV"
            );


            surveyData.setFileSize(
                    file.getSize()
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

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to store survey data file",
                    e
            );
        }
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
    // COUNT CSV RECORDS
    // =========================================================

    private long countCsvRecords(
            Path file
    ) throws IOException {

        try (var lines =
                     Files.lines(file)) {

            long nonEmptyLines =
                    lines
                            .map(String::trim)
                            .filter(line -> !line.isEmpty())
                            .count();

            /*
             * First line is the CSV header.
             */
            return Math.max(
                    0,
                    nonEmptyLines - 1
            );
        }
    }


    // =========================================================
    // DELETE DATA RECORD
    // =========================================================

    @Transactional
    public void deleteSurveyData(
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


        surveyDataRepository.delete(
                data
        );
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
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

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