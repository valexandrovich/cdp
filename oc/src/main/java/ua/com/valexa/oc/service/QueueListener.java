package ua.com.valexa.oc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.cdpcommon.dto.EtiProfileDto;
import ua.com.valexa.cdpcommon.dto.OcRequest;
import ua.com.valexa.oc.model.OcCompanyProfile;
import ua.com.valexa.oc.repository.OcCompanyProfileRepository;

import java.util.List;
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


//    @Bean
//    public TaskExecutor taskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(10);
//        executor.setMaxPoolSize(20);
//        executor.setQueueCapacity(100);
//        executor.setThreadNamePrefix("OcServiceExecutor-");
//        executor.initialize();
//        return executor;
//    }


    private static final ThreadLocal<OcService> threadLocalOcService = ThreadLocal.withInitial(() -> new OcService());
    private String getFirstNonEmptyState(EtiProfileDto dto) {
        if (dto.getIncState() != null && !dto.getIncState().isEmpty()) {
            return dto.getIncState();
        }
        if (dto.getBusinessState() != null && !dto.getBusinessState().isEmpty()) {
            return dto.getBusinessState();
        }
        if (dto.getMailingState() != null && !dto.getMailingState().isEmpty()) {
            return dto.getMailingState();
        }
        return null; // or a default value if none of the states are non-null and non-empty
    }



    @RabbitListener(queues = "#{getQueueOcRequest}")
    public void receiveDownloaderMessage(EtiProfileDto dto) {
        OcRequest request = new OcRequest();
        request.setCompanyName(dto.getOrgName());

        request.setState(getFirstNonEmptyState(dto));
//        request.setState(dto.getBusinessState());
//        request.setState(dto.getMailingState());



        request.setMaxRetries(1);

//        OcService ocService1 = new OcService();
//        OcSelenium selenium = new OcSelenium();
        List<OcCompanyProfile> profiles =  ocService.search(request);
        saveProfiles(profiles);

//        CompletableFuture<List<OcCompanyProfile>> cfuture = CompletableFuture.supplyAsync(() -> {
//            OcService ocService = threadLocalOcService.get();
////            return ocService.search(request);
//            return ocSelenium.search(request);
//        }, taskExecutor);
//
//        cfuture.thenAcceptAsync(ocCompanyProfiles -> {
//            saveProfiles(ocCompanyProfiles);
//        });

//        CompletableFuture<List<OcCompanyProfile>> cfuture = CompletableFuture.supplyAsync(() -> ocService.search(request), taskExecutor);
//        cfuture.thenAcceptAsync(ocCompanyProfiles -> {
//            saveProfiles(ocCompanyProfiles);
//        });
    }

    private void saveProfiles(List<OcCompanyProfile> profiles) {
        ocCompanyProfileRepository.saveAll(profiles);
    }

}
