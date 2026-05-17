package ru.unn.st7;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

    public static void main(String[] args) {
        configureChromeDriverPath();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver webDriver = new ChromeDriver(options);
        try {
            String generatedPassword = readGeneratedPassword(webDriver);
            System.out.println("Задание 1. Сгенерированный пароль: " + generatedPassword);
            Task2.printClientIp(webDriver);
            Task3.saveForecastToFile(webDriver, "result/forecast.txt");
        } catch (Exception e) {
            System.out.println("Error");
            System.out.println(e.toString());
        } finally {
            webDriver.quit();
        }
    }

    private static void configureChromeDriverPath() {
        String existingProperty = System.getProperty("webdriver.chrome.driver");
        if (existingProperty != null && !existingProperty.isBlank()) {
            return;
        }

        String envPath = System.getenv("CHROMEDRIVER_PATH");
        if (envPath != null && !envPath.isBlank()) {
            System.setProperty("webdriver.chrome.driver", envPath);
            return;
        }

        Path defaultPath = Path.of("/opt/homebrew/bin/chromedriver");
        if (Files.exists(defaultPath)) {
            System.setProperty("webdriver.chrome.driver", defaultPath.toString());
        }
    }

    private static String readGeneratedPassword(WebDriver webDriver) {
        webDriver.get("https://www.calculator.net/password-generator.html");

        clickGenerateButtonIfPresent(webDriver);
        for (int i = 0; i < 20; i++) {
            String password = extractPasswordViaJs(webDriver);
            if (password != null && !password.isBlank()) {
                return password;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return collectFromPageSource(webDriver.getPageSource()).stream()
            .max(Comparator.comparingInt(String::length))
            .orElse("Пароль не найден");
    }

    private static void clickGenerateButtonIfPresent(WebDriver webDriver) {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript(
            "const b = Array.from(document.querySelectorAll('button,input[type=\"button\"],input[type=\"submit\"]'))"
                + ".find(e => ((e.innerText || '') + ' ' + (e.value || '')).toLowerCase().includes('generate'));"
                + "if (b) b.click();"
        );
    }

    private static List<String> collectFromPageSource(String pageSource) {
        List<String> values = new java.util.ArrayList<>();
        Pattern pattern = Pattern.compile("value=\\\"([^\\\"]{8,})\\\"");
        Matcher matcher = pattern.matcher(pageSource);
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            boolean valid = !candidate.contains(" ")
                && candidate.length() >= 8
                && !candidate.equalsIgnoreCase("password")
                && candidate.matches(".*[A-Za-z].*")
                && candidate.matches(".*\\d.*");
            if (valid) {
                values.add(candidate);
            }
        }
        return values;
    }

    private static String extractPasswordViaJs(WebDriver webDriver) {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        Object result = js.executeScript(
            "const selectors = ["
                + "'#generated_password', '#result', '[id*=pass]', '[id*=result]', '[class*=pass]', '[class*=result]',"
                + "'input[readonly]', 'input[type=text]', 'textarea'];"
                + "const vals = [];"
                + "selectors.forEach(s => document.querySelectorAll(s).forEach(e => {"
                + "  const v = (e.value || e.textContent || '').trim();"
                + "  const ok = v.length >= 8 && !v.includes(' ') && v.toLowerCase() !== 'password'"
                + "    && /[A-Za-z]/.test(v) && /\\d/.test(v);"
                + "  if (ok) vals.push(v);"
                + "}));"
                + "vals.sort((a,b) => b.length-a.length);"
                + "return vals.length ? vals[0] : '';"
        );
        return result == null ? "" : result.toString();
    }
}
