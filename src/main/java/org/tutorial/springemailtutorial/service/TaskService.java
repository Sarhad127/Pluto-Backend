package org.tutorial.springemailtutorial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.MyTaskDto;
import org.tutorial.springemailtutorial.model.MyTask;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.model.myColumns;
import org.tutorial.springemailtutorial.repository.MyColumnsRepository;
import org.tutorial.springemailtutorial.repository.TaskRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository TaskRepository;
    private final MyColumnsRepository myColumnsRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public TaskService(TaskRepository TaskRepository, MyColumnsRepository myColumnsRepository,
                       UserRepository userRepository, JwtService jwtService) {
        this.TaskRepository = TaskRepository;
        this.myColumnsRepository = myColumnsRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public MyTask createTask(MyTaskDto taskDto, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        if (taskDto.getText() == null || taskDto.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Task text cannot be empty");
        }
        if (taskDto.getColumnId() == null) {
            throw new IllegalArgumentException("Column ID cannot be null");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        Optional<myColumns> columnOpt = myColumnsRepository.findById(taskDto.getColumnId());
        if (columnOpt.isEmpty()) {
            throw new RuntimeException("Column not found.");
        }
        myColumns column = columnOpt.get();
        Optional<Integer> maxPositionOpt = TaskRepository.findMaxPositionByColumnId(column.getId());
        int newPosition = maxPositionOpt.map(pos -> pos + 1).orElse(1);
        MyTask task = new MyTask();
        task.setText(taskDto.getText());
        task.setColor(taskDto.getColor());
        task.setPosition(newPosition);
        task.setColumn(column);
        return TaskRepository.save(task);
    }
}
