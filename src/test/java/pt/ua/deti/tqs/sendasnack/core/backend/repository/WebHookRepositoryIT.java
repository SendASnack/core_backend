package pt.ua.deti.tqs.sendasnack.core.backend.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks.Hook;
import pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks.WebHook;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.WebHookEvent;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class WebHookRepositoryIT {

    @Autowired
    private WebHookRepository webHookRepository;

    private WebHook webHook;

    @Container
    public static final MariaDBContainer<?> mariaDb = new MariaDBContainer<>(DockerImageName.parse("mariadb"))
            .withDatabaseName("SendASnack_Core_Test")
            .withUsername("admin")
            .withPassword("admin")
            .withExposedPorts(3306)
            .waitingFor(new HttpWaitStrategy().forPort(3306)
                    .withStartupTimeout(Duration.ofMinutes(5)));

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",mariaDb::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDb::getUsername);
        registry.add("spring.datasource.password", mariaDb::getPassword);

    }

    @BeforeEach
    void setUp() {
        webHook = new WebHook(null, "Business", new Hook(null, "https://myservice.com/", HttpMethod.POST, "VALUE"), WebHookEvent.DELIVERY_STATUS);
    }

    @AfterEach
    void tearDown() {
        webHookRepository.deleteAll();
    }

    @Test
    void findById() {

        assertThat(webHookRepository.findById(0L)).isNotNull().isEmpty();

        Long id = webHookRepository.save(webHook).getId();

        assertThat(webHookRepository.findById(id)).isNotNull().isNotEmpty();
        assertThat(webHookRepository.findById(id).get()).isEqualTo(webHook);

    }

    @Test
    void existsById() {

        assertThat(webHookRepository.existsById(0L)).isNotNull().isFalse();

        Long id = webHookRepository.save(webHook).getId();

        assertThat(webHookRepository.existsById(id)).isNotNull().isTrue();

    }

    @Test
    void deleteById() {

        assertThat(webHookRepository.findAll()).doesNotContain(webHook);

        Long id = webHookRepository.save(webHook).getId();

        assertThat(webHookRepository.findAll()).contains(webHook);

        webHookRepository.deleteById(id);

        assertThat(webHookRepository.findAll()).doesNotContain(webHook);

    }

    @Test
    void getAllByBusinessUsername() {

        assertThat(webHookRepository.getAllByBusinessUsername("NoName")).isNotNull().isEmpty();
        assertThat(webHookRepository.getAllByBusinessUsername(webHook.getBusinessUsername())).isNotNull().isEmpty();

        webHookRepository.save(webHook);

        assertThat(webHookRepository.getAllByBusinessUsername(webHook.getBusinessUsername())).isNotNull().isNotEmpty().hasSize(1);

    }

    @Test
    void getAllByBusinessUsernameAndWhen() {

        assertThat(webHookRepository.getAllByBusinessUsernameAndWhen("NoName", null)).isNotNull().isEmpty();
        assertThat(webHookRepository.getAllByBusinessUsernameAndWhen(webHook.getBusinessUsername(), webHook.getWhen())).isNotNull().isEmpty();
        assertThat(webHookRepository.getAllByBusinessUsernameAndWhen(webHook.getBusinessUsername(), null)).isNotNull().isEmpty();

        webHookRepository.save(webHook);

        assertThat(webHookRepository.getAllByBusinessUsernameAndWhen(webHook.getBusinessUsername(), webHook.getWhen())).isNotNull().isNotEmpty().hasSize(1);
        assertThat(webHookRepository.getAllByBusinessUsernameAndWhen(webHook.getBusinessUsername(), null)).isNotNull().isEmpty();
        assertThat(webHookRepository.getAllByBusinessUsernameAndWhen("NoName", null)).isNotNull().isEmpty();

    }

}