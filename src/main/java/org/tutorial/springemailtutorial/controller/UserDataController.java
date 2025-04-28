package org.tutorial.springemailtutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.MyColumnsDto;
import org.tutorial.springemailtutorial.dto.MyTaskDto;
import org.tutorial.springemailtutorial.dto.UserDataDto;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.UserRepository;
import org.tutorial.springemailtutorial.service.JwtService;
import org.tutorial.springemailtutorial.service.TaskService;
import org.tutorial.springemailtutorial.service.myColumnsService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserDataController {

    private final myColumnsService columnsService;
    private final TaskService taskService;
    private final UserRepository UserRepository;
    private final JwtService jwtService;

    @Autowired
    public UserDataController(myColumnsService columnsService, TaskService taskService, UserRepository UserRepository, JwtService jwtService) {
        this.columnsService = columnsService;
        this.taskService = taskService;
        this.UserRepository = UserRepository;
        this.jwtService = jwtService;
    }

    @GetMapping("/userdata")
    public ResponseEntity<UserDataDto> getUserData(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> user = UserRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        Optional<User> userOptional = UserRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with username: " + username);
        }
        Long userId = user.get().getId();
        List<MyColumnsDto> columns = columnsService.getColumnsForUser(userId);
        List<MyTaskDto> tasks = taskService.getTasksForUser(userId);
        UserDataDto userDataDto = new UserDataDto();
        userDataDto.setUserId(userId);
        userDataDto.setColumns(columns);
        userDataDto.setTasks(tasks);
        return new ResponseEntity<>(userDataDto, HttpStatus.OK);
    }
}