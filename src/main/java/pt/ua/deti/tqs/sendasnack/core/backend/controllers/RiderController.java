package pt.ua.deti.tqs.sendasnack.core.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.ForbiddenOperationException;
import pt.ua.deti.tqs.sendasnack.core.backend.model.Delivery;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.RiderUser;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.User;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.*;
import pt.ua.deti.tqs.sendasnack.core.backend.security.auth.AuthHandler;
import pt.ua.deti.tqs.sendasnack.core.backend.services.DeliveryService;
import pt.ua.deti.tqs.sendasnack.core.backend.services.UserService;
import pt.ua.deti.tqs.sendasnack.core.backend.webhooks.WebHookHandler;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/rider")
public class RiderController {

    private final UserService userService;
    private final DeliveryService deliveryService;
    private final AuthHandler authHandler;
    private final WebHookHandler webHookHandler;

    @Autowired
    public RiderController(UserService userService, DeliveryService deliveryService, AuthHandler authHandler, WebHookHandler webHookHandler) {
        this.userService = userService;
        this.deliveryService = deliveryService;
        this.authHandler = authHandler;
        this.webHookHandler = webHookHandler;
    }

    @PatchMapping("/deliveries/{deliveryId}/accept")
    public MessageResponse acceptDelivery(@PathVariable Long deliveryId) {

        List<Delivery> freeDeliveries = deliveryService.getAllFreeDeliveries();
        Optional<Delivery> deliveryOptional = freeDeliveries.stream()
                .filter(delivery -> delivery.getId().equals(deliveryId))
                .findFirst();

        if (deliveryOptional.isEmpty()) {
            throw new EntityNotFoundException(String.format("Unable to find the delivery %s.", deliveryId));
        }

        Delivery delivery = deliveryOptional.get();
        User rider = userService.findByUsername(authHandler.getCurrentUsername());

        if (!(rider instanceof RiderUser)) {
            throw new ForbiddenOperationException("You must be a rider to do that.");
        }

        ((RiderUser) rider).getAcceptedDeliveries().add(delivery);
        userService.save(rider);

        delivery.setRiderUsername(rider.getUsername());
        deliveryService.save(delivery);

        return new MessageResponse("Delivery successfully accepted.");

    }

    @PatchMapping("/deliveries/{deliveryId}/reject")
    public MessageResponse rejectDelivery(@PathVariable Long deliveryId) {

        List<Delivery> freeDeliveries = deliveryService.getAllFreeDeliveries();
        Optional<Delivery> deliveryOptional = freeDeliveries.stream()
                .filter(delivery -> delivery.getId().equals(deliveryId))
                .findFirst();

        if (deliveryOptional.isEmpty()) {
            throw new EntityNotFoundException(String.format("Unable to find the delivery %s.", deliveryId));
        }

        Delivery delivery = deliveryOptional.get();
        User rider = userService.findByUsername(authHandler.getCurrentUsername());

        if (!(rider instanceof RiderUser)) {
            throw new ForbiddenOperationException("You must be a rider to do that.");
        }

        ((RiderUser) rider).getRejectedDeliveries().add(delivery);
        userService.save(rider);

        return new MessageResponse("Delivery successfully rejected.");

    }

    @PatchMapping("/deliveries/{deliveryId}/status")
    public MessageResponse changeDeliveryStatus(@PathVariable Long deliveryId, @RequestBody DeliveryStatus deliveryStatus) {

        Optional<Delivery> deliveryOpt = deliveryService.getDeliveryById(deliveryId);

        if (deliveryOpt.isEmpty()) {
            throw new EntityNotFoundException(String.format("Unable to find a delivery with id %s.", deliveryId));
        }

        Delivery delivery = deliveryOpt.get();

        delivery.setDeliveryStatus(deliveryStatus);
        deliveryService.save(delivery);

        webHookHandler.notifyAllHooks(authHandler.getCurrentUsername(), WebHookEvent.DELIVERY_STATUS, deliveryStatus.toString(), deliveryId);

        return new MessageResponse(String.format("Delivery status changed to %s.", deliveryStatus));

    }

    @GetMapping("/deliveries")
    public List<Delivery> getRiderDeliveries(@RequestParam Optional<DeliveryFilter> deliveryFilter) {

        User user = userService.findByUsername(authHandler.getCurrentUsername());

        if (!(user instanceof RiderUser)) {
            throw new ForbiddenOperationException("Only riders can access this endpoint.");
        }

        RiderUser rider = (RiderUser) user;

        if (deliveryFilter.isEmpty()) {
            return new ArrayList<>(rider.getAcceptedDeliveries());
        } else {
            switch (deliveryFilter.get()) {
                case AVAILABLE:
                default:
                    return deliveryService.getAllFreeDeliveries();
                case ONGOING:
                    return rider.getAcceptedDeliveries()
                            .stream()
                            .filter(delivery -> delivery.getDeliveryStatus() != DeliveryStatus.DELIVERED)
                            .collect(Collectors.toList());
                case HISTORY_ACCEPTED:
                    return rider.getAcceptedDeliveries()
                            .stream()
                            .filter(delivery -> delivery.getDeliveryStatus() == DeliveryStatus.DELIVERED)
                            .collect(Collectors.toList());
                case HISTORY_REJECTED:
                    return new ArrayList<>(rider.getRejectedDeliveries());
            }
        }

    }

    @GetMapping("/profile/{username}")
    public RiderUser getUserProfile(@PathVariable String username) {

        User userFound = userService.findByUsername(username);

        if (!(userFound instanceof RiderUser)) {
            throw new EntityNotFoundException(String.format("Unable to find the rider with username %s.", username));
        }

        RiderUser riderUser = (RiderUser) userFound;

        riderUser.setAcceptedDeliveries(null);
        riderUser.setRejectedDeliveries(null);

        return riderUser;

    }

    @PatchMapping("/profile/{username}/availability")
    public MessageResponse changeAvailabilityStatus(@PathVariable String username, @RequestBody AvailabilityStatus availabilityStatus) {

        User userFound = userService.findByUsername(username);

        if (!(userFound instanceof RiderUser)) {
            throw new EntityNotFoundException(String.format("Unable to find the rider with username %s.", username));
        }

        RiderUser riderUser = (RiderUser) userFound;
        riderUser.setAvailabilityStatus(availabilityStatus);

        userService.save(riderUser);

        return new MessageResponse(String.format("The status was changed to %s.", availabilityStatus));

    }

}
