package com.orangehrm.config.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orangehrm.utils.ErrorHandler;

import java.io.File;
import java.io.IOException;

public class JsonMapperConfiguration {

    // Thread-safe ObjectMapper instance
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private JsonMapperConfiguration() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static String serialize(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return handleSerializationError(e, "serialize", "Failed to serialize object to JSON");
        }
    }

    public static String serializePretty(Object value) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return handleSerializationError(e, "serializePretty", "Failed to serialize object to pretty-printed JSON");
        }
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            return handleDeserializationError(e, "deserialize", "Failed to deserialize JSON to object");
        }
    }

    public static <T> T deserialize(File file, Class<T> clazz) throws IOException {
        try {
            return OBJECT_MAPPER.readValue(file, clazz);
        } catch (JsonProcessingException e) {
            return handleDeserializationError(e, "deserialize", "Failed to deserialize JSON file to object");
        }
    }

    public static <T> T deserialize(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            return handleDeserializationError(e, "deserialize", "Failed to deserialize JSON to object with TypeReference");
        }
    }

    public static <T> T convertToJsonAndBack(Object inputObject, Class<T> targetType) {
        try {
            if (inputObject == null) return null;
            return OBJECT_MAPPER.convertValue(inputObject, targetType);
        } catch (IllegalArgumentException e) {
            return handleDeserializationError(e, "convertToJsonAndBack", "Failed to convert object to JSON and back");
        }
    }

    private static <T> T handleDeserializationError(Exception e, String method, String message) {
        ErrorHandler.logError(e, method, message);
        throw new JsonProcessingExceptionWrapper(message, e);
    }

    private static String handleSerializationError(Exception e, String method, String message) {
        ErrorHandler.logError(e, method, message);
        throw new JsonProcessingExceptionWrapper(message, e);
    }

    public static class JsonProcessingExceptionWrapper extends RuntimeException {
        public JsonProcessingExceptionWrapper(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
