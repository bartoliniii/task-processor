package pl.bkwapisz.taskprocessor.processing;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
record TaskStatus(@Id String id, ProcessingStatus status, int progress, TaskResult result) {

    static TaskStatus createNewTaskStatus() {
        return new TaskStatus(null, ProcessingStatus.NEW, 0, null);
    }

    record TaskResult(int position, int typos) {
    }

    enum ProcessingStatus {
        NEW, PROCESSING, FINISHED, FAILED
    }

}
