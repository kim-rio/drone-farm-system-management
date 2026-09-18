package com.dmfs.survey.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class KmlFileValidator implements SurveyDataFileValidator {

    private static final String EXTENSION = "kml";

    @Override
    public boolean supports(String extension) {
        return EXTENSION.equalsIgnoreCase(extension);
    }

    @Override
    public ValidationResult validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ValidationResult.invalid(
                    "KML file is empty or was not provided."
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            return ValidationResult.invalid(
                    "KML file name is missing."
            );
        }

        if (!fileName.toLowerCase().endsWith(".kml")) {
            return ValidationResult.invalid(
                    "File does not have the required .kml extension."
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

            StringBuilder content = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {

                content.append(line)
                        .append('\n');

                /*
                 * Prevent unnecessarily loading a very large
                 * file during this basic structural check.
                 */
                if (content.length() > 2_000_000) {
                    break;
                }
            }

            String xml = content.toString().trim();

            if (xml.isEmpty()) {
                return ValidationResult.invalid(
                        "KML file does not contain readable content."
                );
            }

            String lower = xml.toLowerCase();

            if (!lower.contains("<kml")) {
                return ValidationResult.invalid(
                        "KML root element <kml> was not found."
                );
            }

            if (!lower.contains("</kml>")) {
                return ValidationResult.invalid(
                        "KML closing element </kml> was not found."
                );
            }

            if (!lower.contains("<document")
                    && !lower.contains("<folder")
                    && !lower.contains("<placemark")) {

                return ValidationResult.invalid(
                        "KML file does not contain recognizable KML content."
                );
            }

            if (!lower.contains("<coordinates")) {
                return ValidationResult.invalid(
                        "KML file does not contain coordinate geometry."
                );
            }

            return ValidationResult.valid(
                    "KML file structure is valid."
            );

        } catch (IOException e) {

            return ValidationResult.invalid(
                    "Unable to read KML file: " + e.getMessage()
            );
        }
    }
}