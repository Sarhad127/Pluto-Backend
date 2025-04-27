package org.tutorial.springemailtutorial.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.MyTaskDto;
import org.tutorial.springemailtutorial.model.MyTask;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.model.myColumns;
import org.tutorial.springemailtutorial.repository.MyColumnsRepository;
import org.tutorial.springemailtutorial.repository.TaskRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.List;
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
        MyTask task = new MyTask();
        task.setText(taskDto.getText());
        task.setColor(taskDto.getColor());
        task.setColumn(column);
        return TaskRepository.save(task);
    }

    @Transactional
    public void reorderTasks(List<MyTaskDto> taskDtos, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        for (MyTaskDto dto : taskDtos) {
            Optional<MyTask> taskOpt = TaskRepository.findById(dto.getId());
            if (taskOpt.isEmpty()) {
                throw new RuntimeException("Task not found.");
            }
            MyTask task = taskOpt.get();
            task.setText(dto.getText());
            TaskRepository.save(task);
        }
    }
}
