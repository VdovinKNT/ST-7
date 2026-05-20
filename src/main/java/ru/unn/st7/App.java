package ru.unn.st7;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class App {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("[A-Za-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\\\",.<>/?|`~]{8,}");

    public static void main(String[] args) {
        WebDriver webDriver = createDriver();
        try {
            String password = getGeneratedPassword(webDriver);
            System.out.println("Задание 1: сгенерированный пароль = " + password);

            String ip = Task2.fetchClientIp(webDriver);
            System.out.println("Задание 2: ваш IP = " + ip);

            Task3.generateForecastTable(webDriver);
            System.out.println("Задание 3: таблица прогноза сохранена в result/forecast.txt");
        } catch (Exception e) {
            System.out.println("Error");
            System.out.println(e);
        } finally {
            webDriver.quit();
        }
    }

    private static WebDriver createDriver() {
        String driverPath = System.getenv("CHROMEDRIVER_PATH");
        if (driverPath != null && !driverPath.isBlank()) {
            System.setProperty("webdriver.chrome.driver", driverPath);
        }
        return new ChromeDriver();
    }

    public static String getGeneratedPassword(WebDriver webDriver) {
        webDriver.get("https://www.calculator.net/password-generator.html");
        new WebDriverWait(webDriver, Duration.ofSeconds(10))
            .until(d -> !d.findElements(By.tagName("body")).isEmpty());

        List<By> buttonSelectors = Arrays.asList(
            By.cssSelector("button"),
            By.cssSelector("input[type='button']")
        );
        for (By selector : buttonSelectors) {
            for (WebElement button : webDriver.findElements(selector)) {
                String text = button.getText().trim();
                String value = button.getAttribute("value");
                if ("Regenerate".equalsIgnoreCase(text)
                        || "Generate".equalsIgnoreCase(text)
                        || "Regenerate".equalsIgnoreCase(value)
                        || "Generate".equalsIgnoreCase(value)) {
                    button.click();
                    break;
                }
            }
        }

        String password = readPasswordFromInputs(webDriver);
        if (password != null) {
            return password;
        }

        String text = webDriver.findElement(By.tagName("body")).getText();
        Matcher matcher = PASSWORD_PATTERN.matcher(text);
        while (matcher.find()) {
            String candidate = matcher.group();
            if (isLikelyPassword(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Не удалось найти сгенерированный пароль на странице");
    }

    private static String readPasswordFromInputs(WebDriver webDriver) {
        for (WebElement input : webDriver.findElements(By.cssSelector("input[type='text'], input[type='search']"))) {
            String value = input.getAttribute("value");
            if (value != null && isLikelyPassword(value.trim())) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean isLikelyPassword(String value) {
        if (value.length() < 8 || value.length() > 64 || value.contains(" ")) {
            return false;
        }
        boolean hasLetter = value.matches(".*[A-Za-z].*");
        boolean hasDigit = value.matches(".*\\d.*");
        return hasLetter && hasDigit;
    }
}
