package pl.bkwapisz.taskprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
// unfortunately testcontainers for now requires clearing context
// to make sure that we connect to proper instance of container
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
public abstract class TaskProcessorAbstractIntegrationTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Container
    private static final MongoDBContainer MONGODB_CONTAINER = new MongoDBContainer("mongo:6.0");

    @Container
    private static final RabbitMQContainer RABBITMQ_CONTAINER = new RabbitMQContainer("rabbitmq:3.11");

    @DynamicPropertySource
    static void setupContainersProperties(DynamicPropertyRegistry registry) {
        MONGODB_CONTAINER.start();
        RABBITMQ_CONTAINER.start();
        registry.add("spring.data.mongodb.uri", MONGODB_CONTAINER::getReplicaSetUrl);
        registry.add("spring.rabbitmq.port", RABBITMQ_CONTAINER::getFirstMappedPort);
    }

    @SneakyThrows
    public String asJson(final Object obj) {
        return objectMapper.writeValueAsString(obj);
    }
}
