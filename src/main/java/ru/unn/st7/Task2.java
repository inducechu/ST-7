package ru.unn.st7;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class Task2 {
    private Task2() {
    }

    public static void printClientIp(WebDriver webDriver) {
        webDriver.get("https://api.ipify.org/?format=json");
        WebElement pre = webDriver.findElement(By.tagName("pre"));
        JSONObject json = new JSONObject(pre.getText());
        String ip = json.optString("ip", "IP не найден");
        System.out.println("Задание 2. IPv4 клиента: " + ip);
    }
}
