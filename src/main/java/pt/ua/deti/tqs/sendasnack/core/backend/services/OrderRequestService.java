package pt.ua.deti.tqs.sendasnack.core.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.OrderNotFoundException;
import pt.ua.deti.tqs.sendasnack.core.backend.model.OrderRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.OrderRequestRepository;

import java.util.List;
import java.util.Optional;

@Service
public class OrderRequestService {

    private final OrderRequestRepository orderRequestRepository;

    @Autowired
    public OrderRequestService(OrderRequestRepository orderRequestRepository) {
        this.orderRequestRepository = orderRequestRepository;
    }

    public Optional<OrderRequest> getById(Long orderRequestId) {
        return orderRequestRepository.findById(orderRequestId);
    }

    public List<OrderRequest> getAllOrders() {
        return orderRequestRepository.findAll();
    }

    public List<OrderRequest> getAllOrdersFromBusiness(String businessUsername) {
        return orderRequestRepository.findAllByBusinessUsername(businessUsername);
    }

    public Long save(OrderRequest orderRequest) {
        OrderRequest savedEntity = orderRequestRepository.save(orderRequest);
        return savedEntity != null ? savedEntity.getId() : null;
    }

    public void delete(OrderRequest orderRequest) {
        if (!orderRequestRepository.existsById(orderRequest.getId()))
            throw new OrderNotFoundException("Order Not Found");
        orderRequestRepository.delete(orderRequest);
    }

}
