package ua.com.valexa.etisaver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.valexa.cdpcommon.dto.EtiProfileDto;
import ua.com.valexa.etisaver.entity.EtiProfile;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class QueueListener {

    @Autowired
    EtiSaverService etiSaverService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${queue.oc.request}")
    String queueOcRequest;


    @RabbitListener(queues = "#{getQueueEtiSaver}")
    public void receiveDownloaderMessage(EtiProfileDto dto) {
        log.info("EintaxidSaver get request: " + dto);

        CompletableFuture<Void> cfuture = etiSaverService.save(dto);
        cfuture.thenRunAsync(() ->{
            sendOcRequest(dto);
        });

    }


    private void sendOcRequest(EtiProfileDto dto){
        rabbitTemplate.convertAndSend(queueOcRequest, dto);
    }

}
