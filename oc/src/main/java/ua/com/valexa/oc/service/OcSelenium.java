package ua.com.valexa.oc.service;

import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.cdpcommon.dto.OcRequest;
import ua.com.valexa.cdpcommon.enums.StateMapping;
import ua.com.valexa.oc.model.OcCompanyProfile;
import ua.com.valexa.oc.model.OcUserProfile;
import org.openqa.selenium.Proxy;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OcSelenium {

    private static final Logger log = LoggerFactory.getLogger(OcSelenium.class);


//    @Autowired
    private WebDriver webDriver;
//



    public List<OcCompanyProfile> search(OcRequest ocRequest) {






        log.info("Got request: {}", ocRequest);
        List<OcCompanyProfile> profiles = new ArrayList<>();

        OcCompanyProfile failedProfile = new OcCompanyProfile();

        int currentTry = 1;
        while (currentTry <= ocRequest.getMaxRetries()) {
            try {
                OcUserProfile userProfile = OcAccountService.getRandomProfile();


                WebDriverManager.chromedriver().setup();


                Proxy proxy = new Proxy();
                proxy.setHttpProxy(userProfile.getProxyHost() + ":" + userProfile.getProxyPort());
                proxy.setSslProxy(userProfile.getProxyHost() + ":" + userProfile.getProxyPort());


//                proxy.setSslProxy("your-proxy-server:port");

                // Set up ChromeOptions
                ChromeOptions options = new ChromeOptions();
                options.setProxy(proxy);
//
//                options.addArguments("--headless"); // Run in headless mode if needed
//                options.addArguments("--disable-gpu");
//                options.addArguments("--window-size=1920,1200");
//                options.addArguments("--ignore-certificate-errors");
                webDriver = new ChromeDriver(options);


//                DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) client.getCredentialsProvider();
//                credentialsProvider.addCredentials(userProfile.getProxyUser(), userProfile.getProxyPassword());

//                ProxyConfig proxyConfig = new ProxyConfig(userProfile.getProxyHost(), userProfile.getProxyPort(), "http", false);
//                client.getOptions().setProxyConfig(proxyConfig);

                String searchUrl = buildSearchUrl(ocRequest);
                failedProfile.setId(UUID.randomUUID());
                failedProfile.setSearchUrl(searchUrl);
                failedProfile.setIrsEin(ocRequest.getIrsEin());

                failedProfile.setOcAcc(userProfile.getUserName());
                failedProfile.setProxyHost(userProfile.getProxyHost());
                failedProfile.setProxyPort(userProfile.getProxyPort());


//                HtmlPage page = client.getPage(searchUrl);
                webDriver.get(searchUrl);


                if (isLoginNeeded()) {
                    login(userProfile);
                    webDriver.get(searchUrl);
                }

//                List<HtmlListItem> results = getSearchResults(page);
//                List<HtmlListItem> filteredResults = fiterSearchResults(results);
//
//
//                List<HtmlPage> companyPages = new ArrayList<>();
//                for (HtmlListItem item : filteredResults) {
//                    companyPages.add(getCompanyPage(item));
//                }
//
//                profiles = new ArrayList<>();
//                for (HtmlPage companyPage : companyPages) {
//                    profiles.add(extractData(companyPage));
//                }

                int ps = profiles.size();
                System.out.println("RES: " + profiles.size());
                currentTry = ocRequest.getMaxRetries() + 1;
                profiles.stream().forEach(p -> p.setSearchUrl(searchUrl));
                profiles.stream().forEach(p -> p.setCountResults(ps));

                profiles.stream().forEach(p -> p.setOcAcc(userProfile.getUserName()));
                profiles.stream().forEach(p -> p.setProxyHost(userProfile.getProxyHost()));
                profiles.stream().forEach(p -> p.setProxyPort(userProfile.getProxyPort()));

                return profiles;

            } catch (Exception e) {
                failedProfile.setError(e.getMessage());
                failedProfile.setCountResults(0);
                profiles.add(failedProfile);
                currentTry++;
                log.error(e.getMessage());
            }
        }
        return profiles;




        /////////////////


//        log.info("Got request: {}", ocRequest);
//        List<OcCompanyProfile> profiles = new ArrayList<>();
//
//        OcUserProfile userProfile = OcAccountService.getRandomProfile();
//
//        String searchUrl = buildSearchUrl(ocRequest);
//
//        webDriver.get(searchUrl);
//
//
//        WebElement heading = webDriver.findElement(By.xpath("//*[self::h2][contains(text(),'Please sign in or register to continue')]"));
//
//        if (isLoginNeeded()){
//            login(userProfile);
//            webDriver.get(searchUrl);
//        }
//
//
//        String title = webDriver.getTitle();
//        System.out.println(title);
//
//
//
//
//        return profiles;
    }


    private boolean isLoginNeeded(){
        try {
            WebElement heading = webDriver.findElement(By.xpath("//*[self::h2][contains(text(),'Please sign in or register to continue')]"));
            log.debug("Login needed: {}", heading != null);
            return heading != null;
        } catch (Exception ex){
            return true;
        }

    }

    private void login(OcUserProfile ocUserProfile){
        log.debug("Signing in");
        String loginUrl = "https://opencorporates.com/users/sign_in";
        String username = ocUserProfile.getUserName();
        String password = ocUserProfile.getUserPassword();
        webDriver.get(loginUrl);

        // Locate the email input field and enter the username
        webDriver.get(loginUrl);

        // Set up WebDriverWait
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.of(20, ChronoUnit.SECONDS));

        // Locate the email input field and enter the username
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[3]/div[1]/form/div[2]/div/input")));
        emailInput.sendKeys(username);

        // Locate the password input field and enter the password
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[3]/div[1]/form/div[3]/div/input")));
        passwordInput.sendKeys(password);

        // Locate the submit button and click it
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div[3]/div[1]/form/div[6]/div/button")));
        submitButton.click();


        // Wait for the next page to load or for a specific element to be visible after login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Signed in successfully.')]"))); // Example condition, adjust as necessary
    }



    private String buildSearchUrl(OcRequest ocRequest) {
        String jurisdiction = "country/us";
        if (ocRequest.getState() != null){
            StateMapping stateShort = StateMapping.getByState(ocRequest.getState().toUpperCase());
            if (stateShort != null) {
                jurisdiction = "us_" + stateShort.toString().toLowerCase();
            }
        }


        String url = "https://opencorporates.com/companies/" +
                jurisdiction +
                "?utf8=%E2%9C%93&q=" +
                ocRequest.getCompanyName().replace(" ", "+") +
//                "&commit=Go&jurisdiction_code=&utf8=%E2%9C%93&commit=Go&controller=searches&action=search_companies&mode=best_fields&search_fields%5B%5D=name&search_fields%5B%5D=previous_names&search_fields%5B%5D=company_number&search_fields%5B%5D=other_company_numbers&branch=false&inactive=false&nonprofit=&order=";
                "&commit=Go&jurisdiction_code=&utf8=%E2%9C%93&commit=Go&controller=searches&action=search_companies&mode=best_fields&search_fields%5B%5D=name&search_fields%5B%5D=previous_names&search_fields%5B%5D=company_number&search_fields%5B%5D=other_company_numbers&branch=&inactive=&nonprofit=&order=";
        log.info("Searching URL: {}", url);
        return url;
    }

//    public String parseWebsite(String url) {
//        webDriver.get(url);
//
//        // Example: Get the title of the page
//        String title = webDriver.getTitle();
//
//        // Example: Get a specific element by its ID
//        WebElement element = webDriver.findElement(By.id("elementId"));
//        String elementText = element.getText();
//
//        // Perform other parsing actions as needed
//
//        // Return the extracted data
//        return "Title: " + title + ", Element Text: " + elementText;
//    }

}
