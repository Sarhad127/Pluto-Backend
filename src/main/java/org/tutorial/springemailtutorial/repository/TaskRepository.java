package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorial.springemailtutorial.model.MyTask;

public interface TaskRepository extends JpaRepository<MyTask, Long> {
}
