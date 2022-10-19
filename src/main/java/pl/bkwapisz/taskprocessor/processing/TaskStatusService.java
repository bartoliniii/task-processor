package pl.bkwapisz.taskprocessor.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pl.bkwapisz.taskprocessor.processing.dtos.Task;
import pl.bkwapisz.taskprocessor.processing.dtos.TaskInput;

import java.util.Optional;

@Service
@RequiredArgsConstructor
class TaskStatusService {

    private static String exchangeName = "testExchange";

    private final TaskStatusRepository taskStatusRepository;
    private final RabbitTemplate rabbitTemplate;

    public TaskStatus createTask(final String input, final String pattern) {
        //todo extract to separate service handing status
        final var savedTask = taskStatusRepository.save(TaskStatus.createNewTaskStatus());
        rabbitTemplate.convertAndSend(exchangeName, new Task(savedTask.id(), new TaskInput(input, pattern)));
        return savedTask;
    }

    public Optional<TaskStatus> getTaskStatus(final String taskId) {
        return taskStatusRepository.findById(taskId);
    }

    public Iterable<TaskStatus> getAllTaskStatuses() {
        return taskStatusRepository.findAll();
    }
}
