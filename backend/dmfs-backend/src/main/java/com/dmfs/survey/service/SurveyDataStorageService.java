package com.dmfs.survey.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SurveyDataStorageService {

    private final Path storageDirectory;

    public SurveyDataStorageService(
            @Value("${dmfs.storage.survey-data:storage/survey-data}")
            String storagePath
    ) {
        this.storageDirectory = Paths.get(storagePath)
                .toAbsolutePath()
                .normalize();
    }

    /**
     * Stores an uploaded survey input file and returns
     * the storage key used to locate the file.
     */
    public String store(
            MultipartFile file,
            String packageCode
    ) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot store an empty file."
            );
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null
                || originalFileName.isBlank()) {

            throw new IllegalArgumentException(
                    "Uploaded file name is missing."
            );
        }

        /*
         * Generate a safe storage file name rather than
         * trusting the original client-provided name.
         */
        String extension = extractExtension(
                originalFileName
        );

        String safeFileName =
                UUID.randomUUID()
                        .toString()
                        + "."
                        + extension;

        Path packageDirectory = storageDirectory
                .resolve(packageCode)
                .normalize();

        /*
         * Prevent path traversal.
         */
        if (!packageDirectory.startsWith(storageDirectory)) {
            throw new IllegalArgumentException(
                    "Invalid storage path."
            );
        }

        try {

            Files.createDirectories(
                    packageDirectory
            );

            Path targetPath = packageDirectory
                    .resolve(safeFileName)
                    .normalize();

            if (!targetPath.startsWith(packageDirectory)) {
                throw new IllegalArgumentException(
                        "Invalid file storage path."
                );
            }

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            /*
             * Storage key is relative to the configured
             * survey-data storage directory.
             */
            return packageCode
                    + "/"
                    + safeFileName;

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to store survey data file.",
                    e
            );
        }
    }

    /**
     * Calculates SHA-256 checksum of the uploaded file.
     */
    public String calculateChecksum(
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot calculate checksum for an empty file."
            );
        }

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            try (InputStream inputStream =
                         file.getInputStream()) {

                byte[] buffer = new byte[8192];

                int bytesRead;

                while ((bytesRead =
                        inputStream.read(buffer)) != -1) {

                    digest.update(
                            buffer,
                            0,
                            bytesRead
                    );
                }
            }

            return HexFormat.of()
                    .formatHex(
                            digest.digest()
                    );

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is not available.",
                    e
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to calculate file checksum.",
                    e
            );
        }
    }

    /**
     * Extracts a file extension without the dot.
     */
    private String extractExtension(
            String fileName
    ) {

        int lastDot =
                fileName.lastIndexOf('.');

        if (lastDot < 0
                || lastDot == fileName.length() - 1) {

            throw new IllegalArgumentException(
                    "Uploaded file does not have a valid extension."
            );
        }

        return fileName
                .substring(lastDot + 1)
                .toLowerCase();
    }
}