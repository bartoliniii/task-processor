package pl.bkwapisz.taskprocessor.processing.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskQueueConfiguration {

    @Bean
    public Queue taskQueue(@Value("${taskProcessor.rabbitmq.tasksQueueName}") final String queueName) {
        return new Queue(queueName, true, false, false);
    }
}
