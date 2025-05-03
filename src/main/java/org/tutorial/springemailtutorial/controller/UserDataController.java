package org.tutorial.springemailtutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        Long boardId = null;
        int boardPosition = 1;
        List<MyColumnsDto> columns = new ArrayList<>();
        if (!boards.isEmpty()) {
            Board board = boards.get(0);
            boardId = board.getId();
            boardPosition = board.getPosition();
            columns = columnsService.getColumnsForBoard(boardId);
        }
        List<MyTaskDto> tasks = taskService.getTasksForUser(authHeader);
        UserDataDto userDataDto = new UserDataDto();
        userDataDto.setUserId(userId);
        userDataDto.setBoardId(boardId);
        userDataDto.setBoardPosition(boardPosition);
        userDataDto.setColumns(columns);
        userDataDto.setTasks(tasks);
        return ResponseEntity.ok(userDataDto);
    }
}