package org.tutorial.springemailtutorial.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.MyColumnsDto;
import org.tutorial.springemailtutorial.dto.MyTaskDto;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.model.MyColumn;
import org.tutorial.springemailtutorial.repository.BoardRepository;
import org.tutorial.springemailtutorial.repository.MyColumnsRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class myColumnsService {

    private final MyColumnsRepository myColumnsRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BoardRepository boardRepository;
    private final TaskService taskService;

    @Autowired
    public myColumnsService(MyColumnsRepository myColumnsRepository,
                            UserRepository userRepository,
                            JwtService jwtService,
                            BoardRepository boardRepository,
                            TaskService taskService) {
        this.myColumnsRepository = myColumnsRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.boardRepository = boardRepository;
        this.taskService = taskService;
    }

    public MyColumn saveColumn(MyColumnsDto columnDto, Long boardId, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        List<MyColumn> boardColumns = myColumnsRepository.findByBoardIdOrderByPlacement(boardId);
        int placement = boardColumns.size() + 1;

        MyColumn column = new MyColumn();
        column.setTitle(columnDto.getTitle());
        column.setTitleColor(columnDto.getTitleColor());
        column.setBoard(board);
        column.setPlacement(placement);

        return myColumnsRepository.save(column);
    }

    @Transactional
    public void reorderColumns(List<MyColumnsDto> columnDtos, Long boardId, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));

        if (!board.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User not authorized to modify this board");
        }
        List<MyColumn> columns = myColumnsRepository.findByBoardUserIdAndBoardIdOrderByPlacement(user.getId(), boardId);
        Map<String, MyColumn> columnMap = columns.stream()
                .collect(Collectors.toMap(MyColumn::getTitle, Function.identity()));
        for (MyColumnsDto dto : columnDtos) {
            if (!columnMap.containsKey(dto.getTitle())) {
                throw new EntityNotFoundException("Column not found: " + dto.getTitle());
            }
        }
        for (int i = 0; i < columnDtos.size(); i++) {
            MyColumnsDto dto = columnDtos.get(i);
            MyColumn column = columnMap.get(dto.getTitle());

            column.setPlacement(i + 1);

            if (dto.getTitleColor() != null) {
                column.setTitleColor(dto.getTitleColor());
            }
        }
        myColumnsRepository.saveAll(columns);
    }


    @Transactional
    public MyColumn updateColumn(Long columnId, Long boardId, MyColumnsDto columnDto, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));

        if (!board.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User not authorized to modify this board");
        }
        MyColumn column = myColumnsRepository.findById(columnId)
                .orElseThrow(() -> new EntityNotFoundException("Column not found"));

        if (!column.getBoard().getId().equals(boardId)) {
            throw new IllegalArgumentException("Column does not belong to specified board");
        }
        if (columnDto.getTitle() != null && !columnDto.getTitle().isBlank()) {
            column.setTitle(columnDto.getTitle());
        }

        if (columnDto.getTitleColor() != null && !columnDto.getTitleColor().isBlank()) {
            column.setTitleColor(columnDto.getTitleColor());
        }
        if (columnDto.getPlacement() != null) {
            column.setPlacement(columnDto.getPlacement());
        }
        return myColumnsRepository.save(column);
    }

    @Transactional
    public void deleteColumn(Long columnId, Long boardId, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));

        if (!board.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User not authorized to modify this board");
        }
        MyColumn column = myColumnsRepository.findById(columnId)
                .orElseThrow(() -> new EntityNotFoundException("Column not found"));
        if (!column.getBoard().getId().equals(boardId)) {
            throw new IllegalArgumentException("Column does not belong to specified board");
        }
        myColumnsRepository.delete(column);
        List<MyColumn> remainingColumns = myColumnsRepository.findByBoardIdOrderByPlacement(boardId);
        for (int i = 0; i < remainingColumns.size(); i++) {
            MyColumn col = remainingColumns.get(i);
            col.setPlacement(i + 1);
        }
        myColumnsRepository.saveAll(remainingColumns);
    }

    public List<MyColumnsDto> getColumnsForBoard(Long boardId) {
        List<MyColumn> columns = myColumnsRepository.findByBoardIdOrderByPlacement(boardId);
        return columns.stream()
                .map(column -> {
                    List<MyTaskDto> tasks = taskService.getTasksForColumn(column.getId());
                    return new MyColumnsDto(
                            column.getId(),
                            column.getTitle(),
                            column.getPlacement(),
                            column.getTitleColor(),
                            tasks
                    );
                })
                .collect(Collectors.toList());
    }
}