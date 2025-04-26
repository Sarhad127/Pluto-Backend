package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorial.springemailtutorial.model.myColumns;

public interface MyColumnsRepository extends JpaRepository<myColumns, Long> {
}
