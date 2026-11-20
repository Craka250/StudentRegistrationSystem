
package ke.ac.kyu.srms.model;

import java.nio.file.Path;

public class StudentPhoto {

    private final Path filePath;
    private final String originalFilename;
    private final String contentType;
    private final long fileSize;

    public StudentPhoto(
            Path filePath,
            String originalFilename,
            String contentType,
            long fileSize
    ) {
        if (filePath == null) {
            throw new IllegalArgumentException("Photo path is required.");
        }

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Original filename is required.");
        }

        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type is required.");
        }

        if (fileSize <= 0 || fileSize > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Photo size must be between 1 byte and 5 MB."
            );
        }

        this.filePath = filePath;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }
}
