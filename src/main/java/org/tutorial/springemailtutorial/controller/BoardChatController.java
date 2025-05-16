package org.tutorial.springemailtutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.BoardChatMessage;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.BoardChatMessageRepository;
import org.tutorial.springemailtutorial.repository.BoardRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;
import org.tutorial.springemailtutorial.service.JwtService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/boards/{boardId}/chat")
public class BoardChatController {

    @Autowired
    private BoardChatMessageRepository chatRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    public static class ChatMessageDTO {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @PostMapping
    public ResponseEntity<BoardChatMessage> sendMessage(@PathVariable Long boardId,
                                                        @RequestBody ChatMessageDTO chatMessageDTO,
                                                        @RequestHeader("Authorization") String authHeader) {
        String username = jwtService.extractUsername(authHeader.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (!board.getUsers().contains(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BoardChatMessage chat = new BoardChatMessage();
        chat.setBoard(board);
        chat.setSender(user);
        chat.setMessage(chatMessageDTO.getMessage());
        chat.setTimestamp(LocalDateTime.now());

        return ResponseEntity.ok(chatRepository.save(chat));
    }

    @GetMapping
    public ResponseEntity<List<BoardChatMessage>> getMessages(@PathVariable Long boardId,
                                                              @RequestHeader("Authorization") String authHeader) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        String username = jwtService.extractUsername(authHeader.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!board.getUsers().contains(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<BoardChatMessage> messages = chatRepository.findByBoardIdOrderByTimestampAsc(boardId);
        return ResponseEntity.ok(messages);
    }
}
