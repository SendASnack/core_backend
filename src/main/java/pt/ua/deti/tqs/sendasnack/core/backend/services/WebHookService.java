package pt.ua.deti.tqs.sendasnack.core.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks.WebHook;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.WebHookRepository;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.WebHookEvent;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class WebHookService {

    private final WebHookRepository webHookRepository;

    @Autowired
    public WebHookService(WebHookRepository webHookRepository) {
        this.webHookRepository = webHookRepository;
    }

    public Optional<WebHook> getWebHook(Long id) {
        return webHookRepository.findById(id);
    }

    public List<WebHook> getRegisteredWebHooks(String businessUsername) {
        return webHookRepository.getAllByBusinessUsername(businessUsername);
    }

    public List<WebHook> getDeliveryStatusWebHook(String businessUsername, WebHookEvent webHookProperty) {
        return webHookRepository.getAllByBusinessUsernameAndWhen(businessUsername, webHookProperty);
    }

    public Long save(WebHook webHook) {
        WebHook storedWebHook = webHookRepository.save(webHook);
        return storedWebHook != null ? storedWebHook.getId() : null;
    }

    public void delete(Long webHookId) {
        if (!webHookRepository.existsById(webHookId))
            throw new EntityNotFoundException(String.format("Unable to find hook with id %s.", webHookId));
        webHookRepository.deleteById(webHookId);
    }

}
