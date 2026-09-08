package com.dmfs.operations.dto;
import jakarta.validation.constraints.*;
public record CompleteSurveyRequest(@NotBlank String equipmentUsed, @NotNull @DecimalMin("-90") @DecimalMax("90") Double minLatitude, @NotNull @DecimalMin("-180") @DecimalMax("180") Double minLongitude, @NotNull @DecimalMin("-90") @DecimalMax("90") Double maxLatitude, @NotNull @DecimalMin("-180") @DecimalMax("180") Double maxLongitude) {}
