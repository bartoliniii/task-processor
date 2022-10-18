package pl.bkwapisz.taskprocessor;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@RunWith(SpringRunner.class)
@SpringBootTest
abstract class TaskProcessorAbstractIntegrationTest {

    @Container
    public static MongoDBContainer MONGODB_CONTAINER = new MongoDBContainer("mongo:6.0");

    @Container
    public static RabbitMQContainer RABBITMQ_CONTAINER = new RabbitMQContainer("rabbitmq:3.11-management");

    @BeforeAll
    public static void prepareContainers() {
        MONGODB_CONTAINER.withReuse(true);
        MONGODB_CONTAINER.start();
        System.setProperty("MONGO_URI", MONGODB_CONTAINER.getReplicaSetUrl());

        RABBITMQ_CONTAINER.withReuse(true);
        RABBITMQ_CONTAINER.start();
        System.setProperty("RABBITMQ_PORT", String.valueOf(RABBITMQ_CONTAINER.getAmqpPort()));
    }

    @AfterAll
    public static void turnOffContainers() {
        MONGODB_CONTAINER.stop();
    }
}
