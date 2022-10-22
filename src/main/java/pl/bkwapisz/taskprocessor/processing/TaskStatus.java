package pl.bkwapisz.taskprocessor.processing;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
record TaskStatus(@Id String id, ProcessingStatus status, int progress, TaskResult result) {

    static TaskStatus createNew() {
        return new TaskStatus(null, ProcessingStatus.NEW, 0, null);
    }

    public TaskStatus withProgress(final int progress) {
        if (!status.equals(ProcessingStatus.PROCESSING)) {
            throw new IllegalStateException("To change progress task has to be marked as processing");
        }
        if (progress < 0 || progress > 100) {
            throw new IllegalStateException("Progress of task have to be >=0 and <=100");
        }
        return new TaskStatus(id, status, progress, result);
    }

    public TaskStatus asProcessing() {
        return new TaskStatus(id, ProcessingStatus.PROCESSING, 0, null);
    }

    public TaskStatus asFinished(TaskResult result) {
        return new TaskStatus(id, ProcessingStatus.FINISHED, 100, result);
    }

    public TaskStatus asFailed() {
        return new TaskStatus(id, ProcessingStatus.FAILED, progress, result);
    }

    record TaskResult(int position, int typos) {
    }

    enum ProcessingStatus {
        NEW, PROCESSING, FINISHED, FAILED
    }

}
