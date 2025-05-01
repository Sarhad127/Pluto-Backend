package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tutorial.springemailtutorial.model.MyTask;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<MyTask, Long> {

    @Query("SELECT MAX(t.position) FROM MyTask t WHERE t.column.id = :columnId")
    Optional<Integer> findMaxPositionByColumnId(@Param("columnId") Long columnId);
}
