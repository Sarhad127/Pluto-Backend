package org.tutorial.springemailtutorial.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.MyTaskDto;
import org.tutorial.springemailtutorial.model.MyTask;
import org.tutorial.springemailtutorial.model.MyColumn;
import org.tutorial.springemailtutorial.repository.MyColumnsRepository;
import org.tutorial.springemailtutorial.repository.TaskRepository;
import org.tutorial.springemailtutorial.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@AllArgsConstructor
@CrossOrigin(origins = "https://todo-frontend-production-8fe7.up.railway.app")
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final MyColumnsRepository myColumnsRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestBody MyTaskDto taskDto, @RequestHeader("Authorization") String token) {
        try {
            System.out.println("Received Task DTO: " + taskDto);
            MyTaskDto createdTask = taskService.createTask(taskDto, token);

            MyTaskDto createdTaskDto = new MyTaskDto();
            createdTaskDto.setId(createdTask.getId());
            createdTaskDto.setText(createdTask.getText());
            createdTaskDto.setColor(createdTask.getColor());
            createdTaskDto.setTagText(createdTask.getTagText());
            createdTaskDto.setTagColor(createdTask.getTagColor());
            createdTaskDto.setColumnId(createdTask.getColumnId());
            createdTaskDto.setPosition(createdTask.getPosition());

            return ResponseEntity.status(201).body(createdTaskDto);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create task: " + e.getMessage());
        }
    }

    @PostMapping("/reorder")
    public ResponseEntity<?> reorderTasks(
            @RequestBody List<MyTaskDto> taskDtos) {
        for (MyTaskDto dto : taskDtos) {
            MyTask task = taskRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Task not found"));
            if (task.getPosition() != dto.getPosition()
                    || !task.getColumn().getId().equals(dto.getColumnId())) {
                task.setPosition(dto.getPosition());
                if (!task.getColumn().getId().equals(dto.getColumnId())) {
                    MyColumn newColumn = myColumnsRepository.findById(dto.getColumnId())
                            .orElseThrow(() -> new RuntimeException("Column not found"));
                    task.setColumn(newColumn);
                }
                taskRepository.save(task);
            }
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

    @PutMapping("/move/{taskId}")
    public ResponseEntity<?> moveTaskToAnotherColumn(
            @PathVariable Long taskId,
            @RequestParam Long newColumnId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            MyTask updatedTask = taskService.moveTaskToAnotherColumn(taskId, newColumnId, authHeader);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to move task: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId, @RequestHeader("Authorization") String authHeader) {
        try {
            taskService.deleteTask(taskId, authHeader);
            return ResponseEntity.status(200).body("Task deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete task: " + e.getMessage());
        }
    }
}
