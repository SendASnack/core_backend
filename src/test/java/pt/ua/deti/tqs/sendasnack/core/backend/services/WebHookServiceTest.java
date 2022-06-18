package pt.ua.deti.tqs.sendasnack.core.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks.Hook;
import pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks.WebHook;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.WebHookRepository;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.WebHookEvent;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebHookServiceTest {

    @Mock
    private WebHookRepository webHookRepository;

    @InjectMocks
    private WebHookService webHookService;

    private WebHook webHook;

    @BeforeEach
    void setUp() {
        webHook = new WebHook(1L, "Business", new Hook(1L, "https://myservice.com/", HttpMethod.POST, "VALUE"), WebHookEvent.DELIVERY_STATUS);
    }

    @Test
    void getWebHook() {

        assertThat(webHookService.getWebHook(webHook.getId()))
                .isNotNull()
                .isEmpty();

        when(webHookRepository.findById(webHook.getId())).thenReturn(Optional.of(webHook));

        assertThat(webHookService.getWebHook(webHook.getId()))
                .isNotNull()
                .isPresent();

        verify(webHookRepository, times(2)).findById(anyLong());

    }

    @Test
    void getRegisteredWebHooks() {

        assertThat(webHookService.getRegisteredWebHooks(webHook.getBusinessUsername()))
                .isNotNull()
                .isEmpty();

        when(webHookRepository.getAllByBusinessUsername(webHook.getBusinessUsername()))
                .thenReturn(Collections.singletonList(webHook));

        assertThat(webHookService.getRegisteredWebHooks(webHook.getBusinessUsername()))
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .containsOnly(webHook);

        assertThat(webHookService.getRegisteredWebHooks("SomeOtherUsername"))
                .isNotNull()
                .isEmpty();

        verify(webHookRepository, times(3)).getAllByBusinessUsername(anyString());

    }

    @Test
    void getDeliveryStatusWebHook() {

        assertThat(webHookService.getDeliveryStatusWebHook(webHook.getBusinessUsername(), webHook.getWhen()))
                .isNotNull()
                .isEmpty();

        when(webHookRepository.getAllByBusinessUsernameAndWhen(webHook.getBusinessUsername(), webHook.getWhen()))
                .thenReturn(Collections.singletonList(webHook));

        assertThat(webHookService.getDeliveryStatusWebHook(webHook.getBusinessUsername(), webHook.getWhen()))
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .containsOnly(webHook);

        assertThat(webHookService.getDeliveryStatusWebHook("SomeOtherUsername", webHook.getWhen()))
                .isNotNull()
                .isEmpty();

        assertThat(webHookService.getDeliveryStatusWebHook("SomeOtherUsername", null))
                .isNotNull()
                .isEmpty();

        verify(webHookRepository, times(4)).getAllByBusinessUsernameAndWhen(anyString(), any());

    }

    @Test
    void save() {
        webHookService.save(webHook);
        verify(webHookRepository, times(1)).save(webHook);
    }

    @Test
    void delete() {

        assertThrows(EntityNotFoundException.class, () -> webHookService.delete(webHook.getId()));
        verify(webHookRepository, times(1)).existsById(webHook.getId());

    }

}