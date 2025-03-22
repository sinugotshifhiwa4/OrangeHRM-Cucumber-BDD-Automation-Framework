package com.orangehrm.pages.base;

import com.orangehrm.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class providing enhanced dropdown selection functionality for Selenium WebElements.
 * Supports selection by visible text, value, and index with improved error handling and validation.
 */
public class DropdownHandler {

    private static final Logger logger = LoggerUtils.getLogger(DropdownHandler.class);

    /**
     * Selects a dropdown option by its visible text.
     * First attempts direct selection, then falls back to searching through options if needed.
     *
     * @param select      The Selenium Select object
     * @param options     List of available WebElements in the dropdown
     * @param value      The text value to select
     * @param elementName The name/identifier of the dropdown element for logging
     * @throws IllegalArgumentException if the specified option is not found
     */
    public static void selectByVisibleText(Select select, List<WebElement> options, Object value, String elementName) {
        String textValue = validateString(value, DropdownSelector.VISIBLE_TEXT.getSelector());

        try {
            select.selectByVisibleText(textValue);
        } catch (Exception error) {
            Optional<WebElement> matchingOption = options.stream()
                    .filter(opt -> opt.getText().equals(textValue))
                    .findFirst();

            if (matchingOption.isEmpty()) {
                String availableOptions = options.stream()
                        .map(WebElement::getText)
                        .filter(text -> !text.isEmpty())
                        .collect(Collectors.joining(", "));

                logger.error("Option '{}' not found in '{}'. Available options: {}",
                        textValue, elementName, availableOptions);
                throw new IllegalArgumentException(String.format(
                        "Option '%s' not found in dropdown '%s'", textValue, elementName));
            }

            select.selectByVisibleText(textValue);
        }
    }

    /**
     * Selects a dropdown option by its value attribute.
     *
     * @param select      The Selenium Select object
     * @param options     List of available WebElements in the dropdown
     * @param value      The value attribute to select
     * @param elementName The name/identifier of the dropdown element for logging
     * @throws IllegalArgumentException if the specified value is not found
     */
    public static void selectByValue(Select select, List<WebElement> options, Object value, String elementName) {
        String stringValue = validateString(value, DropdownSelector.VALUE.getSelector());

        Optional<WebElement> matchingOption = options.stream()
                .filter(opt -> stringValue.equals(opt.getDomProperty("value")))
                .findFirst();

        if (matchingOption.isEmpty()) {
            String availableValues = options.stream()
                    .map(opt -> opt.getDomProperty("value"))
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(String.format(
                    "Option with value '%s' not found in dropdown '%s'. Available values: %s",
                    stringValue, elementName, availableValues));
        }

        select.selectByValue(stringValue);
    }

    /**
     * Selects a dropdown option by its index.
     *
     * @param select      The Selenium Select object
     * @param options     List of available WebElements in the dropdown
     * @param value      The index to select (can be Integer or String)
     * @param elementName The name/identifier of the dropdown element for logging
     * @throws IllegalArgumentException if the index is invalid or out of bounds
     */
    public static void selectByIndex(Select select, List<WebElement> options, Object value, String elementName) {
        int index = validateInteger(value);
        if (index < 0 || index >= options.size()) {
            throw new IllegalArgumentException(String.format(
                    "Index %d is out of bounds in dropdown '%s'. Available range: 0-%d",
                    index, elementName, options.size() - 1));
        }
        select.selectByIndex(index);
    }

    /**
     * Validates and converts input to a non-empty string.
     *
     * @param value      The value to validate
     * @param methodType The selection method being used
     * @return The validated string value
     * @throws IllegalArgumentException if the value is null or invalid
     */
    private static String validateString(Object value, String methodType) {
        if (value == null) {
            throw new IllegalArgumentException("Value for selection method '" + methodType + "' cannot be null");
        }
        if (!(value instanceof String stringValue) || stringValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Value for selection method '" + methodType + "' must be a non-empty string");
        }
        return stringValue.trim();
    }

    /**
     * Validates and converts input to an integer.
     *
     * @param value The value to validate (can be Integer or String)
     * @return The validated integer value
     * @throws IllegalArgumentException if the value is null or invalid
     */
    private static int validateInteger(Object value) {
        return switch (value) {
            case null -> throw new IllegalArgumentException("Index value cannot be null");
            case Integer index -> index;
            case String stringValue -> {
                try {
                    yield Integer.parseInt(stringValue.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid index value: '" + stringValue + "'. It must be a valid integer.");
                }
            }
            default -> throw new IllegalArgumentException(
                    "Index value must be an integer, but received: " + value.getClass().getSimpleName());
        };
    }
}