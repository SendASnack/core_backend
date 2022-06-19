package pt.ua.deti.tqs.sendasnack.core.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.WebHookDAO;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.ForbiddenOperationException;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.BusinessUser;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.User;
import pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks.WebHook;
import pt.ua.deti.tqs.sendasnack.core.backend.security.auth.AuthHandler;
import pt.ua.deti.tqs.sendasnack.core.backend.services.UserService;
import pt.ua.deti.tqs.sendasnack.core.backend.services.WebHookService;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.MessageResponse;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/business/webhook")
public class WebHooksController {

    private final WebHookService webHookService;
    private final UserService userService;
    private final AuthHandler authHandler;

    @Autowired
    public WebHooksController(WebHookService webHookService, UserService userService, AuthHandler authHandler) {
        this.webHookService = webHookService;
        this.userService = userService;
        this.authHandler = authHandler;
    }

    @PostMapping("")
    public MessageResponse registerWebHook(@RequestBody WebHookDAO webHookDAO) {

        User user = userService.findByUsername(authHandler.getCurrentUsername());

        if (!(user instanceof BusinessUser)) {
            throw new ForbiddenOperationException("You must have a business account to do this.");
        }

        webHookDAO.setBusinessUsername(authHandler.getCurrentUsername());
        webHookService.save(webHookDAO.toDataEntity());

        return new MessageResponse("Your hook was successfully registered.");

    }

    @DeleteMapping("/{webHookId}")
    public MessageResponse deleteWebHook(@PathVariable Long webHookId) {

        User user = userService.findByUsername(authHandler.getCurrentUsername());

        if (!(user instanceof BusinessUser)) {
            throw new ForbiddenOperationException("You must have a business account to do this.");
        }

        Optional<WebHook> webHookOptional = webHookService.getWebHook(webHookId);

        if (webHookOptional.isEmpty()) {
            throw new EntityNotFoundException(String.format("Unable to find webHook with id %s.", webHookId));
        }

        if (!webHookOptional.get().getBusinessUsername().equals(user.getUsername())) {
            throw new ForbiddenOperationException("You must own the webHook to unregister it.");
        }

        webHookService.delete(webHookId);

        return new MessageResponse("Your hook was successfully unregistered.");

    }

    @GetMapping("")
    public List<WebHook> getAllWebHooks() {

        User user = userService.findByUsername(authHandler.getCurrentUsername());

        if (!(user instanceof BusinessUser)) {
            throw new ForbiddenOperationException("You must have a business account to do this.");
        }

        return webHookService.getRegisteredWebHooks(user.getUsername());

    }

}
