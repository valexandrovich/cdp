package ua.com.valexa.etiharvester.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ua.com.valexa.etiharvester.service.EtiHarvesterService;

@RestController
@Slf4j
public class EtiHarvesterController {

    @Autowired
    EtiHarvesterService etiHarvesterService;

    @GetMapping("/harvest")
    public Mono<Void> startHarvesting(){
        log.info("Start harvesting");
        return etiHarvesterService.startHarvesting();
    }

}
