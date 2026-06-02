package ru.unn.st7;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

    public static void main(String[] args) {
        setupDriverExecutable();
        ChromeOptions configOptions = new ChromeOptions();
        configOptions.addArguments("--no-sandbox");
        configOptions.addArguments("--disable-dev-shm-usage");

        WebDriver driverInstance = new ChromeDriver(configOptions);
        try {
            String passResult = fetchSecretKey(driverInstance);
            System.out.println("Задание 1. Сгенерированный пароль: " + passResult);
            Task2.printClientIp(driverInstance);
            Task3.saveForecastToFile(driverInstance, "result/forecast.txt");
        } catch (Exception error) {
            System.err.println("Execution Error:");
            error.printStackTrace();
        } finally {
            driverInstance.quit();
        }
    }

    private static void setupDriverExecutable() {
        String currentDriverProp = System.getProperty("webdriver.chrome.driver");
        if (currentDriverProp != null && !currentDriverProp.strip().isEmpty()) {
            return;
        }

        String locationEnv = System.getenv("CHROMEDRIVER_PATH");
        if (locationEnv != null && !locationEnv.strip().isEmpty()) {
            System.setProperty("webdriver.chrome.driver", locationEnv);
            return;
        }

        Path homebrewLocation = Path.of("/opt/homebrew/bin/chromedriver");
        if (Files.exists(homebrewLocation)) {
            System.setProperty("webdriver.chrome.driver", homebrewLocation.toAbsolutePath().toString());
        }
    }

    private static String fetchSecretKey(WebDriver driverInstance) {
        driverInstance.get("https://www.calculator.net/password-generator.html");

        executeSubmitAction(driverInstance);
        for (int attempt = 0; attempt < 20; attempt++) {
            String parsedKey = findKeyWithScripts(driverInstance);
            if (parsedKey != null && !parsedKey.strip().isEmpty()) {
                return parsedKey;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return scanRawHtml(driverInstance.getPageSource()).stream()
                .max(Comparator.comparingInt(String::length))
                .orElse("Пароль не найден");
    }

    private static void executeSubmitAction(WebDriver driverInstance) {
        JavascriptExecutor executor = (JavascriptExecutor) driverInstance;
        executor.executeScript(
                "const targetBtn = Array.from(document.querySelectorAll('button, input[type=\"button\"], input[type=\"submit\"]'))"
                        + ".find(elem => ((elem.innerText || '') + ' ' + (elem.value || '')).toLowerCase().includes('generate'));"
                        + "if (targetBtn) targetBtn.click();"
        );
    }

    private static List<String> scanRawHtml(String pageSource) {
        List<String> matchedTokens = new ArrayList<>();
        Pattern htmlPattern = Pattern.compile("value=\\\"([^\\\"]{8,})\\\"");
        Matcher htmlMatcher = htmlPattern.matcher(pageSource);
        while (htmlMatcher.find()) {
            String item = htmlMatcher.group(1).trim();
            boolean isCompliant = !item.contains(" ")
                    && item.length() >= 8
                    && !item.equalsIgnoreCase("password")
                    && item.matches(".*[A-Za-z].*")
                    && item.matches(".*\\d.*");
            if (isCompliant) {
                matchedTokens.add(item);
            }
        }
        return matchedTokens;
    }

    private static String findKeyWithScripts(WebDriver driverInstance) {
        JavascriptExecutor executor = (JavascriptExecutor) driverInstance;
        Object scriptOutput = executor.executeScript(
                "const targetQueries = ["
                        + "'#generated_password', '#result', '[id*=pass]', '[id*=result]', '[class*=pass]', '[class*=result]',"
                        + "'input[readonly]', 'input[type=text]', 'textarea'];"
                        + "const matches = [];"
                        + "targetQueries.forEach(query => document.querySelectorAll(query).forEach(element => {"
                        + "  const textVal = (element.value || element.textContent || '').trim();"
                        + "  const isValidPass = textVal.length >= 8 && !textVal.includes(' ') && textVal.toLowerCase() !== 'password'"
                        + "    && /[A-Za-z]/.test(textVal) && /\\d/.test(textVal);"
                        + "  if (isValidPass) matches.push(textVal);"
                        + "}));"
                        + "matches.sort((first, second) => second.length - first.length);"
                        + "return matches.length ? matches[0] : '';"
        );
        return scriptOutput == null ? "" : scriptOutput.toString();
    }
}
