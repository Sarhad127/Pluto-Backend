package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorial.springemailtutorial.model.BoardChatMessage;

import java.util.List;

public interface BoardChatMessageRepository extends JpaRepository<BoardChatMessage, Long> {
    List<BoardChatMessage> findByBoardIdOrderByTimestampAsc(Long boardId);
}
