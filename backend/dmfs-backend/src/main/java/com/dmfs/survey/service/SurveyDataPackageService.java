package com.dmfs.survey.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import com.dmfs.mission.service.MissionService;
import com.dmfs.survey.dto.SurveyDataFileResponse;
import com.dmfs.survey.dto.SurveyDataPackageResponse;
import com.dmfs.survey.entity.SurveyDataFile;
import com.dmfs.survey.entity.SurveyDataPackage;
import com.dmfs.survey.entity.Surveyy;
import com.dmfs.survey.enums.SurveyDataFileType;
import com.dmfs.survey.repository.SurveyDataFileRepository;
import com.dmfs.survey.repository.SurveyDataPackageRepository;
import com.dmfs.survey.repository.SurveyyRepository;
import com.dmfs.survey.validator.SurveyDataFileValidator;
import com.dmfs.survey.validator.ValidationResult;

@Service
public class SurveyDataPackageService {

    private final SurveyyRepository surveyyRepository;
    private final SurveyDataPackageRepository packageRepository;
    private final SurveyDataFileRepository fileRepository;
    private final List<SurveyDataFileValidator> validators;
    private final SurveyDataStorageService storageService;
    private final UserRepository userRepository;
    private final MissionService missionService;

    public SurveyDataPackageService(
            SurveyyRepository surveyyRepository,
            SurveyDataPackageRepository packageRepository,
            SurveyDataFileRepository fileRepository,
            List<SurveyDataFileValidator> validators,
            SurveyDataStorageService storageService,
            UserRepository userRepository,
            MissionService missionService
    ) {
        this.surveyyRepository = surveyyRepository;
        this.packageRepository = packageRepository;
        this.fileRepository = fileRepository;
        this.validators = validators;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.missionService = missionService;
    }

    @Transactional
    public SurveyDataPackageResponse createPackage(
            Long surveyId,
            List<MultipartFile> files,
            String authenticatedEmail
    ) {

        /*
         * --------------------------------------------------------
         * 1. Validate request
         * --------------------------------------------------------
         */

        if (surveyId == null) {
            throw new IllegalArgumentException(
                    "Survey ID is required."
            );
        }

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one survey input file is required."
            );
        }

        /*
         * --------------------------------------------------------
         * 2. Verify authenticated user
         * --------------------------------------------------------
         */

        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new AccessDeniedException(
                    "Authenticated user could not be identified."
            );
        }

        User authenticatedUser = userRepository.findByEmail(
                authenticatedEmail.trim().toLowerCase()
        ).orElseThrow(() ->
                new AccessDeniedException(
                        "Authenticated user not found."
                )
        );

        /*
         * --------------------------------------------------------
         * 3. Verify survey exists
         * --------------------------------------------------------
         */

        Surveyy survey = surveyyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Survey not found with ID: " + surveyId
                ));

        /*
         * --------------------------------------------------------
         * 4. Verify survey assignment
         * --------------------------------------------------------
         */

        if (survey.getOperator() == null
                || survey.getOperator().getId() == null
                || !survey.getOperator()
                        .getId()
                        .equals(authenticatedUser.getId())) {

            throw new AccessDeniedException(
                    "You are not authorized to upload data for this survey."
            );
        }

        /*
         * --------------------------------------------------------
         * 5. Create package
         * --------------------------------------------------------
         */

        SurveyDataPackage dataPackage = new SurveyDataPackage();

        dataPackage.setSurvey(survey);
        dataPackage.setPackageCode(generatePackageCode());
        dataPackage.setStatus("VALIDATING");
        dataPackage.setSubmittedBy(authenticatedUser.getId());

        /*
         * --------------------------------------------------------
         * 6. Track file types to prevent duplicates
         * --------------------------------------------------------
         */

        Set<String> suppliedTypes = new HashSet<>();

        boolean uavProvided = false;

        /*
         * --------------------------------------------------------
         * 7. Process every uploaded file
         * --------------------------------------------------------
         */

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException(
                        "One of the uploaded files is empty."
                );
            }

            String extension = extractExtension(
                    file.getOriginalFilename()
            );

            /*
             * Only operator input formats are allowed.
             */

            SurveyDataFileType fileType =
                    determineFileType(extension);

            /*
             * Prevent duplicate file types inside
             * the same package.
             */

            if (!suppliedTypes.add(fileType.name())) {
                throw new IllegalArgumentException(
                        "Duplicate file type detected: "
                                + fileType.name()
                );
            }

            if (fileType == SurveyDataFileType.UAV) {
                uavProvided = true;
            }

            /*
             * Find the validator responsible for this
             * file extension.
             */

            SurveyDataFileValidator validator =
                    findValidator(extension);

            ValidationResult validationResult =
                    validator.validate(file);

            /*
             * ------------------------------------------------
             * Create file metadata
             * ------------------------------------------------
             */

            SurveyDataFile dataFile = new SurveyDataFile();

            dataFile.setFileName(
                    file.getOriginalFilename()
            );

            dataFile.setFileType(
                    fileType.name()
            );

            dataFile.setFileExtension(
                    extension
            );

            dataFile.setFileSize(
                    file.getSize()
            );

            dataFile.setValidationStatus(
                    validationResult.isValid()
                            ? "VALID"
                            : "INVALID"
            );

            dataFile.setValidationMessage(
                    validationResult.getMessage()
            );

            /*
             * Store the actual uploaded file.
             */

            String storageKey = storageService.store(
                    file,
                    dataPackage.getPackageCode()
            );

            dataFile.setStorageKey(storageKey);

            /*
             * Calculate SHA-256 checksum for file integrity.
             */

            String checksum = storageService.calculateChecksum(
                    file
            );

            dataFile.setChecksum(checksum);

            dataPackage.addFile(dataFile);
        }

        /*
         * --------------------------------------------------------
         * 8. UAV is mandatory
         * --------------------------------------------------------
         */

        if (!uavProvided) {

            dataPackage.setStatus("DRAFT");

            throw new IllegalArgumentException(
                    "UAV survey input file is required."
            );
        }

        /*
         * --------------------------------------------------------
         * 9. Check whether all supplied files are valid
         * --------------------------------------------------------
         */

        boolean allFilesValid = dataPackage.getFiles()
                .stream()
                .allMatch(file ->
                        "VALID".equals(
                                file.getValidationStatus()
                        )
                );

        if (!allFilesValid) {

            dataPackage.setStatus("DRAFT");

            packageRepository.save(dataPackage);

            return toResponse(dataPackage);
        }

        /*
         * --------------------------------------------------------
         * 10. Package is structurally valid
         * --------------------------------------------------------
         */

        dataPackage.setStatus("VALID");

        dataPackage.setValidatedAt(
                LocalDateTime.now()
        );

        /*
         * --------------------------------------------------------
         * 11. Persist package and files
         * --------------------------------------------------------
         */

        SurveyDataPackage savedPackage =
                packageRepository.save(dataPackage);

        /*
         * --------------------------------------------------------
         * 12. Return response
         * --------------------------------------------------------
         */

        return toResponse(savedPackage);
    }

    /*
     * ============================================================
     * Submit package
     * ============================================================
     */

    @Transactional
    public SurveyDataPackageResponse submitPackage(
            Long packageId,
            String authenticatedEmail
    ) {

        /*
         * --------------------------------------------------------
         * 1. Validate request
         * --------------------------------------------------------
         */

        if (packageId == null) {
            throw new IllegalArgumentException(
                    "Package ID is required."
            );
        }

        if (authenticatedEmail == null
                || authenticatedEmail.isBlank()) {

            throw new AccessDeniedException(
                    "Authenticated user could not be identified."
            );
        }

        /*
         * --------------------------------------------------------
         * 2. Find authenticated user
         * --------------------------------------------------------
         */

        User authenticatedUser = userRepository.findByEmail(
                authenticatedEmail.trim().toLowerCase()
        ).orElseThrow(() ->
                new AccessDeniedException(
                        "Authenticated user not found."
                )
        );

        /*
         * --------------------------------------------------------
         * 3. Find package
         * --------------------------------------------------------
         */

        SurveyDataPackage dataPackage =
                packageRepository.findById(packageId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Survey data package not found with ID: "
                                                + packageId
                                )
                        );

        /*
         * --------------------------------------------------------
         * 4. Verify survey assignment
         * --------------------------------------------------------
         */

        Surveyy survey = dataPackage.getSurvey();

        if (survey == null
                || survey.getOperator() == null
                || survey.getOperator().getId() == null
                || !survey.getOperator()
                        .getId()
                        .equals(authenticatedUser.getId())) {

            throw new AccessDeniedException(
                    "You are not authorized to submit this survey data package."
            );
        }

        /*
         * --------------------------------------------------------
         * 5. Verify package status
         * --------------------------------------------------------
         */

        if (!"VALID".equals(dataPackage.getStatus())) {

            throw new IllegalStateException(
                    "Only a VALID survey data package can be submitted."
            );
        }

        /*
         * --------------------------------------------------------
         * 6. Verify all files are valid
         * --------------------------------------------------------
         */

        boolean allFilesValid = dataPackage.getFiles()
                .stream()
                .allMatch(file ->
                        "VALID".equals(
                                file.getValidationStatus()
                        )
                );

        if (!allFilesValid) {

            throw new IllegalStateException(
                    "The survey data package contains invalid files."
            );
        }

        /*
         * --------------------------------------------------------
         * 7. Submit package
         * --------------------------------------------------------
         */

        dataPackage.setStatus("SUBMITTED");

        dataPackage.setSubmittedBy(
                authenticatedUser.getId()
        );

        dataPackage.setSubmittedAt(
                LocalDateTime.now()
        );

        /*
         * --------------------------------------------------------
         * 8. Persist submitted package
         * --------------------------------------------------------
         */

        SurveyDataPackage submittedPackage =
                packageRepository.save(dataPackage);

        /*
         * --------------------------------------------------------
         * 9. Complete the associated mission
         *
         * The mission is completed only after the survey
         * data package has successfully been submitted.
         * --------------------------------------------------------
         */

        if (survey.getServiceRequest() == null
                || survey.getServiceRequest().getId() == null) {

            throw new IllegalStateException(
                    "Survey is not linked to a service request."
            );
        }

        missionService.completeMissionForServiceRequest(
                survey.getServiceRequest().getId()
        );

        /*
         * --------------------------------------------------------
         * 10. Return submitted package
         * --------------------------------------------------------
         */

        return toResponse(submittedPackage);
    }

    /*
     * ============================================================
     * File type detection
     * ============================================================
     */

    private SurveyDataFileType determineFileType(
            String extension
    ) {

        return switch (extension.toLowerCase(Locale.ROOT)) {

            case "uav" -> SurveyDataFileType.UAV;

            case "xyz" -> SurveyDataFileType.XYZ;

            case "kml" -> SurveyDataFileType.KML;

            case "bna" -> SurveyDataFileType.BNA;

            /*
             * Processed products are intentionally rejected.
             */

            case "grd", "tif", "tiff", "jpg", "jpeg" ->
                    throw new IllegalArgumentException(
                            "Processed survey product '." +
                                    extension +
                                    "' is not accepted as operator input data."
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported survey input file type: ." +
                                    extension
                    );
        };
    }

    /*
     * ============================================================
     * Validator selection
     * ============================================================
     */

    private SurveyDataFileValidator findValidator(
            String extension
    ) {

        return validators.stream()
                .filter(validator ->
                        validator.supports(extension)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No validator is available for file type: ."
                                        + extension
                        )
                );
    }

    /*
     * ============================================================
     * File extension extraction
     * ============================================================
     */

    private String extractExtension(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "Uploaded file name is missing."
            );
        }

        int lastDot = fileName.lastIndexOf('.');

        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            throw new IllegalArgumentException(
                    "Uploaded file does not have a valid extension."
            );
        }

        return fileName
                .substring(lastDot + 1)
                .toLowerCase(Locale.ROOT);
    }

    /*
     * ============================================================
     * Package code generation
     * ============================================================
     */

    private String generatePackageCode() {

        String code;

        do {

            code = "SDP-" +
                    LocalDateTime.now().getYear() +
                    "-" +
                    UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();

        } while (
                packageRepository.existsByPackageCode(code)
        );

        return code;
    }

    /*
     * ============================================================
     * Entity -> DTO
     * ============================================================
     */

    private SurveyDataPackageResponse toResponse(
            SurveyDataPackage dataPackage
    ) {

        SurveyDataPackageResponse response =
                new SurveyDataPackageResponse();

        response.setId(
                dataPackage.getId()
        );

        response.setPackageCode(
                dataPackage.getPackageCode()
        );

        response.setSurveyId(
                dataPackage.getSurvey().getId()
        );

        response.setStatus(
                dataPackage.getStatus()
        );

        response.setSubmittedBy(
                dataPackage.getSubmittedBy()
        );

        response.setCreatedAt(
                dataPackage.getCreatedAt()
        );

        response.setValidatedAt(
                dataPackage.getValidatedAt()
        );

        response.setSubmittedAt(
                dataPackage.getSubmittedAt()
        );

        List<SurveyDataFileResponse> fileResponses =
                dataPackage.getFiles()
                        .stream()
                        .map(this::toFileResponse)
                        .toList();

        response.setFiles(fileResponses);

        return response;
    }

    private SurveyDataFileResponse toFileResponse(
            SurveyDataFile file
    ) {

        return new SurveyDataFileResponse(
                file.getId(),
                file.getFileName(),
                file.getFileType(),
                file.getFileExtension(),
                file.getFileSize(),
                file.getValidationStatus(),
                file.getValidationMessage()
        );
    }
}