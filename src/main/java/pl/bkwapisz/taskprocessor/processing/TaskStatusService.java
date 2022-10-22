package pl.bkwapisz.taskprocessor.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;

    public TaskStatus createNewTaskStatus() {
        return taskStatusRepository.save(TaskStatus.createNew());
    }

    public Iterable<TaskStatus> getAllTaskStatuses() {
        return taskStatusRepository.findAll();
    }

    public void updateTaskProgress(final String taskId, final int progress) {
        final var statusToUpdate = getTaskStatus(taskId);
        taskStatusRepository.save(statusToUpdate.withProgress(progress));
    }

    public void markTaskAsBeingProcessed(final String taskId) {
        final var statusToUpdate = getTaskStatus(taskId);
        taskStatusRepository.save(statusToUpdate.asProcessing());
    }

    public void markTaskAsFailed(final String taskId) {
        final var statusToUpdate = getTaskStatus(taskId);
        taskStatusRepository.save(statusToUpdate.asFailed());
    }

    public void markTaskAsFinished(final String taskId, final TextSearchPatternEngine.SearchResult result) {
        final var statusToUpdate = getTaskStatus(taskId);
        final var taskResult = new TaskStatus.TaskResult(result.position(), result.typos());
        taskStatusRepository.save(statusToUpdate.asFinished(taskResult));
    }

    public Optional<TaskStatus> getTaskStatusOpt(final String taskId) {
        return taskStatusRepository.findById(taskId);
    }

    TaskStatus getTaskStatus(final String taskId) {
        return getTaskStatusOpt(taskId).orElseThrow();
    }
}
