package ua.com.valexa.oc;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.com.valexa.cdpcommon.dto.OcRequest;
import ua.com.valexa.oc.service.OcSelenium;
import ua.com.valexa.oc.service.OcService;

@SpringBootTest
class OcApplicationTests {

    @Autowired
    OcSelenium selenium;

    @Autowired
    OcService ocService;


    @Test
    void contextLoads() {
        OcRequest ocRequest = new OcRequest();
        ocRequest.setState("Delaware");
        ocRequest.setCompanyName("3M Co");
        ocRequest.setMaxRetries(1);

//        selenium.search(ocRequest);
        ocService.search(ocRequest);


    }

}
