package ua.com.valexa.oc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.com.valexa.cdpcommon.dto.OcRequest;
import ua.com.valexa.oc.model.OcCompanyProfile;
import ua.com.valexa.oc.model.OcUserProfile;
import ua.com.valexa.oc.repository.OcCompanyProfileRepository;
import ua.com.valexa.oc.service.OcService;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class OcApplicationTests {



    @Autowired
    OcService ocService;

    @Autowired
    OcCompanyProfileRepository ocCompanyProfileRepository;


    @Test
    void contextLoads() {

        OcRequest o1 = new OcRequest();
        o1.setCompanyName("3M Co");
        o1.setState("Delaware");
        o1.setMaxRetries(1);

        OcUserProfile u1 = new OcUserProfile();
        u1.setProxyHost("");
        u1.setProxyPort(0);
        u1.setProxyPassword("");
        u1.setProxyUser("");
        u1.setUserName("");
        u1.setUserPassword("");


        OcUserProfile u2 = new OcUserProfile();
        u2.setProxyHost("rotating.proxyempire.io");
        u2.setProxyPort(9033);
        u2.setProxyUser("td77UH76kCIdISfy");
        u2.setProxyPassword("wifi;us;;;");
        u2.setUserName("missbella143@hotmail.com");
        u2.setUserPassword("!fgdA3rtgHS");




        for (int i = 0; i < 100; i++) {
            List<OcCompanyProfile> profiles = ocService.search(o1, u2);
            System.out.println("Profiles: " + profiles.size());
            ocCompanyProfileRepository.saveAll(profiles);
        }



    }

    List<OcRequest> getRequstList(){
        List<OcRequest > result = new ArrayList<>();

        OcRequest o1 = new OcRequest();
        o1.setCompanyName("3M Co");
        o1.setState("Delaware");
        o1.setMaxRetries(1);
        result.add(o1);


        return result;



    }

}
