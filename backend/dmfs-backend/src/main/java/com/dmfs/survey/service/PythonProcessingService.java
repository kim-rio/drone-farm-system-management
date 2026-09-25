package com.dmfs.survey.service;

import com.dmfs.survey.dto.ProcessingResultResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PythonProcessingService {

    private final RestClient restClient;
    private final SurveyDataStorageService storageService;

    public PythonProcessingService(
            @Value("${dmfs.processing.url:http://localhost:8001}")
            String processingUrl,
            SurveyDataStorageService storageService
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(processingUrl)
                .build();

        this.storageService = storageService;
    }

    public ProcessingResultResponse processUav(String storageKey) {

        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException(
                    "UAV storage key is required."
            );
        }

        Path uavPath = storageService.resolveStorageKey(storageKey);

        if (!Files.exists(uavPath)) {
            throw new IllegalStateException(
                    "UAV file does not exist: " + uavPath
            );
        }

        if (!Files.isRegularFile(uavPath)) {
            throw new IllegalStateException(
                    "UAV storage path is not a file: " + uavPath
            );
        }

        FileSystemResource resource =
                new FileSystemResource(uavPath) {

                    @Override
                    public String getFilename() {
                        return uavPath.getFileName().toString();
                    }
                };

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        /*
         * IMPORTANT:
         *
         * Python expects:
         *
         *     file: UploadFile
         *
         * Therefore the multipart field MUST be named "file".
         */
        body.add("file", resource);

        ProcessingResultResponse result =
                restClient.post()
                        .uri("/api/processing/uav")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(body)
                        .retrieve()
                        .body(ProcessingResultResponse.class);

        if (result == null) {
            throw new IllegalStateException(
                    "Python processing service returned no response."
            );
        }

        if (!result.isSuccess()) {
            throw new IllegalStateException(
                    "Python processing failed: "
                            + result.getMessage()
            );
        }

        return result;
    }
}
