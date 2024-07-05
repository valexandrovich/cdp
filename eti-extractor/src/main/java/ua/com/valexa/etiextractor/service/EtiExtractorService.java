package ua.com.valexa.etiextractor.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import ua.com.valexa.cdpcommon.dto.EtiExtractRequest;
import ua.com.valexa.cdpcommon.dto.EtiProfileDto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class EtiExtractorService {

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @SneakyThrows
    public CompletableFuture<EtiProfileDto> extractFuture(EtiExtractRequest request) {
        return CompletableFuture.supplyAsync(() -> extract(request), executorService);
    }


    @SneakyThrows
    private EtiProfileDto extract(EtiExtractRequest request){
        return getProfileByUrl(request.getCompanyLink());
    }


    private HtmlPage loginToWebsite(WebClient webClient) throws Exception {
        String loginUrl = "https://eintaxid.com/customers/login.php";
        HtmlPage loginPage = webClient.getPage(loginUrl);


        HtmlEmailInput emailInput = (HtmlEmailInput) loginPage.getByXPath("/html/body/div[2]/div/div/div/div/form/div[1]/input").get(0);
        HtmlPasswordInput passwordInput = (HtmlPasswordInput) loginPage.getByXPath("/html/body/div[2]/div/div/div/div/form/div[2]/input[1]").get(0);
        HtmlButton submitButton = (HtmlButton) loginPage.getByXPath("/html/body/div[2]/div/div/div/div/form/button").get(0);
        // Replace the below with actual form field names and login URL
//        HtmlForm form = loginPage.getFormByName("loginForm");
//        HtmlTextInput usernameField = form.getInputByName("email");
//        HtmlPasswordInput passwordField = form.getInputByName("password");
//        HtmlSubmitInput submitButton = form.getInputByName("submit");

        // Set credentials
//        usernameField.setValueAttribute("your-username");
//        passwordField.setValueAttribute("your-password");

        emailInput.setValueAttribute("sotnik5alex@gmail.com");
        passwordInput.setValueAttribute("Free1312!");


        // Submit the form
        HtmlPage homePage = submitButton.click();
        return homePage;
    }

    @SneakyThrows
    public EtiProfileDto getProfileByUrl(String url) {

        try {
            EtiProfileDto profile = new EtiProfileDto();

            WebClient webClient = new WebClient();
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);

            HtmlPage page = webClient.getPage(url);


            // check for paid plan needed

            if (isLoginNeeded(page)){
                System.out.println("NEED LOGIN!!");
                page = loginToWebsite(webClient);
            }

            page = webClient.getPage(url);
            profile.setDescription(getDescription(page));
            List<HtmlTableRow> rows = extractRows(page);

            for (HtmlTableRow row : rows) {

                HtmlTableCell thCell = row.getCell(0);
                HtmlTableCell tdCell = row.getCell(1);

                if (thCell != null && tdCell != null) {
                    String thValue = thCell.asNormalizedText().trim();
                    String tdValue = tdCell.asNormalizedText().trim();

                    if (thValue.equals("Organization Name")) {
                        profile.setOrgName(tdValue);
                    }

                    if (thValue.equals("IRS EIN (Taxpayer Id)") || thValue.equals("EIN (Taxpayer Id)")) {
                        profile.setIrsEin(tdValue);
                    }

                    if (thValue.equals("Doing Business As")) {
                        profile.setDoingBusinessAs(tdValue);
                    }

                    if (thValue.equals("Type of business")) {
                        profile.setTypeOfBusiness(tdValue);
                    }

                    if (thValue.equals("Business Phone") || thValue.equals("Phone Number")) {
                        profile.setBusinessProfile(tdValue);
                    }

                    if (thValue.equals("Business Address") || thValue.equals("Address1")) {
                        profile.setBusinessAddress(tdValue);
                    }

                    if (thValue.equals("Business Address Line 2") || thValue.equals("Address2")) {
                        profile.setBusinessAddressLine2(tdValue);
                    }

                    if (thValue.equals("Business City") || thValue.equals("City")) {
                        profile.setBusinessCity(tdValue);
                    }

                    if (thValue.equals("Business State") || thValue.equals("State")) {
                        profile.setBusinessState(tdValue);
                    }

                    if (thValue.equals("Business ZIP") || thValue.equals("Zip")) {
                        profile.setBusinessZip(tdValue);
                    }

                    if (thValue.equals("Mailing Address")) {
                        profile.setMailingAddress(tdValue);
                    }
                    if (thValue.equals("Mailing Address2")) {
                        profile.setMailingAddress2(tdValue);
                    }

                    if (thValue.equals("Mailing City")) {
                        profile.setMailingCity(tdValue);
                    }

                    if (thValue.equals("Mailing State")) {
                        profile.setMailingState(tdValue);
                    }

                    if (thValue.equals("Mailing ZIP")) {
                        profile.setMailingZIP(tdValue);
                    }

                    if (thValue.equals("Central Index Key (CIK)")) {
                        profile.setCik(tdValue);
                    }

                    if (thValue.equals("End of fiscal year")) {
                        profile.setEndOfFiscalYear(tdValue);
                    }

                    if (thValue.equals("Incorporation State")) {
                        profile.setIncState(tdValue);
                    }

                    if (thValue.equals("Incorporation Sub division")) {
                        profile.setIncSubDiv(tdValue);
                    }

                    if (thValue.equals("Incorporation Country")) {
                        profile.setIncCountry(tdValue);
                    }

                    if (thValue.equals("Filing Year")) {
                        profile.setFillingYear(tdValue);
                    }

                    profile.setLink(url);

                }

            }

            webClient.close();

            return profile;
        } catch (Exception ex){
            log.error(ex.getMessage());
            return null;
        }

    }


    private String getDescription(HtmlPage page) {
        String desc1 = ((DomText) page.getByXPath("/html/body/div[2]/div/div[1]/div[1]/div[2]/text()").get(1)).asNormalizedText();
        ;
        String desc2 = ((DomText) page.getByXPath("/html/body/div[2]/div/div[1]/div[1]/div[2]/text()").get(6)).asNormalizedText();
        return desc1 + "; " + desc2;
    }


    private List<HtmlTableRow> extractRows(HtmlPage page) {
        List<HtmlTableRow> rows = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            List<Elements> elements = page.getByXPath("/html/body/div[2]/div/div[1]/div[1]/div[2]/table["+i+"]");
            if (elements.size() == 1){
                HtmlTable t1 = ((HtmlTable) page.getByXPath("/html/body/div[2]/div/div[1]/div[1]/div[2]/table["+i+"]").get(0));
                rows.addAll(t1.getRows());
            }
        }

//        HtmlTable t1 = ((HtmlTable) page.getByXPath("/html/body/div[2]/div/div[1]/div[1]/div[2]/table[1]").get(0));
//        rows.addAll(t1.getRows());
//
//        HtmlTable t2 = ((HtmlTable) page.getByXPath("/html/body/div[2]/div/div[1]/div[1]/div[2]/table[2]").get(0));
//        rows.addAll(t2.getRows());
//
//        HtmlTable t3 = ((HtmlTable) page.getByXPath("/html/body/div[2]/div/div[1]/div[1]/div[2]/table[3]").get(0));
//        rows.addAll(t3.getRows());
//
//        HtmlTable t4 = ((HtmlTable) page.getByXPath("/html/body/div[2]/div/div[1]/div[1]/div[2]/table[4]").get(0));
//        rows.addAll(t4.getRows());

        return rows;
    }

    private boolean isLoginNeeded(HtmlPage page){
        try {
            HtmlHeading3 paidHead =  (HtmlHeading3) page.getByXPath("/html/body/div[2]/h3[1]").get(0);
            if(paidHead.asNormalizedText().startsWith("Paid only access")){
                return true;
            }
        } catch (Exception ex){
            log.error(ex.getMessage());
        }
        return false;
    }

}
