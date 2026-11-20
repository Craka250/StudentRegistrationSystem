
package ke.ac.kyu.srms.storage;

import ke.ac.kyu.srms.model.StudentPhoto;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class StudentPhotoStorage {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final long MAX_IMAGE_PIXELS = 40_000_000L;

    private static final Path STORAGE_DIRECTORY =
            Path.of(
                    System.getProperty("user.home"),
                    ".srms",
                    "student-photos"
            ).toAbsolutePath().normalize();

    /**
     * Stores a selected photograph using a unique filename.
     *
     * This method must only be called after the user confirms
     * the student registration or update.
     */
    public StudentPhoto store(File sourceFile) throws IOException {

        if (sourceFile == null) {
            throw new IllegalArgumentException(
                    "No photograph was selected."
            );
        }

        if (!sourceFile.exists() || !sourceFile.isFile()) {
            throw new IOException(
                    "The selected photograph could not be found."
            );
        }

        long fileSize = Files.size(sourceFile.toPath());

        if (fileSize <= 0 || fileSize > MAX_FILE_SIZE) {
            throw new IOException(
                    "Photograph size must be greater than 0 and "
                    + "must not exceed 5 MB."
            );
        }

        // Inspect the actual image format instead of trusting
        // the uploaded filename or extension.
        String format = validateImage(sourceFile.toPath());

        String extension;
        String contentType;

        if ("jpeg".equals(format)) {
            extension = ".jpg";
            contentType = "image/jpeg";
        } else if ("png".equals(format)) {
            extension = ".png";
            contentType = "image/png";
        } else {
            throw new IOException(
                    "Only JPEG and PNG photographs are supported."
            );
        }

        try {
            Files.createDirectories(STORAGE_DIRECTORY);

            String filename = UUID.randomUUID() + extension;

            Path destination = STORAGE_DIRECTORY.resolve(filename);

            Files.copy(
                    sourceFile.toPath(),
                    destination
            );

            try {
                long storedSize = Files.size(destination);

                if (storedSize <= 0 || storedSize > MAX_FILE_SIZE) {
                    throw new IOException(
                            "The stored photograph exceeds the allowed size."
                    );
                }

                // Use the copied file's actual image metadata.
                String copiedFormat = validateImage(destination);

                if (!format.equals(copiedFormat)) {
                    throw new IOException(
                            "The photograph format changed during storage."
                    );
                }

                return new StudentPhoto(
                        destination,
                        sourceFile.getName(),
                        contentType,
                        storedSize
                );

            } catch (IOException | RuntimeException ex) {
                Files.deleteIfExists(destination);
                throw ex;
            }

        } catch (IOException ex) {
            throw new IOException(
                    "Unable to store the student photograph: "
                    + ex.getMessage(),
                    ex
            );
        }
    }

    /**
     * Validates the actual image content.
     *
     * Returns either "jpeg" or "png".
     */
    private String validateImage(Path imagePath) throws IOException {

        try (ImageInputStream input =
                ImageIO.createImageInputStream(imagePath.toFile())) {

            if (input == null) {
                throw new IOException(
                        "The selected file is not a readable image."
                );
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new IOException(
                        "The selected file is not a supported image."
                );
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(input, true, true);

                String format = reader.getFormatName()
                        .toLowerCase(java.util.Locale.ROOT);

                if (!format.equals("jpeg") && !format.equals("png")
                        && !format.equals("jpg")) {
                    throw new IOException(
                            "Only JPEG and PNG photographs are supported."
                    );
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0
                        || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new IOException(
                            "The photograph dimensions are invalid "
                            + "or too large."
                    );
                }

                BufferedImage image = reader.read(0);

                if (image == null) {
                    throw new IOException(
                            "The photograph could not be decoded."
                    );
                }

                return format.equals("jpg") ? "jpeg" : format;

            } finally {
                reader.dispose();
            }
        }
    }

    /**
     * Deletes a photograph only if it belongs to this application's
     * managed student-photo directory and has a generated filename.
     */
    public void deleteStoredPhoto(String storedPath) throws IOException {

        if (storedPath == null || storedPath.isBlank()) {
            return;
        }

        Path path = Path.of(storedPath).toAbsolutePath().normalize();

        if (!STORAGE_DIRECTORY.equals(path.getParent())) {
            throw new IOException(
                    "Refusing to delete a file outside student-photo storage."
            );
        }

        String filename = path.getFileName().toString();

        if (!filename.matches(
                "[0-9a-fA-F-]{36}\\.(jpg|png)"
        )) {
            throw new IOException(
                    "The photograph filename is not recognised."
            );
        }

        if (Files.isSymbolicLink(path)) {
            throw new IOException(
                    "Refusing to delete a symbolic link."
            );
        }

        Files.deleteIfExists(path);
    }

    public Path getStorageDirectory() {
        return STORAGE_DIRECTORY;
    }
}
