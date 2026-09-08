package com.dmfs.geologist.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public record ReviewRequest(@NotBlank @Pattern(regexp = "(?i)APPROVED|REJECTED|REPROCESSED", message = "Decision must be APPROVED, REJECTED, or REPROCESSED") String decision, String comment) {}
