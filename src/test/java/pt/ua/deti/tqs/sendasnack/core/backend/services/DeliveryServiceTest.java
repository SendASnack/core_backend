package pt.ua.deti.tqs.sendasnack.core.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import pt.ua.deti.tqs.sendasnack.core.backend.model.Delivery;
import pt.ua.deti.tqs.sendasnack.core.backend.model.OrderRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.DeliveryRepository;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.DeliveryStatus;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    private Delivery delivery;

    @BeforeEach
    void setUp() throws IOException {
        OrderRequest orderRequest = new ObjectMapper().readValue("{\n" +
                "    \"id\": 1,\n" +
                "    \"businessUsername\": \"Hugo1307\",\n" +
                "    \"costumer\": {\n" +
                "        \"name\": \"Costumer\",\n" +
                "        \"email\": \"hugo@ua.pt\",\n" +
                "        \"address\": {\n" +
                "            \"city\": \"Aveiro\",\n" +
                "            \"street\": \"Rua do Sol\",\n" +
                "            \"postalCode\": \"5680-654\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"order\": {\n" +
                "        \"date\": \"2022-05-30 00:00:00\",\n" +
                "        \"totalPrice\": 25.00,\n" +
                "        \"products\": [\n" +
                "            {\n" +
                "                \"name\": \"Product 1\",\n" +
                "                \"description\": \"This is the new product\",\n" +
                "                \"ingredients\": [\n" +
                "                    \"Lettice\",\n" +
                "                    \"Tomato\"\n" +
                "                ],\n" +
                "                \"price\": 25.00\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"deliveryTime\": \"2022-05-31 01:00:00\",\n" +
                "    \"orderStatus\": \"READY\"\n" +
                "}", OrderRequest.class);

        delivery = new Delivery(1L, orderRequest, Date.from(Instant.now()), DeliveryStatus.READY, null);

    }

    @Test
    void getAllFreeDeliveries() {

        assertThat(deliveryService.getAllFreeDeliveries()).isNotNull().isEmpty();

        when(deliveryRepository.findAllByRiderIsNull()).thenReturn(Collections.singletonList(delivery));

        assertThat(deliveryService.getAllFreeDeliveries()).isNotNull().hasSize(1);
        assertThat(deliveryService.getAllFreeDeliveries()).isNotNull().containsOnly(delivery);

        verify(deliveryRepository, times(3)).findAllByRiderIsNull();

    }

    @Test
    void getDeliveryById() {

        assertThat(deliveryService.getDeliveryById(1L)).isNotNull().isEmpty();

        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

        assertThat(deliveryService.getDeliveryById(1L)).isNotNull().isNotEmpty();
        assertThat(deliveryService.getDeliveryById(1L).get()).isEqualTo(delivery);

    }

    @Test
    void save() {
        deliveryService.save(delivery);
        verify(deliveryRepository, times(1)).save(delivery);
    }

}