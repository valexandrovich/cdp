package ua.com.valexa.etisaver;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ua.com.valexa.cdpcommon.dto.EtiProfileDto;
import ua.com.valexa.etisaver.entity.EtiProfile;
import ua.com.valexa.etisaver.repository.EtiProfileRepository;

@SpringBootTest
class EtiSaverApplicationTests {


    @Autowired
    EtiProfileRepository etiProfileRepository;


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {

        PageRequest pr = PageRequest.of(0, 100);
        Page<EtiProfile> page = etiProfileRepository.findAll(pr);

        for (EtiProfile profile : page.getContent()){



            rabbitTemplate.convertAndSend("cdp.oc.request", toDto(profile));


        }

        System.out.println(page);

    }




    private static EtiProfileDto toDto(EtiProfile entity) {
        if (entity == null) {
            return null;
        }
        EtiProfileDto dto = new EtiProfileDto();
        dto.setOrgName(entity.getOrgName());
        dto.setIrsEin(entity.getIrsEin());
        dto.setDoingBusinessAs(entity.getDoingBusinessAs());
        dto.setTypeOfBusiness(entity.getTypeOfBusiness());
        dto.setDescription(entity.getDescription());
        dto.setBusinessProfile(entity.getBusinessProfile());
        dto.setBusinessAddress(entity.getBusinessAddress());
        dto.setBusinessAddressLine2(entity.getBusinessAddressLine2());
        dto.setBusinessCity(entity.getBusinessCity());
        dto.setBusinessState(entity.getBusinessState());
        dto.setBusinessZip(entity.getBusinessZip());
        dto.setMailingAddress(entity.getMailingAddress());
        dto.setMailingAddress2(entity.getMailingAddress2());
        dto.setMailingCity(entity.getMailingCity());
        dto.setMailingState(entity.getMailingState());
        dto.setMailingZIP(entity.getMailingZIP());
        dto.setCik(entity.getCik());
        dto.setEndOfFiscalYear(entity.getEndOfFiscalYear());
        dto.setIncState(entity.getIncState());
        dto.setIncSubDiv(entity.getIncSubDiv());
        dto.setIncCountry(entity.getIncCountry());
        dto.setFillingYear(entity.getFillingYear());
        dto.setLink(entity.getLink());
        return dto;
    }

}
