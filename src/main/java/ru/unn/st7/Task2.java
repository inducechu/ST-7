package ru.unn.st7;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class Task2 {
    private Task2() {
    }

    public static void printClientIp(WebDriver driverInstance) {
        driverInstance.get("https://api.ipify.org/?format=json");
        WebElement rawContainer = driverInstance.findElement(By.tagName("pre"));
        JSONObject parsedJson = new JSONObject(rawContainer.getText());
        String clientIpAddress = parsedJson.optString("ip", "IP не найден");
        System.out.println("Задание 2. IPv4 клиента: " + clientIpAddress);
    }
}
