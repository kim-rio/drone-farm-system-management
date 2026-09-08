package com.dmfs.geologist.dto;
import java.time.OffsetDateTime;
public record MapResponse(Long id, Long surveyId, String surveyCode, String surveyName, String companyName, String mapName, String filePath, int anomalyCount, String decision, String reviewComment, OffsetDateTime generatedAt, OffsetDateTime reviewedAt) {}
