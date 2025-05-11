package org.tutorial.springemailtutorial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.MyTaskDto;
import org.tutorial.springemailtutorial.model.MyTask;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.model.MyColumn;
import org.tutorial.springemailtutorial.repository.MyColumnsRepository;
import org.tutorial.springemailtutorial.repository.TaskRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public MyTaskDto createTask(MyTaskDto taskDto, String authHeader) {
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
        Optional<MyColumn> columnOpt = myColumnsRepository.findById(taskDto.getColumnId());
        if (columnOpt.isEmpty()) {
            throw new RuntimeException("Column not found.");
        }
        MyColumn column = columnOpt.get();
        Optional<Integer> maxPositionOpt = TaskRepository.findMaxPositionByColumnId(column.getId());
        int newPosition = maxPositionOpt.map(pos -> pos + 1).orElse(1);
        MyTask task = new MyTask();
        task.setText(taskDto.getText());
        task.setColor(taskDto.getColor());
        task.setTagText(taskDto.getTagText());
        task.setTagColor(taskDto.getTagColor());
        task.setPosition(newPosition);
        task.setColumn(column);
        MyTask savedTask = TaskRepository.save(task);

        MyTaskDto savedTaskDto = new MyTaskDto();
        savedTaskDto.setId(savedTask.getId());
        savedTaskDto.setText(savedTask.getText());
        savedTaskDto.setColor(savedTask.getColor());
        savedTaskDto.setTagText(savedTask.getTagText());
        savedTaskDto.setTagColor(savedTask.getTagColor());
        savedTaskDto.setColumnId(savedTask.getColumn().getId());
        savedTaskDto.setPosition(savedTask.getPosition());
        return savedTaskDto;
    }

    public MyTask updateTask(Long taskId, MyTaskDto taskDto, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        Optional<MyTask> taskOpt = TaskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("Task not found with ID: " + taskId);
        }
        MyTask task = taskOpt.get();
        if (taskDto.getTagText() != null) {
            task.setTagText(taskDto.getTagText());
        }

        task.setTagColor(taskDto.getTagColor());

        if (taskDto.getText() != null && !taskDto.getText().trim().isEmpty()) {
            task.setText(taskDto.getText());
        }
        if (taskDto.getColor() != null) {
            task.setColor(taskDto.getColor());
        }
        if (taskDto.getColumnId() != null) {
            Optional<MyColumn> columnOpt = myColumnsRepository.findById(taskDto.getColumnId());
            if (columnOpt.isEmpty()) {
                throw new RuntimeException("Column not found.");
            }
            task.setColumn(columnOpt.get());
        }
        return TaskRepository.save(task);
    }

    public MyTask moveTaskToAnotherColumn(Long taskId, Long newColumnId, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        Optional<MyTask> taskOpt = TaskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("Task not found with ID: " + taskId);
        }
        MyTask task = taskOpt.get();
        Optional<MyColumn> newColumnOpt = myColumnsRepository.findById(newColumnId);
        if (newColumnOpt.isEmpty()) {
            throw new RuntimeException("Column not found with ID: " + newColumnId);
        }
        MyColumn newColumn = newColumnOpt.get();
        task.setColumn(newColumn);
        Optional<Integer> maxPositionOpt = TaskRepository.findMaxPositionByColumnId(newColumnId);
        int newPosition = maxPositionOpt.map(pos -> pos + 1).orElse(1);
        task.setPosition(newPosition);
        return TaskRepository.save(task);
    }

    public List<MyTaskDto> getTasksForUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        List<MyColumn> columns = myColumnsRepository.findByUserWithTasks(user.getId());
        List<MyTask> tasks = columns.stream()
                .flatMap(col -> col.getTasks().stream())
                .collect(Collectors.toList());
        return tasks.stream()
                .map(task -> new MyTaskDto(
                        task.getId(),
                        task.getText(),
                        task.getColor(),
                        task.getTagText(),
                        task.getTagColor(),
                        task.getColumn().getId(),
                        task.getPosition()
                ))
                .collect(Collectors.toList());
    }

    public void deleteTask(Long taskId, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        Optional<MyTask> taskOpt = TaskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("Task not found with ID: " + taskId);
        }
        MyTask task = taskOpt.get();
        TaskRepository.delete(task);
    }

    public List<MyTaskDto> getTasksForColumn(Long columnId) {
        List<MyTask> tasks = TaskRepository.findByColumnId(columnId);
        return tasks.stream()
                .map(task -> new MyTaskDto(
                        task.getId(),
                        task.getText(),
                        task.getColor(),
                        task.getTagText(),
                        task.getTagColor(),
                        task.getColumn().getId(),
                        task.getPosition()
                ))
                .collect(Collectors.toList());
    }
}
