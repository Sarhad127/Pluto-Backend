package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tutorial.springemailtutorial.model.MyColumn;

import java.util.List;

public interface MyColumnsRepository extends JpaRepository<MyColumn, Long> {

    List<MyColumn> findByBoardIdOrderByPlacement(Long boardId);

    @Query("SELECT c FROM MyColumn c JOIN c.board b JOIN b.users u WHERE u.id = :userId AND b.id = :boardId ORDER BY c.placement")
    List<MyColumn> findByBoardUserIdAndBoardIdOrderByPlacement(@Param("userId") Long userId, @Param("boardId") Long boardId);

    @Query("SELECT DISTINCT c FROM MyColumn c JOIN FETCH c.tasks t JOIN c.board b JOIN b.users u WHERE u.id = :userId")
    List<MyColumn> findByUserWithTasks(@Param("userId") Long userId);
}
