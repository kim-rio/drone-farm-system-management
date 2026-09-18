package com.dmfs.survey.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class XyzFileValidator implements SurveyDataFileValidator {

    private static final String EXTENSION = "xyz";

    @Override
    public boolean supports(String extension) {
        return EXTENSION.equalsIgnoreCase(extension);
    }

    @Override
    public ValidationResult validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ValidationResult.invalid(
                    "XYZ file is empty or was not provided."
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            return ValidationResult.invalid(
                    "XYZ file name is missing."
            );
        }

        if (!fileName.toLowerCase().endsWith(".xyz")) {
            return ValidationResult.invalid(
                    "File does not have the required .xyz extension."
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
            int validDataRows = 0;
            boolean hasHeader = false;

            while ((line = reader.readLine()) != null) {

                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                /*
                 * XYZ files may contain column/header information.
                 */
                String upperLine = line.toUpperCase();

                if (upperLine.contains("TIME")
                        || upperLine.contains("LON")
                        || upperLine.contains("LAT")
                        || upperLine.contains("TMI")
                        || upperLine.contains("X")
                        || upperLine.contains("Y")
                        || upperLine.contains("Z")) {

                    hasHeader = true;
                }

                /*
                 * Validate rows containing numeric XYZ-style data.
                 */
                if (isValidDataRow(line)) {
                    validDataRows++;
                }

                /*
                 * We only need enough rows to establish
                 * structural validity.
                 */
                if (validDataRows >= 3) {
                    break;
                }
            }

            if (lineNumber == 0) {
                return ValidationResult.invalid(
                        "XYZ file does not contain any readable content."
                );
            }

            if (validDataRows == 0) {
                return ValidationResult.invalid(
                        "XYZ file does not contain recognizable numeric data rows."
                );
            }

            return ValidationResult.valid(
                    "XYZ file structure is valid."
            );

        } catch (IOException e) {

            return ValidationResult.invalid(
                    "Unable to read XYZ file: " + e.getMessage()
            );
        }
    }

    private boolean isValidDataRow(String line) {

        String[] tokens = line.split("[,\\s]+");

        if (tokens.length < 3) {
            return false;
        }

        int numericTokens = 0;

        for (String token : tokens) {

            if (isNumeric(token)) {
                numericTokens++;
            }
        }

        return numericTokens >= 3;
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