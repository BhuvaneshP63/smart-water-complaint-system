
package com.swcms.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDirectory;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".webp");

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    // Store uploaded image
    public String storeFile(MultipartFile file, String folder) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Image size must not exceed 5 MB");
        }

        String originalFilename =
                StringUtils.cleanPath(file.getOriginalFilename() == null
                        ? "" : file.getOriginalFilename());

        int extensionIndex = originalFilename.lastIndexOf('.');

        if (extensionIndex < 0) {
            throw new IllegalArgumentException(
                    "Image must have a valid file extension");
        }

        String extension = originalFilename
                .substring(extensionIndex)
                .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Only JPG, JPEG, PNG and WEBP images are allowed");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid image content type");
        }

        if (folder == null ||
                !Set.of("complaints", "feedback", "completion", "worker-feedback")
                        .contains(folder)) {
            throw new IllegalArgumentException("Invalid upload folder");
        }

        try {
            if (!hasValidImageSignature(file)) {
                throw new IllegalArgumentException("Uploaded file content is not a valid JPG, PNG or WEBP image");
            }
            Path rootPath = Paths.get(uploadDirectory)
                    .toAbsolutePath().normalize();

            Path targetDirectory = rootPath.resolve(folder).normalize();

            if (!targetDirectory.startsWith(rootPath)) {
                throw new IllegalArgumentException(
                        "Invalid upload directory");
            }

            Files.createDirectories(targetDirectory);

            String newFilename = UUID.randomUUID() + extension;

            Path targetPath = targetDirectory
                    .resolve(newFilename).normalize();

            if (!targetPath.startsWith(rootPath)) {
                throw new IllegalArgumentException(
                        "Invalid file path");
            }

            file.transferTo(targetPath);

            return folder + "/" + newFilename;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not store uploaded image", e);
        }
    }

    private boolean hasValidImageSignature(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        if (bytes.length < 12) return false;
        boolean jpeg = (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 &&
                (bytes[2] & 0xff) == 0xff;
        boolean png = (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47;
        boolean webp = bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
                bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
        return jpeg || png || webp;
    }

    // Retrieve stored image safely
    public Resource loadFile(String relativePath) {

        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("Image path is empty");
        }

        try {
            Path rootPath = Paths.get(uploadDirectory)
                    .toAbsolutePath().normalize();

            Path filePath = rootPath.resolve(relativePath)
                    .normalize();

            if (!filePath.startsWith(rootPath) ||
                    filePath.equals(rootPath)) {
                throw new IllegalArgumentException("Invalid image path");
            }

            if (!Files.isRegularFile(filePath) ||
                    !Files.isReadable(filePath)) {
                throw new IllegalArgumentException("Image not found");
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Image not found");
            }

            return resource;

        } catch (IOException e) {
            throw new RuntimeException("Could not load image", e);
        }
    }
}
