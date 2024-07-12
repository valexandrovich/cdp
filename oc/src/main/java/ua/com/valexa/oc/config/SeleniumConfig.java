package ua.com.valexa.oc.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumConfig {

    @Bean
    public WebDriver webDriver() {
//        System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver"); // Change the path to the location of your chromedriver

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode if needed
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1200");
        options.addArguments("--ignore-certificate-errors");

        return new ChromeDriver(options);
    }

}
