package com.dmfs.geologist.dto;
public record ProcessingResponse(Long surveyDataId, Long processedDataId, Long anomalyMapId, long recordsProcessed, int anomaliesDetected) {}
