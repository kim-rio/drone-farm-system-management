package com.dmfs.operations.dto;
import jakarta.validation.constraints.NotBlank;
public record RejectOperationRequest(@NotBlank String reason) {}
