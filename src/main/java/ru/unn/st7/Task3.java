package ru.unn.st7;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Task3 {
    private static final String FORECAST_URL =
        "https://api.open-meteo.com/v1/forecast?latitude=56&longitude=44&hourly=temperature_2m,rain&current=cloud_cover&timezone=Europe%2FMoscow&forecast_days=1&wind_speed_unit=ms";

    private Task3() {
    }

    public static void saveForecastToFile(WebDriver webDriver, String outputPath) throws IOException {
        webDriver.get(FORECAST_URL);

        WebElement pre = webDriver.findElement(By.tagName("pre"));
        JSONObject root = new JSONObject(pre.getText());
        JSONObject hourly = root.getJSONObject("hourly");
        JSONArray times = hourly.getJSONArray("time");
        JSONArray temperatures = hourly.getJSONArray("temperature_2m");
        JSONArray rain = hourly.getJSONArray("rain");

        List<String> lines = new ArrayList<>();
        lines.add(String.format("%-3s\t%-16s\t%-12s\t%-12s", "№", "Дата/время", "Температура", "Осадки (мм)"));
        for (int i = 0; i < times.length(); i++) {
            String row = String.format(
                Locale.US,
                "%-3d\t%-16s\t%-12.1f\t%-12.2f",
                i + 1,
                times.getString(i),
                temperatures.getDouble(i),
                rain.getDouble(i)
            );
            lines.add(row);
        }

        Path path = Path.of(outputPath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
        System.out.println("Задание 3. Прогноз сохранен в файл: " + outputPath);
    }
}
