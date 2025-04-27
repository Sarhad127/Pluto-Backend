package org.tutorial.springemailtutorial.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.MyTaskDto;
import org.tutorial.springemailtutorial.model.MyTask;
import org.tutorial.springemailtutorial.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestBody MyTaskDto taskDto, @RequestHeader("Authorization") String token) {
        try {
            System.out.println("Received Task DTO: " + taskDto);
            MyTask createdTask = taskService.createTask(taskDto, token);
            return ResponseEntity.status(201).body(createdTask);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create task: " + e.getMessage());
        }
    }

    @PostMapping("/reorder")
    public ResponseEntity<?> reorderTasks(@RequestBody List<MyTaskDto> taskDtos, @RequestHeader("Authorization") String token) {
        try {
            taskService.reorderTasks(taskDtos, token);
            return ResponseEntity.ok().body("Tasks reordered successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to reorder tasks: " + e.getMessage());
        }
    }
}
