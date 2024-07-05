package ua.com.valexa.etiextractor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.valexa.cdpcommon.dto.EtiExtractRequest;
import ua.com.valexa.cdpcommon.dto.EtiProfileDto;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class QueueListener {

    @Autowired
    EtiExtractorService etiExtractorService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${queue.eti.saver}")
    private String queueEtiSaver;


    @RabbitListener(queues = "#{getQueueEtiExtractor}")
    public void receiveDownloaderMessage(EtiExtractRequest request) {
        log.info("EintaxidExtractor get request: " + request.getCompanyLink());
        CompletableFuture<EtiProfileDto> cfuture = etiExtractorService.extractFuture(request);
        cfuture.thenAccept(this::sendResponse);
    }


    private void sendResponse(EtiProfileDto etiProfileDto) {
        log.info("Sending to save EintaxidProfile: " + etiProfileDto);
        rabbitTemplate.convertAndSend(queueEtiSaver, etiProfileDto);


    }

}
