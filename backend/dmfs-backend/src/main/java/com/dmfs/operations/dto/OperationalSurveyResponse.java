package com.dmfs.operations.dto;
import java.time.OffsetDateTime;
public record OperationalSurveyResponse(Long id, String surveyCode, String surveyName, String clientName, String farmName, String blockName, String status, OffsetDateTime startedAt, OffsetDateTime completedAt, String equipmentUsed) {}
