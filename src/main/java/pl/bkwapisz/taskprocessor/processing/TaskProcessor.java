package pl.bkwapisz.taskprocessor.processing;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.bkwapisz.taskprocessor.processing.dtos.Task;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "taskProcessor.rabbitmq.taskProcessorEnabled", havingValue = "true", matchIfMissing = false)
class TaskProcessor {

    private final TaskStatusService taskStatusService;
    private final TextSearchPatternEngine textSearchPatternEngine;
    private final Random random = new Random();

    @RabbitListener(
            queues = "${taskProcessor.rabbitmq.tasksQueueName}",
            concurrency = "${taskProcessor.rabbitmq.tasksQueueListenerConcurrency}"
    )
    public void processTask(Task task) {
        log.info("Got task to processing: {}", task);
        try {
            taskStatusService.markTaskAsBeingProcessed(task.taskId());
            sleepSomeSeconds();
            final TextSearchPatternEngine.SearchResult result = textSearchPatternEngine.findPattern(
                    task.taskInput().input(),
                    task.taskInput().pattern(),
                    progress -> updateProgress(task.taskId(), progress)
            );
            updateProgress(task.taskId(), 100);
            taskStatusService.markTaskAsFinished(task.taskId(), result);
            log.info("Task({}) calculation finished", task.taskId());
        } catch (RuntimeException ex) {
            log.error("Exception occurred during calculation task: {}", task.taskId(), ex);
            taskStatusService.markTaskAsFailed(task.taskId());
        }
    }

    @SneakyThrows
    private void sleepSomeSeconds() {
        TimeUnit.SECONDS.sleep(1 + random.nextInt(5));
    }

    private void updateProgress(final String taskId, final Integer progress) {
        log.debug("Updated task ({}) progress: {}%", taskId, progress);
        taskStatusService.updateTaskProgress(taskId, progress);
    }

}
