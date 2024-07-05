package ua.com.valexa.etisaver.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.cdpcommon.dto.EtiProfileDto;
import ua.com.valexa.etisaver.entity.EtiProfile;
import ua.com.valexa.etisaver.repository.EtiProfileRepository;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class EtiSaverService {

    @Autowired
    EtiProfileRepository etiProfileRepository;


    ExecutorService executorService = Executors.newFixedThreadPool(20);

    @SneakyThrows
    public CompletableFuture<Void> save(EtiProfileDto dto) {
        return CompletableFuture.runAsync(() -> saveDto(dto), executorService);
    }

    @SneakyThrows
    private void saveDto(EtiProfileDto dto){
        EtiProfile entity = toEntity(dto);
        etiProfileRepository.save(entity);
    }

    private static EtiProfile toEntity(EtiProfileDto dto) {
        if (dto == null) {
            return null;
        }
        EtiProfile entity = new EtiProfile();
        entity.setOrgName(dto.getOrgName());
        entity.setIrsEin(dto.getIrsEin());
        entity.setDoingBusinessAs(dto.getDoingBusinessAs());
        entity.setTypeOfBusiness(dto.getTypeOfBusiness());
        entity.setDescription(dto.getDescription());
        entity.setBusinessProfile(dto.getBusinessProfile());
        entity.setBusinessAddress(dto.getBusinessAddress());
        entity.setBusinessAddressLine2(dto.getBusinessAddressLine2());
        entity.setBusinessCity(dto.getBusinessCity());
        entity.setBusinessState(dto.getBusinessState());
        entity.setBusinessZip(dto.getBusinessZip());
        entity.setMailingAddress(dto.getMailingAddress());
        entity.setMailingAddress2(dto.getMailingAddress2());
        entity.setMailingCity(dto.getMailingCity());
        entity.setMailingState(dto.getMailingState());
        entity.setMailingZIP(dto.getMailingZIP());
        entity.setCik(dto.getCik());
        entity.setEndOfFiscalYear(dto.getEndOfFiscalYear());
        entity.setIncState(dto.getIncState());
        entity.setIncSubDiv(dto.getIncSubDiv());
        entity.setIncCountry(dto.getIncCountry());
        entity.setFillingYear(dto.getFillingYear());
        entity.setLink(dto.getLink());
        return entity;
    }


}
