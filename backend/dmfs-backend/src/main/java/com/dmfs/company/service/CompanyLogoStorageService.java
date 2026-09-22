package com.dmfs.company.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class CompanyLogoStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp"
    );

    private final Path storageDirectory;

    public CompanyLogoStorageService(
            @Value("${dmfs.storage.company-logos:storage/company-logos}")
            String storagePath
    ) {
        this.storageDirectory = Paths.get(storagePath)
                .toAbsolutePath()
                .normalize();
    }

    public String store(MultipartFile file, Long companyId) {

        validate(file);

        String extension = extensionFor(file.getContentType());

        Path companyDirectory = storageDirectory
                .resolve(String.valueOf(companyId))
                .normalize();

        if (!companyDirectory.startsWith(storageDirectory)) {
            throw new IllegalArgumentException("Invalid company logo storage path.");
        }

        try {
            Files.createDirectories(companyDirectory);

            String fileName =
                    UUID.randomUUID()
                            + extension;

            Path target =
                    companyDirectory
                            .resolve(fileName)
                            .normalize();

            if (!target.startsWith(companyDirectory)) {
                throw new IllegalArgumentException(
                        "Invalid company logo storage path."
                );
            }

            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return companyId + "/" + fileName;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to store company logo.",
                    e
            );
        }
    }

    public void delete(String relativePath) {

        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        Path target =
                storageDirectory
                        .resolve(relativePath)
                        .normalize();

        if (!target.startsWith(storageDirectory)) {
            throw new IllegalArgumentException(
                    "Invalid company logo path."
            );
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to delete previous company logo.",
                    e
            );
        }
    }

    public Path resolve(String relativePath) {

        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException(
                    "Company logo is not configured."
            );
        }

        Path target =
                storageDirectory
                        .resolve(relativePath)
                        .normalize();

        if (!target.startsWith(storageDirectory)) {
            throw new IllegalArgumentException(
                    "Invalid company logo path."
            );
        }

        return target;
    }

    private void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a company logo."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Company logo must be 5 MB or smaller."
            );
        }

        String contentType =
                file.getContentType();

        if (
                contentType == null
                || !ALLOWED_TYPES.contains(
                        contentType.toLowerCase(Locale.ROOT)
                )
        ) {
            throw new IllegalArgumentException(
                    "Company logo must be PNG, JPEG, or WebP."
            );
        }
    }

    private String extensionFor(String contentType) {

        return switch (
                contentType.toLowerCase(Locale.ROOT)
        ) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            default ->
                    throw new IllegalArgumentException(
                            "Unsupported company logo format."
                    );
        };
    }
}
