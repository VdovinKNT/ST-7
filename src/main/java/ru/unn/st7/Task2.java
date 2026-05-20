package ru.unn.st7;

import java.time.Duration;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Task2 {

    public static void main(String[] args) {
        WebDriver webDriver = new ChromeDriver();
        try {
            String ip = fetchClientIp(webDriver);
            System.out.println(ip);
        } catch (Exception e) {
            System.out.println("Error");
            System.out.println(e);
        } finally {
            webDriver.quit();
        }
    }

    public static String fetchClientIp(WebDriver webDriver) {
        webDriver.get("https://api.ipify.org/?format=json");
        new WebDriverWait(webDriver, Duration.ofSeconds(10))
            .until(d -> !d.findElements(By.tagName("body")).isEmpty());

        String json = readJsonText(webDriver);

        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(json);
            Object ip = obj.get("ip");
            if (ip == null) {
                throw new IllegalStateException("Ключ ip не найден в ответе сервера");
            }
            return ip.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось распарсить JSON от ipify", e);
        }
    }

    private static String readJsonText(WebDriver webDriver) {
        WebElement pre = webDriver.findElements(By.tagName("pre")).stream().findFirst().orElse(null);
        if (pre != null) {
            return pre.getText();
        }

        WebElement body = webDriver.findElement(By.tagName("body"));
        String bodyText = body.getText();
        if (bodyText == null || bodyText.isBlank()) {
            throw new IllegalStateException("Получен пустой ответ от сервиса ipify");
        }
        return bodyText;
    }
}
