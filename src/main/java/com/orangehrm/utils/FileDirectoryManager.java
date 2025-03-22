package com.orangehrm.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDirectoryManager {
    /**
     * Creates a directory if it does not already exist.
     *
     * @param dirPath The path to the directory to create.
     * @throws IOException If the directory cannot be created due to an I/O error.
     */
    public static void createDirIfNotExists(String dirPath) throws IOException {
        if (dirPath == null) {
            throw new IllegalArgumentException("Directory path cannot be null.");
        }
        try {
            Path dir = Paths.get(dirPath);
            Files.createDirectories(dir);
        } catch (Exception error) {
            ErrorHandler.logError(
                    error,
                    "createDirIfNotExists",
                    "Failed to ensure directory exists: " + dirPath
            );
            throw error;
        }
    }

    /**
     * Creates a file in a specified directory if it does not already exist.
     *
     * @param dirPath  The path to the directory to create the file in.
     * @param fileName The name of the file to create.
     * @throws IOException If the file cannot be created due to an I/O error.
     */
    public static void createFileIfNotExists(String dirPath, String fileName) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        try {
            createDirIfNotExists(dirPath);
            Path filePath = Paths.get(dirPath, fileName);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (Exception error) {
            ErrorHandler.logError(
                    error,
                    "createFileIfNotExists",
                    "Failed to ensure file exists: " + fileName + " in " + dirPath
            );
            throw error;
        }
    }

    /**
     * Utility method to check if a file exists at the given path.
     *
     * @param filePath The path of the file to check.
     * @return true if the file exists, false otherwise.
     */
    public static boolean doesFileExist(String filePath) {
        return filePath != null && Files.exists(Paths.get(filePath));
    }

    public static void ensureDirectoryAndFileExists(String directoryPath, String filename) throws IOException {
        try {
            FileDirectoryManager.createDirIfNotExists(directoryPath);
            FileDirectoryManager.createFileIfNotExists(directoryPath, filename);
        } catch (Exception error){
            ErrorHandler.logError(error, "ensureDirectoryAndFileExist",
                    "Failed to ensure directory: " + "'" + directoryPath + "'" + " and file exists: " + "'" + filename + "'");
            throw error;
        }
    }

    /**
     * Safely deletes a file if it exists.
     *
     * @param filePath The path to the file to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public static boolean safeDeleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path) && !Files.isDirectory(path)) {
                Files.delete(path);
                return true;
            }
            return false;
        } catch (Exception error) {
            ErrorHandler.logError(
                    error,
                    "safeDeleteFile",
                    "Failed to delete file: " + filePath
            );
            return false;
        }
    }

    /**
     * Reads the content of a file into a string.
     *
     * @param filePath The path to the file.
     * @return The content of the file as a string.
     * @throws IOException If the file cannot be read.
     */
    public static String readFileContent(String filePath) throws IOException {
        if (!doesFileExist(filePath)) {
            throw new IOException("File does not exist: " + filePath);
        }
        try {
            return Files.readString(Paths.get(filePath));
        } catch (Exception error) {
            ErrorHandler.logError(
                    error,
                    "readFileContent",
                    "Failed to read file content: " + filePath
            );
            throw error;
        }
    }

    /**
     * Writes content to a file, creating it if it doesn't exist.
     *
     * @param filePath The path to the file.
     * @param content The content to write.
     * @param append Whether to append to the file or overwrite it.
     * @throws IOException If the file cannot be written to.
     */
    public static void writeFileContent(String filePath, String content, boolean append) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();
            if (parentDir != null) {
                createDirIfNotExists(parentDir.toString());
            }
            if (append && Files.exists(path)) {
                Files.writeString(path, content, java.nio.file.StandardOpenOption.APPEND);
            } else {
                Files.writeString(path, content);
            }
        } catch (Exception error) {
            ErrorHandler.logError(
                    error,
                    "writeFileContent",
                    "Failed to write content to file: " + filePath
            );
            throw error;
        }
    }

    /**
     * Copies a file from source to destination.
     *
     * @param sourcePath Path of the source file
     * @param destinationPath Path of the destination file
     * @param replaceExisting Whether to replace the destination if it exists
     * @throws IOException If an I/O error occurs
     */
    public static void copyFile(String sourcePath, String destinationPath, boolean replaceExisting) throws IOException {
        if (sourcePath == null || destinationPath == null) {
            throw new IllegalArgumentException("Source and destination paths cannot be null.");
        }
        try {
            Path source = Paths.get(sourcePath);
            Path destination = Paths.get(destinationPath);

            // Ensure parent directory exists
            Path destinationParent = destination.getParent();
            if (destinationParent != null) {
                createDirIfNotExists(destinationParent.toString());
            }

            if (replaceExisting) {
                Files.copy(source, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(source, destination);
            }
        } catch (Exception error) {
            ErrorHandler.logError(
                    error,
                    "copyFile",
                    "Failed to copy file from " + sourcePath + " to " + destinationPath
            );
            throw error;
        }
    }
}
