package ua.com.valexa.oc.service;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.com.valexa.cdpcommon.dto.OcRequest;
import ua.com.valexa.cdpcommon.enums.StateMapping;
import ua.com.valexa.oc.model.OcCompanyProfile;
import ua.com.valexa.oc.model.OcUserProfile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OcService {

    private static final Logger log = LoggerFactory.getLogger(OcService.class);


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

    public List<OcCompanyProfile> search(OcRequest ocRequest) {
        log.info("Got request: {}", ocRequest);
        List<OcCompanyProfile> profiles = new ArrayList<>();

        OcCompanyProfile failedProfile = new OcCompanyProfile();

        int currentTry = 1;
        while (currentTry <= ocRequest.getMaxRetries()) {
            try {
                OcUserProfile userProfile = OcAccountService.getRandomProfile();

                DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) client.getCredentialsProvider();
                credentialsProvider.addCredentials(userProfile.getProxyUser(), userProfile.getProxyPassword());

                ProxyConfig proxyConfig = new ProxyConfig(userProfile.getProxyHost(), userProfile.getProxyPort(), "http", false);
                client.getOptions().setProxyConfig(proxyConfig);

                String searchUrl = buildSearchUrl(ocRequest);
                failedProfile.setId(UUID.randomUUID());
                failedProfile.setSearchUrl(searchUrl);
                failedProfile.setIrsEin(ocRequest.getIrsEin());

                failedProfile.setOcAcc(userProfile.getUserName());
                failedProfile.setProxyHost(userProfile.getProxyHost());
                failedProfile.setProxyPort(userProfile.getProxyPort());


                HtmlPage page = client.getPage(searchUrl);

                if (isLoginNeeded(page)) {
                    login(userProfile);
                    page = client.getPage(searchUrl);
                }

                List<HtmlListItem> results = getSearchResults(page);
                List<HtmlListItem> filteredResults = fiterSearchResults(results);


                List<HtmlPage> companyPages = new ArrayList<>();
                for (HtmlListItem item : filteredResults) {
                    companyPages.add(getCompanyPage(item));
                }

                profiles = new ArrayList<>();
                for (HtmlPage companyPage : companyPages) {
                    profiles.add(extractData(companyPage));
                }

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
    }




    private boolean isLoginNeeded(HtmlPage page) {
        log.debug("Checking is login needed");
        HtmlHeading2 paidHead = (HtmlHeading2) page.getByXPath("/html/body/div[2]/div[2]/div/h2").get(0);
        return paidHead.asNormalizedText().startsWith("Please sign in");
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
                "?utf8=%E2%9C%93&q=" +
                ocRequest.getCompanyName().replace(" ", "+") +
//                "&commit=Go&jurisdiction_code=&utf8=%E2%9C%93&commit=Go&controller=searches&action=search_companies&mode=best_fields&search_fields%5B%5D=name&search_fields%5B%5D=previous_names&search_fields%5B%5D=company_number&search_fields%5B%5D=other_company_numbers&branch=false&inactive=false&nonprofit=&order=";
                "&commit=Go&jurisdiction_code=&utf8=%E2%9C%93&commit=Go&controller=searches&action=search_companies&mode=best_fields&search_fields%5B%5D=name&search_fields%5B%5D=previous_names&search_fields%5B%5D=company_number&search_fields%5B%5D=other_company_numbers&branch=&inactive=&nonprofit=&order=";
        log.info("Searching URL: {}", url);
        return url;
    }

    List<HtmlListItem> getSearchResults(HtmlPage page) {
        log.debug("Getting search results");
        HtmlUnorderedList companiesList = (HtmlUnorderedList) page.getByXPath("/html/body/div[2]/div[2]/div[1]/div[2]/ul").get(0);
        return companiesList.getByXPath(".//li");
    }

    List<HtmlListItem> fiterSearchResults(List<HtmlListItem> results) {
        log.debug("Filtering search results");
        return results.stream()
                .filter(item -> !isControllingCompany(item))
                .collect(Collectors.toList());
    }

    private static boolean isControllingCompany(HtmlListItem item) {
        log.debug("Cheking is controlling company");
        List<HtmlElement> icons = item.getByXPath(".//i[@class='icon-fullscreen' and @rel='tooltip' and @data-original-title='this is a controlling company']");
        return !icons.isEmpty();
    }

    private HtmlPage getCompanyPage(HtmlListItem item) throws IOException {
        log.debug("Getting url for company page");
//        HtmlAnchor anchor = item.getFirstByXPath(".//a[@class='company_search_result']");
        HtmlAnchor anchor = (HtmlAnchor) item.getByXPath("/html/body/div[2]/div[2]/div[1]/div[2]/ul/li/a[2]").get(0);
        String url = "https://opencorporates.com" + anchor.getHrefAttribute();
        return client.getPage(url);
    }

    private OcCompanyProfile extractData(HtmlPage page) {
        log.debug("Extracting data from company page");
        HtmlDivision attributes = page.getFirstByXPath("/html/body/div[2]/div[2]/div[1]/div[1]/div");
        OcCompanyProfile companyProfile = new OcCompanyProfile();
        companyProfile.setId(UUID.randomUUID());

        List<?> dts = attributes.getByXPath(".//dl[@class='attributes dl-horizontal']/dt");
        List<?> dds = attributes.getByXPath(".//dl[@class='attributes dl-horizontal']/dd");

        for (int i = 0; i < dts.size(); i++) {
            String dtText = ((HtmlDefinitionTerm) dts.get(i)).asNormalizedText().trim();
            HtmlDefinitionDescription dd = (HtmlDefinitionDescription) dds.get(i);

            switch (dtText) {
                case "Company Number":
                    companyProfile.setCompanyNumber(dd.asNormalizedText().trim());
                    break;
                case "Incorporation Date":
                    HtmlSpan span = dd.getFirstByXPath(".//span[@itemprop='foundingDate']");
                    if (span != null) {
                        companyProfile.setIncorparatedDate(span.asNormalizedText().trim());
                    }
                    break;
                case "Agent Address":
                    companyProfile.setAgentAddress(dd.asNormalizedText().trim());
                    break;
                case "Company Type":
                    companyProfile.setCompanyType(dd.asNormalizedText().trim());
                    break;
                case "Jurisdiction":
                    HtmlAnchor jurisdictionAnchor = dd.getFirstByXPath(".//a");
                    if (jurisdictionAnchor != null) {
                        companyProfile.setJurisdiction(jurisdictionAnchor.asNormalizedText().trim());
                    }
                    break;
                case "Agent Name":
                    companyProfile.setAgentName(dd.asNormalizedText().trim());
                    break;
                case "Directors / Officers":
                    companyProfile.setDirectors(dd.asNormalizedText().trim());
                    break;
                case "Registry Page":
                    HtmlAnchor registryAnchor = dd.getFirstByXPath(".//a");
                    if (registryAnchor != null) {
                        companyProfile.setRegistryPage(registryAnchor.getHrefAttribute());
                    }
                    break;
            }
        }

        return companyProfile;
    }

    private static String getTextFromDd(HtmlDivision div, String className) {
        log.debug("Getting text from attributes div: {}", className);
        HtmlDivision dd = div.getFirstByXPath(".//dd[@class='" + className + "']");
        return dd != null ? dd.asNormalizedText().trim() : null;
    }

    private static String getTextFromSpan(HtmlDivision div, String ddClassName, String spanItemprop) {
        log.debug("Getting text from attributes div: {}, {}", ddClassName, spanItemprop);
        HtmlSpan span = div.getFirstByXPath(".//dd[@class='" + ddClassName + "']//span[@itemprop='" + spanItemprop + "']");
        return span != null ? span.asNormalizedText().trim() : null;
    }

    private static String getHrefFromAnchor(HtmlDivision div, String ddClassName, String anchorClassName) {
        log.debug("Getting text from attributes div: {}, {}", ddClassName, anchorClassName);
        HtmlAnchor anchor = div.getFirstByXPath(".//dd[@class='" + ddClassName + "']//a[@class='" + anchorClassName + "']");
        return anchor != null ? anchor.getHrefAttribute() : null;
    }

}
