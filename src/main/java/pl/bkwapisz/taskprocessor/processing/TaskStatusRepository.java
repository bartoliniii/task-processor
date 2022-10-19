package pl.bkwapisz.taskprocessor.processing;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TaskStatusRepository extends CrudRepository<TaskStatus, String> {
}
