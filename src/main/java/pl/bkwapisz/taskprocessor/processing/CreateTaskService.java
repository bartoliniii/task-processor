package pl.bkwapisz.taskprocessor.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.bkwapisz.taskprocessor.processing.dtos.Task;
import pl.bkwapisz.taskprocessor.processing.dtos.TaskInput;

@Service
@RequiredArgsConstructor
class CreateTaskService {

    @Value("${taskProcessor.rabbitmq.tasksQueueName}")
    private String queueName;

    private final TaskStatusService taskStatusService;
    private final RabbitTemplate rabbitTemplate;

    public TaskStatus createTask(final String input, final String pattern) {
        final var taskStatus = taskStatusService.createNewTaskStatus();
        rabbitTemplate.convertAndSend(queueName, new Task(taskStatus.id(), new TaskInput(input, pattern)));
        return taskStatus;
    }
}
