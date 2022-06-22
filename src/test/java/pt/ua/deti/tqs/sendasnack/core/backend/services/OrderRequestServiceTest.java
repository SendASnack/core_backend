package pt.ua.deti.tqs.sendasnack.core.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.OrderNotFoundException;
import pt.ua.deti.tqs.sendasnack.core.backend.model.OrderRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.OrderRequestRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRequestServiceTest {

    @Mock
    private OrderRequestRepository orderRequestRepository;

    @InjectMocks
    private OrderRequestService orderRequestService;

    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() throws IOException {
        orderRequest = new ObjectMapper().readValue("{\n" +
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
    }

    @Test
    void getById() {

        when(orderRequestRepository.findById(orderRequest.getId())).thenReturn(Optional.of(orderRequest));

        assertThat(orderRequestService.getById(orderRequest.getId())).isNotNull().isPresent();
        assertThat(orderRequestService.getById(orderRequest.getId()).get().getId()).isEqualTo(orderRequest.getId());

        verify(orderRequestRepository, Mockito.times(2)).findById(Mockito.anyLong());

    }

    @Test
    void getAllOrders() {

        when(orderRequestRepository.findAll()).thenReturn(Collections.singletonList(orderRequest));

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().hasSize(1);

        when(orderRequestRepository.findAll()).thenReturn(new ArrayList<>());

        assertThat(orderRequestService.getAllOrders()).isNotNull().isEmpty();

        verify(orderRequestRepository, Mockito.times(2)).findAll();

    }

    @Test
    void getAllOrdersFromBusiness() {

        when(orderRequestRepository.findAllByBusinessUsername(orderRequest.getBusinessUsername())).thenReturn(Collections.singletonList(orderRequest));

        assertThat(orderRequestService.getAllOrdersFromBusiness(orderRequest.getBusinessUsername())).isNotNull().isNotEmpty().hasSize(1);

        when(orderRequestRepository.findAllByBusinessUsername(orderRequest.getBusinessUsername())).thenReturn(new ArrayList<>());

        assertThat(orderRequestService.getAllOrdersFromBusiness(orderRequest.getBusinessUsername())).isNotNull().isEmpty();

        verify(orderRequestRepository, Mockito.times(2)).findAllByBusinessUsername(Mockito.anyString());

    }

    @Test
    void save() {
        orderRequestService.save(orderRequest);
        verify(orderRequestRepository, Mockito.times(1)).save(orderRequest);
    }

    @Test
    void delete() {

        when(orderRequestRepository.existsById(orderRequest.getId())).thenReturn(false);

        assertThrows(OrderNotFoundException.class, () -> orderRequestService.delete(orderRequest));

        when(orderRequestRepository.existsById(orderRequest.getId())).thenReturn(true);

        orderRequestService.delete(orderRequest);

        verify(orderRequestRepository, times(1)).delete(any());
        verify(orderRequestRepository, times(2)).existsById(anyLong());

    }

}