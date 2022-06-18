package pt.ua.deti.tqs.sendasnack.core.backend.webhooks;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pt.ua.deti.tqs.sendasnack.core.backend.services.WebHookService;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.WebHookEvent;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.WebHookPostBody;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class WebHookHandler {

    private final WebHookService webHookService;

    private final RestTemplate restTemplate;

    @Autowired
    public WebHookHandler(WebHookService webHookService) {
        this.webHookService = webHookService;
        this.restTemplate = new RestTemplateBuilder().setConnectTimeout(Duration.of(5, ChronoUnit.SECONDS)).setReadTimeout(Duration.of(5, ChronoUnit.SECONDS)).build();
    }

    public void notifyAllHooks(String businessUsername, WebHookEvent webHookEvent, Object... args) {

        // args[0]: New Status; args[1]: Order Id;
        if (webHookEvent == WebHookEvent.DELIVERY_STATUS) {

            webHookService.getDeliveryStatusWebHook(businessUsername, webHookEvent)
                    .forEach(webHook -> {
                        log.info(String.format("Calling WebHook %s.", webHook.getHook().getUrl()));
                        CompletableFuture.runAsync(() -> restTemplate.postForEntity(webHook.getHook().getUrl(), new WebHookPostBody((String) args[0], (Long) args[1]), WebHookPostBody.class))
                                .thenAccept(unused -> log.info(String.format("Successfully called WebHook %s.", webHook.getHook().getUrl())))
                                .exceptionally(throwable -> {
                                    log.warn(String.format("Unable to reach WebHook %s", webHook.getHook().getUrl()));
                                    return null;
                                });
                    });
        }

    }

}
