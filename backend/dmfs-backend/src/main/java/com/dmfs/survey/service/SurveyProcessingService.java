package com.dmfs.survey.service;

import com.dmfs.geologist.entity.MagneticAnomalyMap;
import com.dmfs.geologist.entity.ProcessedMagneticData;
import com.dmfs.geologist.entity.Survey;
import com.dmfs.geologist.entity.SurveyData;
import com.dmfs.geologist.repository.MagneticAnomalyMapRepository;
import com.dmfs.geologist.repository.ProcessedMagneticDataRepository;
import com.dmfs.geologist.repository.SurveyDataRepository;
import com.dmfs.geologist.repository.SurveyRepository;
import com.dmfs.survey.dto.ProcessingResultResponse;
import com.dmfs.survey.entity.SurveyDataFile;
import com.dmfs.survey.entity.SurveyDataPackage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

@Service
public class SurveyProcessingService {

    private final PythonProcessingService pythonProcessingService;
    private final SurveyDataStorageService storageService;

    private final SurveyRepository surveyRepository;
    private final SurveyDataRepository surveyDataRepository;
    private final ProcessedMagneticDataRepository processedRepository;
    private final MagneticAnomalyMapRepository anomalyMapRepository;

    public SurveyProcessingService(
            PythonProcessingService pythonProcessingService,
            SurveyDataStorageService storageService,
            SurveyRepository surveyRepository,
            SurveyDataRepository surveyDataRepository,
            ProcessedMagneticDataRepository processedRepository,
            MagneticAnomalyMapRepository anomalyMapRepository
    ) {
        this.pythonProcessingService = pythonProcessingService;
        this.storageService = storageService;
        this.surveyRepository = surveyRepository;
        this.surveyDataRepository = surveyDataRepository;
        this.processedRepository = processedRepository;
        this.anomalyMapRepository = anomalyMapRepository;
    }

    @Transactional
    public ProcessingResultResponse processPackage(
            SurveyDataPackage dataPackage
    ) {

        if (dataPackage == null) {
            throw new IllegalArgumentException(
                    "Survey data package is required."
            );
        }

        if (dataPackage.getSurvey() == null
                || dataPackage.getSurvey().getId() == null) {

            throw new IllegalStateException(
                    "Survey data package is not linked to a survey."
            );
        }

        /*
         * --------------------------------------------------------
         * 1. Find the UAV file
         * --------------------------------------------------------
         */

        SurveyDataFile uavFile =
                dataPackage.getFiles()
                        .stream()
                        .filter(file ->
                                "UAV".equalsIgnoreCase(
                                        file.getFileType()
                                )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No UAV file was found in the survey data package."
                                )
                        );

        if (uavFile.getStorageKey() == null
                || uavFile.getStorageKey().isBlank()) {

            throw new IllegalStateException(
                    "UAV file has no storage key."
            );
        }

        /*
         * --------------------------------------------------------
         * 2. Resolve the stored UAV file
         * --------------------------------------------------------
         */

        Path uavPath =
                storageService.resolveStorageKey(
                        uavFile.getStorageKey()
                );

        if (!Files.exists(uavPath)) {
            throw new IllegalStateException(
                    "Stored UAV file does not exist: "
                            + uavPath
            );
        }

        /*
         * --------------------------------------------------------
         * 3. Load the geologist Survey entity
         *
         * Surveyy and geologist.entity.Survey currently map
         * to the same database table.
         * --------------------------------------------------------
         */

        Long surveyId =
                dataPackage.getSurvey().getId();

        Survey survey =
                surveyRepository.findById(surveyId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Geologist survey entity not found with ID: "
                                                + surveyId
                                )
                        );

        /*
         * --------------------------------------------------------
         * 4. Run Python processing
         * --------------------------------------------------------
         */

        ProcessingResultResponse result =
                pythonProcessingService.processUav(
                        uavFile.getStorageKey()
                );

        /*
         * --------------------------------------------------------
         * 5. Verify Python processing result
         * --------------------------------------------------------
         */

        if (!result.isSuccess()) {
            throw new IllegalStateException(
                    "UAV processing failed: "
                            + result.getMessage()
            );
        }

        if (result.getOutputFile() == null
                || result.getOutputFile().isBlank()) {

            throw new IllegalStateException(
                    "Python processing did not return a processed output file."
            );
        }

        /*
         * --------------------------------------------------------
         * 6. Create SurveyData record
         * --------------------------------------------------------
         */

        SurveyData surveyData = new SurveyData();

        surveyData.setSurvey(survey);

        /*
         * company_id comes from the survey.
         */
        if (survey.getCompany() == null
                || survey.getCompany().getId() == null) {

            throw new IllegalStateException(
                    "Survey is not linked to a subscriber company."
            );
        }

        surveyData.setCompanyId(
                survey.getCompany().getId()
        );

        surveyData.setFileName(
                uavFile.getFileName()
        );

        surveyData.setFilePath(
                uavFile.getStorageKey()
        );

        surveyData.setFileType(
                uavFile.getFileType()
        );

        surveyData.setFileSize(
                uavFile.getFileSize()
        );

        surveyData.setRecordCount(
                result.getRecordCount() != null
                        ? result.getRecordCount().longValue()
                        : null
        );

        surveyData.setUploadedBy(
                dataPackage.getSubmittedBy()
        );

        surveyData.setUploadedAt(
                OffsetDateTime.now()
        );

        SurveyData savedSurveyData =
                surveyDataRepository.save(surveyData);

        /*
         * --------------------------------------------------------
         * 7. Create ProcessedMagneticData
         * --------------------------------------------------------
         */

        ProcessedMagneticData processed =
                new ProcessedMagneticData();

        processed.setSurvey(survey);

        processed.setSurveyData(
                savedSurveyData
        );

        processed.setFileName(
                extractFileName(
                        result.getOutputFile()
                )
        );

        processed.setFilePath(
                result.getOutputFile()
        );

        processed.setFileType(
                "CSV"
        );

        processed.setFileSize(
                resolveResultFileSize(
                        result.getOutputFile()
                )
        );

        processed.setRecordCount(
                result.getValidRecordCount() != null
                        ? result.getValidRecordCount().longValue()
                        : result.getRecordCount() != null
                        ? result.getRecordCount().longValue()
                        : null
        );

        processed.setProcessedAt(
                OffsetDateTime.now()
        );

        ProcessedMagneticData savedProcessed =
                processedRepository.save(processed);

        /*
         * --------------------------------------------------------
         * 8. Create MagneticAnomalyMap
         * --------------------------------------------------------
         */

        if (result.getGeotiffFile() != null
                && !result.getGeotiffFile().isBlank()) {

            MagneticAnomalyMap anomalyMap =
                    new MagneticAnomalyMap();

            anomalyMap.setSurvey(survey);

            anomalyMap.setProcessedMagneticDataId(
                    savedProcessed.getId()
            );

            anomalyMap.setMapName(
                    survey.getSurveyCode()
                            + " Magnetic Anomaly Map"
            );

            anomalyMap.setFilePath(
                    result.getGeotiffFile()
            );

            anomalyMap.setFileType(
                    "GEOTIFF"
            );

            anomalyMap.setFileSize(
                    resolveResultFileSize(
                            result.getGeotiffFile()
                    )
            );

            anomalyMap.setAnomalyCount(
                    result.getAnomalyCount() != null
                            ? result.getAnomalyCount()
                            : 0
            );

            anomalyMap.setGeneratedAt(
                    OffsetDateTime.now()
            );

            anomalyMapRepository.save(anomalyMap);
        }

        return result;
    }

    private String extractFileName(
            String path
    ) {

        if (path == null || path.isBlank()) {
            return "processed.csv";
        }

        return Path.of(path)
                .getFileName()
                .toString();
    }

    private Long resolveResultFileSize(
            String path
    ) {

        if (path == null || path.isBlank()) {
            return null;
        }

        try {

            Path filePath =
                    Path.of(path)
                            .toAbsolutePath()
                            .normalize();

            if (Files.exists(filePath)) {
                return Files.size(filePath);
            }

        } catch (Exception ignored) {
            /*
             * The Python service may be running in a separate
             * filesystem/container. In that case the returned
             * path is still stored, but the local Java process
             * cannot determine the size.
             */
        }

        return null;
    }
}