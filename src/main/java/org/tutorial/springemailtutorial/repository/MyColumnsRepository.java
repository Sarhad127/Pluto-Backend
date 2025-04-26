package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorial.springemailtutorial.model.myColumns;

import java.util.List;

public interface MyColumnsRepository extends JpaRepository<myColumns, Long> {
    List<myColumns> findByUserIdOrderByPlacement(Long userId);
}
