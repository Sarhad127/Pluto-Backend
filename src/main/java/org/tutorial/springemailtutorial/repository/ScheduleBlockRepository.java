package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorial.springemailtutorial.model.ScheduleBlock;

import java.util.List;

public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlock, Long> {
    List<ScheduleBlock> findByUserId(Long userId);
}
