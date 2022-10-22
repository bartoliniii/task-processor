package pl.bkwapisz.taskprocessor.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
class TaskController {

    record CreateTaskRequest(@NotEmpty String pattern, @NotEmpty String input) {
    }

    record CreateTaskResponse(String id) {
    }

    record TaskResult(int position, int typos) {
        TaskResult(final TaskStatus.TaskResult taskStatus) {
            this(taskStatus.position(), taskStatus.typos());
        }
    }

    record TaskResultResponse(String id, String status, String progress, TaskResult result) {

        static TaskResultResponse create(final TaskStatus taskStatus) {
            final var status = mapStatus(taskStatus.status());
            final var progress = taskStatus.progress() + "%";
            final var taskResult = taskStatus.result() != null ? new TaskResult(taskStatus.result()) : null;
            return new TaskResultResponse(taskStatus.id(), status, progress, taskResult);
        }

        private static String mapStatus(final TaskStatus.ProcessingStatus status) {
            return switch (status) {
                case NEW -> "new";
                case PROCESSING -> "processing";
                case FINISHED -> "finished";
                case FAILED -> "failed";
            };
        }
    }

    private final CreateTaskService createTaskService;
    private final TaskStatusService taskStatusService;

    @PostMapping
    public CreateTaskResponse createTask(@RequestBody @Valid final CreateTaskRequest task) {
        log.info("called createTask with params: {}", task);
        final String createdTaskId = createTaskService.createTask(task.input, task.pattern).id();
        return new CreateTaskResponse(createdTaskId);
    }

    @GetMapping("/{id}")
    public TaskResultResponse getTask(@PathVariable final String id) {
        log.debug("called getTask for id: {}", id);
        return taskStatusService.getTaskStatusOpt(id)
                .map(TaskResultResponse::create)
                .orElseThrow(this::notFoundException);
    }

    @GetMapping
    public List<TaskResultResponse> getTasks() {
        log.debug("called getTasks");
        final var taskStatuses = taskStatusService.getAllTaskStatuses();
        return StreamSupport.stream(taskStatuses.spliterator(), false)
                .map(TaskResultResponse::create)
                .toList();
    }

    private RuntimeException notFoundException() {
        return new ResponseStatusException(NOT_FOUND, "There is no resource with given id");
    }

}
