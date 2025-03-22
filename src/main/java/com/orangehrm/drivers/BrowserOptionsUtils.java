package com.orangehrm.drivers;

import com.orangehrm.utils.ErrorHandler;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BrowserOptionsUtils {

    public static ChromeOptions getChromeOptions(String... arguments) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(processArguments(arguments));
        return options;
    }

    public static FirefoxOptions getFirefoxOptions(String... arguments) {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments(processArguments(arguments));
        return options;
    }

    public static EdgeOptions getEdgeOptions(String... arguments) {
        EdgeOptions options = new EdgeOptions();
        options.addArguments(processArguments(arguments));
        return options;
    }

    private static List<String> processArguments(String[] arguments) {
        try {
            List<String> processedArgs = (arguments == null)
                    ? new ArrayList<>()
                    : Arrays.stream(arguments).map(String::trim).filter(arg -> !arg.isEmpty()).toList();

            // Ensure "--incognito" is always included
            if (!processedArgs.contains("--incognito")) {
                processedArgs = new ArrayList<>(processedArgs); // Convert to mutable list
                processedArgs.addFirst("--incognito"); // Add "--incognito" at the beginning
            }

            return processedArgs;
        } catch (Exception error) {
            ErrorHandler.logError(error, "processArguments", "Failed to process arguments");
            throw new RuntimeException("Failed to process arguments", error);
        }
    }
}
