package ua.com.valexa.oc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import ua.com.valexa.cdpcommon.dto.EtiExtractRequest;
import ua.com.valexa.cdpcommon.dto.EtiProfileDto;
import ua.com.valexa.cdpcommon.dto.OcRequest;
import ua.com.valexa.oc.model.OcCompanyProfile;
import ua.com.valexa.oc.repository.OcCompanyProfileRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class QueueListener {

    private static final Logger log = LoggerFactory.getLogger(QueueListener.class);

    @Autowired
    RabbitTemplate rabbitTemplate;
//
//    @Value("${queue.oc.request}")
//    private String queueOcRequest;

    @Autowired
    OcService ocService;

    @Autowired
    OcCompanyProfileRepository ocCompanyProfileRepository;

    ExecutorService taskExecutor = Executors.newFixedThreadPool(10);


    @RabbitListener(queues = "#{getQueueOcRequest}")
    public void receiveDownloaderMessage(EtiProfileDto dto) {



//        taskExecutor.submit(() -> {
//            OcRequest request = new OcRequest();
//            request.setCompanyName(dto.getOrgName());
//            request.setState(dto.getIncState());
//            request.setMaxRetries(3);
//
//            List<OcCompanyProfile> res = ocService.search(request);
//
//            if (res.size() > 0) {
//                System.out.println(res);
//                ocCompanyProfileRepository.saveAll(res);
//            }
//
//            System.out.println(dto);
//        });

        OcRequest request = new OcRequest();
        request.setCompanyName(dto.getOrgName());
        request.setState(dto.getIncState());
        request.setMaxRetries(3);
        List<OcCompanyProfile> res =  ocService.search(request);

        if (res.size() > 0){
            System.out.println(res);
            ocCompanyProfileRepository.saveAll(res);
        }
        System.out.println(dto);

    }

}
