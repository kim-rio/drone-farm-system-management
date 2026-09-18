package com.dmfs.survey.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class BnaFileValidator implements SurveyDataFileValidator {

    private static final String EXTENSION = "bna";

    @Override
    public boolean supports(String extension) {
        return EXTENSION.equalsIgnoreCase(extension);
    }

    @Override
    public ValidationResult validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ValidationResult.invalid(
                    "BNA file is empty or was not provided."
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            return ValidationResult.invalid(
                    "BNA file name is missing."
            );
        }

        if (!fileName.toLowerCase().endsWith(".bna")) {
            return ValidationResult.invalid(
                    "File does not have the required .bna extension."
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

            String line;

            int lineNumber = 0;
            int coordinateRows = 0;
            boolean hasContent = false;

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                hasContent = true;

                /*
                 * BNA polygon data normally contains coordinate
                 * information. Look for lines containing at
                 * least two numeric values.
                 */
                if (containsCoordinatePair(line)) {
                    coordinateRows++;
                }

                if (coordinateRows >= 3) {
                    break;
                }
            }

            if (!hasContent) {
                return ValidationResult.invalid(
                        "BNA file does not contain readable content."
                );
            }

            if (coordinateRows == 0) {
                return ValidationResult.invalid(
                        "BNA file does not contain recognizable coordinate data."
                );
            }

            return ValidationResult.valid(
                    "BNA file structure is valid."
            );

        } catch (IOException e) {

            return ValidationResult.invalid(
                    "Unable to read BNA file: " + e.getMessage()
            );
        }
    }

    private boolean containsCoordinatePair(String line) {

        String[] tokens = line.split("[,\\s]+");

        int numericTokens = 0;

        for (String token : tokens) {

            if (isNumeric(token)) {
                numericTokens++;

                if (numericTokens >= 2) {
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
