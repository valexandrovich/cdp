package ua.com.valexa.oc.service;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.com.valexa.cdpcommon.dto.OcRequest;
import ua.com.valexa.cdpcommon.enums.StateMapping;
import ua.com.valexa.oc.model.OcSimpleResponse;
import ua.com.valexa.oc.model.OcUserProfile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OCParserService {

    private WebClient client = new WebClient();

    {
        log.info("Configuring WebClient");
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setCssEnabled(false);

        client.setJavaScriptErrorListener(new JavaScriptErrorListener() {
            @Override
            public void scriptException(HtmlPage page, com.gargoylesoftware.htmlunit.ScriptException scriptException) {
                // Do nothing, suppress JavaScript errors
            }

            @Override
            public void timeoutError(HtmlPage page, long allowedTime, long executionTime) {
                // Do nothing, suppress JavaScript errors
            }

            @Override
            public void malformedScriptURL(HtmlPage page, String url, MalformedURLException malformedURLException) {
                // Do nothing, suppress JavaScript errors
            }

            @Override
            public void loadScriptError(HtmlPage page, URL scriptUrl, Exception exception) {
                // Do nothing, suppress JavaScript errors
            }

            @Override
            public void warn(String message, String sourceName, int line, String lineSource, int lineOffset) {
                // Do nothing
            }
        });

        client.setWebConnection(new WebConnectionWrapper(client) {
            @Override
            public WebResponse getResponse(WebRequest request) throws IOException {
                WebResponse response = super.getResponse(request);
                if (response.getStatusCode() == 403) {
                    String emptyContent = "403 Forbidden";
                    WebResponseData data = new WebResponseData(
                            emptyContent.getBytes(StandardCharsets.UTF_8),
                            403,
                            "403 Forbidden",
                            response.getResponseHeaders()
                    );
                    return new WebResponse(data, request, response.getLoadTime());
                }
                return response;
            }
        });
    }


    private static final Logger log = LoggerFactory.getLogger(OCParserService.class);

    @SneakyThrows
    public OcSimpleResponse getData(OcRequest ocRequest){

        OcSimpleResponse ocSimpleResponse = new OcSimpleResponse();
        ocSimpleResponse.setId(UUID.randomUUID());
        ocSimpleResponse.setIrsEin(ocRequest.getIrsEin());
        ocSimpleResponse.setCompanyName(ocRequest.getCompanyName());
        ocSimpleResponse.setState(ocRequest.getState());


        int currentTry = 1;
        while (currentTry <= ocRequest.getMaxRetries()) {

            OcUserProfile userProfile = OcAccountService.getRandomProfile();

            try {

                DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) client.getCredentialsProvider();
                credentialsProvider.addCredentials(userProfile.getProxyUser(), userProfile.getProxyPassword());

                ProxyConfig proxyConfig = new ProxyConfig(userProfile.getProxyHost(), userProfile.getProxyPort(), "http", false);
                client.getOptions().setProxyConfig(proxyConfig);

                String searchUrl = buildSearchUrl(ocRequest);
                ocSimpleResponse.setSearchUrl(searchUrl);

                HtmlPage page = client.getPage(searchUrl);

                if (isLoginNeeded(page)) {
                    login(userProfile);
                    page = client.getPage(searchUrl);
                }

                List<HtmlListItem> results = getSearchResults(page);


                OcSimpleRes res =  buildResponse(results);
                System.out.println(res);

                ocSimpleResponse.setActiveInSearchResult(res.isActiveInSearchResult);
                ocSimpleResponse.setSearchResultCount(res.searchResultCount);

                return ocSimpleResponse;



            } catch (Exception ex){
                log.error(ex.getMessage());

                if (ex.getMessage().contains("because \"companiesList\" is null")){
                    ocSimpleResponse.setSearchResultCount(0);
                    ocSimpleResponse.setError(ex.getMessage());
                    return ocSimpleResponse;
                }

                if (currentTry == ocRequest.getMaxRetries()){
                  ocSimpleResponse.setError(ex.getMessage());
                  return ocSimpleResponse;
                }

                currentTry++;
            }
        }


        return ocSimpleResponse;

    }

    private boolean isLoginNeeded(HtmlPage page) {
        log.debug("Checking is login needed");
        try {
            HtmlHeading2 paidHead = (HtmlHeading2) page.getByXPath("/html/body/div[2]/div[2]/div/h2").get(0);
            return paidHead.asNormalizedText().startsWith("Please sign in");
        } catch (Exception ex){
            return false;
        }


    }

    public void login(OcUserProfile ocUserProfile) throws IOException {
        log.debug("Signing in");
        String loginUrl = "https://opencorporates.com/users/sign_in";
        String username = ocUserProfile.getUserName();
        String password = ocUserProfile.getUserPassword();
        HtmlPage loginPage = client.getPage(loginUrl);
        HtmlEmailInput emailInput = (HtmlEmailInput) loginPage.getByXPath("/html/body/div[2]/div[3]/div[1]/form/div[2]/div/input").get(0);
        HtmlPasswordInput passwordInput = (HtmlPasswordInput) loginPage.getByXPath("/html/body/div[2]/div[3]/div[1]/form/div[3]/div/input").get(0);
        HtmlButton submitButton = (HtmlButton) loginPage.getByXPath("/html/body/div[2]/div[3]/div[1]/form/div[6]/div/button").get(0);
        emailInput.setValueAttribute(username);
        passwordInput.setValueAttribute(password);
        HtmlPage resultPage = submitButton.click();
        client.waitForBackgroundJavaScript(3000);
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
                "?q=" +
                ocRequest.getCompanyName().replace(" ", "+");
        log.info("Searching URL: {}", url);
        return url;
    }

    List<HtmlListItem> getSearchResults(HtmlPage page) {
        log.debug("Getting search results");
        HtmlUnorderedList companiesList = (HtmlUnorderedList) page.getElementById("companies");
        return companiesList.getByXPath(".//li");
    }


    OcSimpleRes buildResponse(List<HtmlListItem> results) {
        log.debug("Filtering search results");
        OcSimpleRes res = new OcSimpleRes();

        res.setActiveInSearchResult(false);
        for (HtmlListItem item : results) {
            if (!itemContainsInactiveSpan(item)) {
                res.setActiveInSearchResult(true);
                break;
            }
        }
        res.setSearchResultCount(results.size());
        return res;
    }

    private boolean itemContainsInactiveSpan(HtmlListItem item) {
        List<HtmlSpan> spans = item.getByXPath(".//span[text()='inactive']");
        return !spans.isEmpty();
    }


    @Getter
    @Setter
    class OcSimpleRes {
        private int searchResultCount;
        private boolean isActiveInSearchResult;
        private String error;

    }



}
