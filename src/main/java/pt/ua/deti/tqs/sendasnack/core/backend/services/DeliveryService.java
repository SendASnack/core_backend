package pt.ua.deti.tqs.sendasnack.core.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ua.deti.tqs.sendasnack.core.backend.model.Delivery;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.DeliveryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Autowired
    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    /**
     * Get All Free deliveries
     *
     * Get all deliveries that were not accepted by any rider yet.
     *
     * @return a list of all deliveries that have to be picked up by a rider.
     */
    public List<Delivery> getAllFreeDeliveries() {
        return deliveryRepository.findAllByRiderIsNull();
    }

    /**
     * Get Delivery By Id
     *
     * @return the delivery with the provided id.
     */
    public Optional<Delivery> getDeliveryById(Long id) {
        return deliveryRepository.findById(id);
    }

    @SuppressWarnings("all")
    public Long save(Delivery delivery) {
        Delivery savedDelivery = deliveryRepository.save(delivery);
        return savedDelivery != null ? savedDelivery.getId() : null;
    }

}
