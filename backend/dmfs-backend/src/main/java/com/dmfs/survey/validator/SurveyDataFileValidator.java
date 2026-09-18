package com.dmfs.survey.validator;

import org.springframework.web.multipart.MultipartFile;

public interface SurveyDataFileValidator {

    /**
     * Validates the structure and format of a survey input file.
     *
     * Scientific correctness and data quality are not checked here.
     *
     * @param file uploaded survey input file
     * @return validation result
     */
    ValidationResult validate(MultipartFile file);

    /**
     * Returns true if this validator supports the given file extension.
     *
     * @param extension file extension without the dot
     * @return true if supported
     */
    boolean supports(String extension);
}