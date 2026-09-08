package com.dmfs.geologist.dto;
import java.time.OffsetDateTime;
public record SurveyResponse(Long id, String surveyCode, String surveyName, String companyName, String status, OffsetDateTime startedAt, OffsetDateTime endedAt) {}
