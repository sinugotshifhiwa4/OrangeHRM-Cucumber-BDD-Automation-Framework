package com.orangehrm.utils;

import org.apache.logging.log4j.Logger;

public class ErrorHandler {

    private static final Logger logger = LoggerUtils.getLogger(ErrorHandler.class);

    private ErrorHandler() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Logs an error with detailed information and returns the error for further handling.
     *
     * @param error        The throwable instance representing the error (required)
     * @param methodName   The name of the method where the error occurred (required)
     * @param errorMessage Additional context about the error (optional)
     * @throws IllegalArgumentException if required parameters are null or invalid
     */
    public static <CustomException extends Throwable> void logError(CustomException error, String methodName, String errorMessage) {
        validateParameters(error, "error");
        validateParameters(methodName, "methodName");

        String detailedMessage = buildErrorMessage(methodName, errorMessage, error);
        logger.error(detailedMessage, error);
    }

    public static void validateParameters(Object param, String paramName) {
        if (param == null) {
            throw new IllegalArgumentException(paramName + " cannot be null");
        }
        if (param instanceof String && ((String) param).isBlank()) {
            throw new IllegalArgumentException(paramName + " cannot be empty or blank");
        }
    }

    /**
     * Validates that required parameters are not null or empty
     */
    public static void validateParameters(String... params) {
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            if (param == null || param.trim().isEmpty()) {
                throw new IllegalArgumentException("Parameter at position " + i + " cannot be null or empty");
            }
        }
    }

    /**
     * Builds a detailed error message with additional context.
     *
     * @param methodName   The name of the method where the error occurred (required)
     * @param errorMessage Additional context about the error (optional)
     * @param error        The throwable instance representing the error (required)
     * @return A detailed error message with additional context
     */
    private static String buildErrorMessage(String methodName, String errorMessage, Throwable error) {
        return String.format(
                "Error in method '%s': %s. Error details: %s",
                methodName,
                errorMessage != null ? errorMessage : "No additional error message provided",
                error.getMessage()
        );
    }

    /**
     * Creates a custom exception instance with the provided detailed message.
     *
     * @param errorType       The custom exception type to create (optional)
     * @param detailedMessage The detailed message to pass to the exception constructor (required)
     * @return The created exception instance
     * @throws IllegalArgumentException if detailedMessage is null or empty
     * @throws CustomException                       The created exception instance
     */
    private static <CustomException extends RuntimeException> CustomException createException(
            Class<CustomException> errorType,
            String detailedMessage) {
        if (errorType == null) {
            @SuppressWarnings("unchecked")
            CustomException runtimeException = (CustomException) new RuntimeException(detailedMessage);
            return runtimeException;
        }

        try {
            return errorType.getConstructor(String.class).newInstance(detailedMessage);
        } catch (ReflectiveOperationException e) {
            logger.warn("Failed to create custom exception of type: {}. Defaulting to RuntimeException", errorType.getName());
            @SuppressWarnings("unchecked")
            CustomException runtimeException = (CustomException) new RuntimeException(detailedMessage);
            return runtimeException;
        }
    }

    public static void logAndRethrow(Exception error, String methodName, String message) throws Exception {
        ErrorHandler.logError(error, methodName, message);
        throw error;
    }

    public static void logAndThrowIfEmptyRuntime(Exception error, String methodName, String message) {
        ErrorHandler.logError(error, methodName, message);
        throw new RuntimeException(message, error);
    }

    public static void logPropertySource(String key, String source, String value) {
        logger.info("Using {} for '{}': '{}'", source, key, value);
    }
}
