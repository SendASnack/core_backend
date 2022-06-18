package pt.ua.deti.tqs.sendasnack.core.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.ForbiddenOperationException;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.IllegalOrderStatusException;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.OrderNotFoundException;
import pt.ua.deti.tqs.sendasnack.core.backend.model.Delivery;
import pt.ua.deti.tqs.sendasnack.core.backend.model.OrderRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.DeliveryStatus;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.OrderStatus;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.MessageResponse;
import pt.ua.deti.tqs.sendasnack.core.backend.security.auth.AuthHandler;
import pt.ua.deti.tqs.sendasnack.core.backend.services.DeliveryService;
import pt.ua.deti.tqs.sendasnack.core.backend.services.OrderRequestService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final AuthHandler authHandler;
    private final OrderRequestService orderRequestService;
    private final DeliveryService deliveryService;

    @Autowired
    public BusinessController(AuthHandler authHandler, OrderRequestService orderRequestService, DeliveryService deliveryService) {
        this.authHandler = authHandler;
        this.orderRequestService = orderRequestService;
        this.deliveryService = deliveryService;
    }

    @PostMapping("/orders")
    public MessageResponse createOrder(@RequestBody OrderRequest orderRequest) {

        if (orderRequest.getOrderStatus() == null)
            orderRequest.setOrderStatus(OrderStatus.PREPARING);

        orderRequest.setBusinessUsername(authHandler.getCurrentUsername());

        orderRequestService.save(orderRequest);

        Delivery delivery = new Delivery(null, orderRequest, orderRequest.getDeliveryTime(), DeliveryStatus.READY, null);
        deliveryService.save(delivery);

        return new MessageResponse("Your order was successfully placed.");

    }

    @DeleteMapping("/orders/{orderId}")
    public MessageResponse cancelOrder(@PathVariable Long orderId) {

        Optional<OrderRequest> orderRequestOptional = orderRequestService.getById(orderId);

        if (orderRequestOptional.isEmpty())
            throw new OrderNotFoundException(String.format("The order %s could not be found.", orderId));

        OrderRequest orderRequest = orderRequestOptional.get();

        if (orderRequest.getOrderStatus() != OrderStatus.PREPARING)
            throw new IllegalOrderStatusException("The order cannot be canceled cause it is no longer cancellable.");

        if (!orderRequest.getBusinessUsername().equals(authHandler.getCurrentUsername()))
            throw new ForbiddenOperationException("You don't have permission to cancel that order.");

        orderRequestService.delete(orderRequest);
        return new MessageResponse(String.format("The order %s was cancelled.", orderId));

    }

    @PatchMapping("/orders/{orderId}/status")
    public MessageResponse changeOrderStatus(@PathVariable Long orderId, @RequestBody OrderStatus orderStatus) {

        Optional<OrderRequest> orderRequestOptional = orderRequestService.getById(orderId);

        if (orderRequestOptional.isEmpty())
            throw new OrderNotFoundException(String.format("The order %s could not be found.", orderId));

        OrderRequest orderRequest = orderRequestOptional.get();

        if (orderStatus != OrderStatus.PREPARING && orderStatus != OrderStatus.READY)
            throw new IllegalOrderStatusException("A Business can only change the status of the delivery to 'Preparing' or 'Ready'.");

        if (!orderRequest.getBusinessUsername().equals(authHandler.getCurrentUsername()))
            throw new ForbiddenOperationException("You don't have permission to edit that order.");

        if (orderStatus == OrderStatus.READY)
            deliveryService.save(new Delivery(null, orderRequest, orderRequest.getDeliveryTime(), DeliveryStatus.READY, null));

        orderRequest.setOrderStatus(orderStatus);
        orderRequestService.save(orderRequest);

        return new MessageResponse(String.format("The status of the order %s was changed to %s.", orderId, orderStatus));

    }

    @GetMapping("/orders/{orderId}")
    public OrderStatus getOrderStatus(@PathVariable Long orderId) {

        Optional<OrderRequest> orderRequestOptional = orderRequestService.getById(orderId);

        if (orderRequestOptional.isEmpty())
            throw new OrderNotFoundException(String.format("The order %s could not be found.", orderId));

        OrderRequest orderRequest = orderRequestOptional.get();

        if (!orderRequest.getBusinessUsername().equals(authHandler.getCurrentUsername()))
            throw new ForbiddenOperationException("You don't have permission to view that order.");

        return orderRequest.getOrderStatus();

    }

    @GetMapping("/orders")
    public List<OrderRequest> getAllOrders() {
        return orderRequestService.getAllOrdersFromBusiness(authHandler.getCurrentUsername());
    }

}
