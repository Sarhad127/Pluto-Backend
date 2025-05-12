package org.tutorial.springemailtutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.BoardDto;
import org.tutorial.springemailtutorial.dto.MyColumnsDto;
import org.tutorial.springemailtutorial.dto.MyTaskDto;
import org.tutorial.springemailtutorial.dto.UserDataDto;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.BoardRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;
import org.tutorial.springemailtutorial.service.JwtService;
import org.tutorial.springemailtutorial.service.TaskService;
import org.tutorial.springemailtutorial.service.myColumnsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserDataController {

    private final myColumnsService columnsService;
    private final TaskService taskService;
    private final UserRepository UserRepository;
    private final JwtService jwtService;
    private final BoardRepository boardRepository;

    @Autowired
    public UserDataController(myColumnsService columnsService,
                              TaskService taskService,
                              UserRepository UserRepository,
                              JwtService jwtService,
                              BoardRepository boardRepository) {
        this.columnsService = columnsService;
        this.taskService = taskService;
        this.UserRepository = UserRepository;
        this.jwtService = jwtService;
        this.boardRepository = boardRepository;
    }

    @GetMapping("/userdata")
    public ResponseEntity<UserDataDto> getUserData(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        User user = UserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        Long userId = user.getId();
        List<Board> boards = boardRepository.findByUsersContaining(user);
        List<BoardDto> boardDtos = boards.stream().map(board -> {
            BoardDto dto = new BoardDto();
            dto.setId(board.getId());
            dto.setTitle(board.getTitle());
            dto.setPosition(board.getPosition());
            dto.setUserId(userId);
            dto.setUserIds(board.getUsers().stream().map(User::getId).collect(Collectors.toList()));
            dto.setColumns(columnsService.getColumnsForBoard(board.getId()));
            return dto;
        }).collect(Collectors.toList());
        Long boardId = null;
        int boardPosition = 1;
        List<MyColumnsDto> selectedBoardColumns = new ArrayList<>();
        if (!boards.isEmpty()) {
            Board firstBoard = boards.get(0);
            boardId = firstBoard.getId();
            boardPosition = firstBoard.getPosition();
            selectedBoardColumns = columnsService.getColumnsForBoard(boardId);
        }
        List<MyTaskDto> tasks = taskService.getTasksForUser(authHeader);
        UserDataDto userDataDto = new UserDataDto();
        userDataDto.setUserId(userId);
        userDataDto.setBoardId(boardId);
        userDataDto.setBoardPosition(boardPosition);
        userDataDto.setColumns(selectedBoardColumns);
        userDataDto.setTasks(tasks);
        userDataDto.setBoards(boardDtos);
        return ResponseEntity.ok(userDataDto);
    }

    @GetMapping("/boards/{boardId}/users")
    public ResponseEntity<?> getUserOnBoard(@RequestHeader("Authorization") String authHeader,
                                            @PathVariable("boardId") Long boardId) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid authorization token");
        }
        try {
            String token = authHeader.substring(7).trim();
            String username = jwtService.extractUsername(token);
            User requestingUser = UserRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new RuntimeException("Board not found with id: " + boardId));
            if (!board.getUsers().contains(requestingUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User doesn't have access to this board");
            }
            Set<User> users = board.getUsers();
            if (users.size() <= 1) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Board has one or no other users");
            }
            List<String> otherUsernames = users.stream()
                    .filter(user -> !user.getId().equals(requestingUser.getId()))
                    .map(User::getUsername)
                    .collect(Collectors.toList());
            System.out.println(otherUsernames);
            return ResponseEntity.ok(otherUsernames);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request");
        }
    }

    @GetMapping("/user/email")
    public ResponseEntity<String> getUserEmail(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);

        User user = UserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        return ResponseEntity.ok(user.getEmail());
    }
}