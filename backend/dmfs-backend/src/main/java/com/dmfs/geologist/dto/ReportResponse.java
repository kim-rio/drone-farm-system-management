package com.dmfs.geologist.dto;
import java.time.OffsetDateTime;
public record ReportResponse(Long id, Long surveyId, String surveyCode, String reportName, String filePath, String fileType, Long fileSize, OffsetDateTime generatedAt) {}
