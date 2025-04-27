package org.tutorial.springemailtutorial.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.MyTaskDto;
import org.tutorial.springemailtutorial.model.MyTask;
import org.tutorial.springemailtutorial.model.myColumns;
import org.tutorial.springemailtutorial.repository.MyColumnsRepository;
import org.tutorial.springemailtutorial.repository.TaskRepository;
import org.tutorial.springemailtutorial.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final MyColumnsRepository myColumnsRepository;

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
    public ResponseEntity<?> reorderTasks(
            @RequestBody List<MyTaskDto> taskDtos,
            @RequestHeader("Authorization") String authHeader) {
        if (taskDtos.stream().anyMatch(dto -> dto.getId() == null)) {
            throw new IllegalArgumentException("All tasks must have IDs for reordering");
        }
        for (MyTaskDto dto : taskDtos) {
            MyTask task = taskRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Task not found: " + dto.getId()));
            task.setPosition(dto.getPosition());
            if (!task.getColumn().getId().equals(dto.getColumnId())) {
                myColumns newColumn = myColumnsRepository.findById(dto.getColumnId())
                        .orElseThrow(() -> new RuntimeException("Column not found"));
                task.setColumn(newColumn);
            }
            taskRepository.save(task);
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long taskId,
            @RequestBody MyTaskDto taskDto,
            @RequestHeader("Authorization") String token) {
        try {
            MyTask updatedTask = taskService.updateTask(taskId, taskDto, token);
            return ResponseEntity.status(200).body(updatedTask);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to update task: " + e.getMessage());
        }
    }
}
