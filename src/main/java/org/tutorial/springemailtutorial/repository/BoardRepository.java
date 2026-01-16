package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.User;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByUsersContaining(User user);
    Optional<Board> findByUsersContainingAndPosition(User user, int position);
}
