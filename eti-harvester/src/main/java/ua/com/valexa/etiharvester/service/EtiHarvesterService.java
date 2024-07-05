package ua.com.valexa.etiharvester.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ua.com.valexa.cdpcommon.dto.EtiExtractRequest;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
@Slf4j
public class EtiHarvesterService {

    @Value("${queue.eti.extractor}")
    private String queueEtiExtractor;

    @Autowired
    RabbitTemplate rabbitTemplate;

    AtomicInteger linksCount = new AtomicInteger(0);

    public Mono<Void> startHarvesting() {
        Mono.fromRunnable(this::collectAll)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        return Mono.empty();
    }

    public void collectAll(){

        long startTime = System.nanoTime();


        log.info("Start harvesting");

        Set<String> linksLevel1 = getLinksLevel1();
        log.info("Links LEVEL 1: {}", linksLevel1.size());

        Set<String> linksLevel2 = collectLinksLevel2(linksLevel1, 10);
        log.info("Links LEVEL 2: {}", linksLevel2.size());

        List<PagePair> pairs =  collectPagesCountForLinksLevel2(linksLevel2);
        log.info("Pairs: {}", pairs.size());

        Set<String> pageLinks = createPageLinks(pairs);
        log.info("Page links: {}", pageLinks.size());

        collectCompanyLinks(pageLinks, 10);
        log.info("Collected links: {}", linksCount.get());

        long endTime = System.nanoTime();

        long durationNano = endTime - startTime;
        Duration duration = Duration.ofNanos(durationNano);

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        String formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        System.out.println("Time taken: " + formattedDuration);
    }

    private Set<String> getLinksLevel1() {
        List<String> level1linksPostifx = Arrays.asList(
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
                "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
        );

        List<String> level1linksPostifxShort = Arrays.asList("z");

        return level1linksPostifx.stream()
                .map(l -> "https://eintaxid.com/companies/" + l + "/")
                .collect(Collectors.toSet());
    }

    private Set<String> collectLinksLevel2(Set<String> linksLevel1, int threadsCount) {
        Set<String> allLevel2urls = new HashSet<>();
        try {
            ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
            List<CompletableFuture<Set<String>>> futures = new ArrayList<>();
            for (String l : linksLevel1) {
                CompletableFuture<Set<String>> future = CompletableFuture.supplyAsync(() ->
                        getLinksLevel2(l), executor);
                futures.add(future);
            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            CompletableFuture<Set<String>> allLinksFuture = allOf.thenApply(v ->
                    futures.stream()
                            .map(CompletableFuture::join)
                            .flatMap(Set::stream)
                            .collect(Collectors.toSet())
            );
            allLevel2urls = allLinksFuture.get();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return allLevel2urls;
    }

    private Set<String> getLinksLevel2(String linkLevel1) {
        Set<String> links = new HashSet<>();
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);
            HtmlPage page = webClient.getPage(linkLevel1);
            links.addAll(page.getByXPath("/html/body/div[2]/div/div[1]/div/div[2]/a")
                    .stream()
                    .map(el -> (HtmlAnchor) el)
                    .map(el -> "https:" + el.getHrefAttribute())
                    .collect(Collectors.toSet()));
        } catch (Exception e) {
            log.error("getLevel2urls: " + e.getMessage());
        }
        return links;
    }

    private List<PagePair> collectPagesCountForLinksLevel2(Set<String> linksLevel2) {
        List<PagePair> pairs = new ArrayList<>();
        try {
            List<CompletableFuture<PagePair>> futures = new ArrayList<>();
            for (String l : linksLevel2) {
                CompletableFuture<PagePair> future = CompletableFuture.supplyAsync(() ->
                        getPagesCountFromLevel2(l));
                futures.add(future);
            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            CompletableFuture<List<PagePair>> allResults = allOf.thenApply(v ->
                    futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList())
            );
            pairs = allResults.get();

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return pairs;
    }

    private PagePair getPagesCountFromLevel2(String level2url) {
        int pagesCount = 0;
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);
            HtmlPage page = webClient.getPage(level2url);
            String rowsCountStr = ((DomText) page.getByXPath("/html/body/div[2]/div/div[2]/div[1]/div[2]/text()").get(0)).asNormalizedText();
            String[] parts = rowsCountStr.split(" ");
            pagesCount = (Integer.parseInt(parts[0]) + Integer.parseInt(parts[parts.length - 1]) - 1) / Integer.parseInt(parts[parts.length - 1]);
        } catch (Exception e) {
            log.error("getPagesCountFromLevel2: " + e.getMessage());
        }
        PagePair result = new PagePair();
        result.setLinkLevel2(level2url);
        result.setPagesCount(pagesCount);
        return result;
    }

    private Set<String> createPageLinks(List<PagePair> pairs){
        Set<String> links = new HashSet<>();
        for (PagePair pp : pairs){
            for (int i = 1; i <= pp.getPagesCount(); i++) {
                links.add(pp.getLinkLevel2() + "?page=" + i);
            }
        }
        return links;
    }

    public void collectCompanyLinks(Set<String> pageLinks, int threadsCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        try {
            List<CompletableFuture<Void>> futures = pageLinks.stream()
                    .map(pageLink -> CompletableFuture.runAsync(() -> sendCompanyLinksFromPage(pageLink), executor))
                    .collect(Collectors.toList());

            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.get(); // Wait for all futures to complete
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    private void sendCompanyLinksFromPage(String pageLink) {
        Set<String> links = new HashSet<>();
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;

        while (retryCount < maxRetries && !success) {
            try (WebClient webClient = new WebClient()) {
                webClient.getOptions().setJavaScriptEnabled(false);
                webClient.getOptions().setCssEnabled(false);
                webClient.getOptions().setTimeout(10000);
                HtmlPage page = webClient.getPage(pageLink);
//                log.info("Parsing link from page: " + pageLink);
                links = page.getByXPath("/html/body/div[2]/div/div[2]/div/div/div[2]/strong[1]/a")
                        .stream()
                        .map(link -> (HtmlAnchor) link)
                        .map(HtmlAnchor::getHrefAttribute)
                        .map(str -> "https://eintaxid.com" + str)
                        .collect(Collectors.toSet());
                success = true;
            } catch (Exception e) {
                retryCount++;
                if (retryCount < maxRetries) {
//                    log.info("Retrying... attempt " + (retryCount + 1));
                }
            }
        }
        log.info("TOTAL HARVESTED: " +  linksCount.get()  + "; Trying to send: " + links.size() + " links");
        linksCount.addAndGet(links.size());
        for (String link : links){
            rabbitTemplate.convertAndSend(queueEtiExtractor, new EtiExtractRequest(link));

        }
//        return links;
    }

    @Data
    class PagePair {
        private String linkLevel2;
        private Integer pagesCount;
    }

}
