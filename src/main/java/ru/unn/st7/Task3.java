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
    private static final String METEO_ENDPOINT =
            "https://api.open-meteo.com/v1/forecast?latitude=56&longitude=44&hourly=temperature_2m,rain&current=cloud_cover&timezone=Europe%2FMoscow&forecast_days=1&wind_speed_unit=ms";

    private Task3() {
    }

    public static void saveForecastToFile(WebDriver driverInstance, String fileDestination) throws IOException {
        driverInstance.get(METEO_ENDPOINT);

        WebElement textHolder = driverInstance.findElement(By.tagName("pre"));
        JSONObject jsonRoot = new JSONObject(textHolder.getText());
        JSONObject hourlyData = jsonRoot.getJSONObject("hourly");
        JSONArray timeline = hourlyData.getJSONArray("time");
        JSONArray tempRecords = hourlyData.getJSONArray("temperature_2m");
        JSONArray rainRecords = hourlyData.getJSONArray("rain");

        List<String> textRows = new ArrayList<>();
        textRows.add(String.format("%-3s\t%-16s\t%-12s\t%-12s", "№", "Дата/время", "Температура", "Осадки (мм)"));

        for (int index = 0; index < timeline.length(); index++) {
            String structuredRow = String.format(
                    Locale.US,
                    "%-3d\t%-16s\t%-12.1f\t%-12.2f",
                    index + 1,
                    timeline.getString(index),
                    tempRecords.getDouble(index),
                    rainRecords.getDouble(index)
            );
            textRows.add(structuredRow);
        }

        Path targetFilePath = Path.of(fileDestination);
        if (targetFilePath.getParent() != null) {
            Files.createDirectories(targetFilePath.getParent());
        }
        Files.write(targetFilePath, textRows, StandardCharsets.UTF_8);
        System.out.println("Задание 3. Прогноз сохранен в файл: " + fileDestination);
    }
}
