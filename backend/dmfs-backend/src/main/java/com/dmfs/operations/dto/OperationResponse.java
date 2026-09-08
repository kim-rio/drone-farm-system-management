package com.dmfs.operations.dto;
import java.time.LocalDate;
public record OperationResponse(Long id, String clientName, String farmName, String blockName, String serviceName, LocalDate requestedDate, String notes, String status, String operatorDecisionReason) {}
