package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tutorial.springemailtutorial.model.MyColumn;

import java.util.List;

public interface MyColumnsRepository extends JpaRepository<MyColumn, Long> {
    List<MyColumn> findByBoardIdOrderByPlacement(Long boardId);
    List<MyColumn> findByBoardUserIdAndBoardIdOrderByPlacement(Long userId, Long boardId);
    @Query("SELECT c FROM MyColumn c JOIN FETCH c.tasks t WHERE c.board.user.id = :userId")
    List<MyColumn> findByUserWithTasks(@Param("userId") Long userId);
}
