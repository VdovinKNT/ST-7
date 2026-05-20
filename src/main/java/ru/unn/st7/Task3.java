package ru.unn.st7;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Task3 {

    private static final String FORECAST_URL =
        "https://api.open-meteo.com/v1/forecast?latitude=56&longitude=44"
            + "&hourly=temperature_2m,rain&current=cloud_cover"
            + "&timezone=Europe%2FMoscow&forecast_days=1&wind_speed_unit=ms";

    public static void main(String[] args) {
        WebDriver webDriver = new ChromeDriver();
        try {
            String table = generateForecastTable(webDriver);
            System.out.println(table);
        } catch (Exception e) {
            System.out.println("Error");
            System.out.println(e);
        } finally {
            webDriver.quit();
        }
    }

    public static String generateForecastTable(WebDriver webDriver) {
        webDriver.get(FORECAST_URL);
        new WebDriverWait(webDriver, Duration.ofSeconds(10))
            .until(d -> !d.findElements(By.tagName("body")).isEmpty());

        String json = readJsonText(webDriver);
        String table = buildTableFromJson(json);
        writeForecastFile(table);
        return table;
    }

    private static String readJsonText(WebDriver webDriver) {
        WebElement pre = webDriver.findElements(By.tagName("pre")).stream().findFirst().orElse(null);
        if (pre != null) {
            return pre.getText();
        }

        String bodyText = webDriver.findElement(By.tagName("body")).getText();
        if (bodyText == null || bodyText.isBlank()) {
            throw new IllegalStateException("Сервис open-meteo вернул пустой ответ");
        }
        return bodyText;
    }

    private static String buildTableFromJson(String json) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(json);
            JSONObject hourly = (JSONObject) root.get("hourly");
            JSONArray time = (JSONArray) hourly.get("time");
            JSONArray temperature = (JSONArray) hourly.get("temperature_2m");
            JSONArray rain = (JSONArray) hourly.get("rain");

            int rows = Math.min(time.size(), Math.min(temperature.size(), rain.size()));

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("| %-3s | %-19s | %-11s | %-10s |%n", "N", "Дата/время", "Температура", "Осадки"));
            sb.append("|-----|---------------------|-------------|------------|\n");

            for (int i = 0; i < rows; i++) {
                String t = String.valueOf(time.get(i));
                String temp = String.valueOf(temperature.get(i));
                String r = String.valueOf(rain.get(i));
                sb.append(String.format("| %-3d | %-19s | %-11s | %-10s |%n", i + 1, t, temp, r));
            }

            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось сформировать таблицу прогноза из JSON", e);
        }
    }

    private static void writeForecastFile(String table) {
        try {
            Path path = Path.of("result", "forecast.txt");
            Files.createDirectories(path.getParent());
            Files.writeString(path, table, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось записать файл result/forecast.txt", e);
        }
    }
}
