package org.tutorial.springemailtutorial.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import org.tutorial.springemailtutorial.dto.ChatMessageDTO;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.BoardChatMessage;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.BoardChatMessageRepository;
import org.tutorial.springemailtutorial.repository.BoardRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;
import org.tutorial.springemailtutorial.service.JwtService;

import java.time.LocalDateTime;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardChatMessageRepository chatRepository;

    @Transactional
    @MessageMapping("/chat/{boardId}")
    public void handleChatMessage(@DestinationVariable Long boardId,
                                  ChatMessageDTO message,
                                  @Header("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header missing or invalid");
        }
        String token = authorizationHeader.substring(7);
        String username = jwtService.extractUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (!board.getUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized for this board");
        }

        BoardChatMessage saved = new BoardChatMessage();
        saved.setBoard(board);
        saved.setSender(user);
        saved.setMessage(message.getMessage());
        saved.setTimestamp(LocalDateTime.now());
        chatRepository.save(saved);

        message.setSender(user.getUsername());
        message.setTimestamp(saved.getTimestamp().toString());

        messagingTemplate.convertAndSend("/board/chat/" + boardId, message);
    }
}