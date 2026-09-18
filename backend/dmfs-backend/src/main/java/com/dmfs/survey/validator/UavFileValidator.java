package com.dmfs.survey.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UavFileValidator implements SurveyDataFileValidator {

    private static final String EXTENSION = "uav";

    @Override
    public boolean supports(String extension) {
        return EXTENSION.equalsIgnoreCase(extension);
    }

    @Override
    public ValidationResult validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ValidationResult.invalid(
                    "UAV file is empty or was not provided."
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            return ValidationResult.invalid(
                    "UAV file name is missing."
            );
        }

        if (!fileName.toLowerCase().endsWith(".uav")) {
            return ValidationResult.invalid(
                    "File does not have the required .uav extension."
            );
        }

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                file.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                )
        ) {

            boolean hasNpro = false;
            boolean hasNtot = false;
            boolean hasVers = false;
            boolean hasColumnHeader = false;
            boolean hasData = false;

            int nonEmptyLines = 0;
            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                nonEmptyLines++;

                String upperLine = line.toUpperCase();

                /*
                 * Common UAV parameters.
                 */
                if (upperLine.contains("NPRO")) {
                    hasNpro = true;
                }

                if (upperLine.contains("NTOT")) {
                    hasNtot = true;
                }

                if (upperLine.contains("VERS")) {
                    hasVers = true;
                }

                /*
                 * The UAV format contains column definitions
                 * before the measurement data.
                 */
                if (upperLine.contains("TIME")
                        || upperLine.contains("YEAR")
                        || upperLine.contains("LON")
                        || upperLine.contains("LAT")
                        || upperLine.contains("TMI")) {

                    hasColumnHeader = true;
                }

                /*
                 * A line containing numeric measurement values
                 * is treated as potential survey data.
                 */
                if (containsNumericData(line)) {
                    hasData = true;
                }
            }

            if (nonEmptyLines < 5) {
                return ValidationResult.invalid(
                        "UAV file does not contain enough structured content."
                );
            }

            if (!hasNpro) {
                return ValidationResult.invalid(
                        "UAV file is missing the NPRO parameter."
                );
            }

            if (!hasNtot) {
                return ValidationResult.invalid(
                        "UAV file is missing the NTOT parameter."
                );
            }

            if (!hasVers) {
                return ValidationResult.invalid(
                        "UAV file is missing the VERS parameter."
                );
            }

            if (!hasColumnHeader) {
                return ValidationResult.invalid(
                        "UAV file does not contain recognizable data-column definitions."
                );
            }

            if (!hasData) {
                return ValidationResult.invalid(
                        "UAV file does not contain recognizable survey data."
                );
            }

            return ValidationResult.valid(
                    "UAV file structure is valid."
            );

        } catch (IOException e) {

            return ValidationResult.invalid(
                    "Unable to read UAV file: " + e.getMessage()
            );
        }
    }

    private boolean containsNumericData(String line) {

        String[] tokens = line.split("\\s+");

        int numericTokens = 0;

        for (String token : tokens) {

            if (isNumeric(token)) {
                numericTokens++;

                if (numericTokens >= 3) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isNumeric(String value) {

        try {
            Double.parseDouble(
                    value.replace(",", ".")
            );

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }
}