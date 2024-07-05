package ua.com.valexa.oc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ua.com.valexa.cdpcommon.dto.OcRequest;
import ua.com.valexa.oc.model.OcCompanyProfile;
import ua.com.valexa.oc.service.OcService;

import java.util.List;

@RestController
public class OcController {

    @Autowired
    OcService ocService;

    @PostMapping("/search")
    public List<OcCompanyProfile> search(
            @RequestBody OcRequest ocRequest
    ) {
           return ocService.search(ocRequest);
    }

}
