package org.tutorial.springemailtutorial.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.BoardDto;
import org.tutorial.springemailtutorial.dto.MyColumnsDto;
import org.tutorial.springemailtutorial.dto.MyTaskDto;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.MyColumn;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.BoardRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TaskService taskService;

    public String extractUsernameFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        return jwtService.extractUsername(token);
    }

    public List<BoardDto> getBoards(String authHeader) {
        String username = extractUsernameFromToken(authHeader);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        User user = userOptional.get();
        List<Board> boards = boardRepository.findByUsersContaining(user);
        return boards.stream()
                .map(board -> new BoardDto(
                        board.getId(),
                        board.getTitle(),
                        board.getPosition(),
                        board.getId(),
                        board.getUsers().stream().map(User::getId).collect(Collectors.toList()),
                        convertToColumnsDto(board.getColumns())
                ))
                .collect(Collectors.toList());
    }

    public BoardDto createBoard(Board board, String authHeader) {
        String username = extractUsernameFromToken(authHeader);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        User user = userOptional.get();
        if (board.getTitle() == null || board.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Board name cannot be empty");
        }
        board.getUsers().add(user);
        List<Board> userBoards = boardRepository.findByUsersContaining(user);
        if (userBoards.size() >= 5) {
            throw new IllegalStateException("You can only have a maximum of 5 boards.");
        }
        int maxPosition = userBoards.stream()
                .mapToInt(Board::getPosition)
                .max()
                .orElse(0);
        board.setPosition(maxPosition + 1);
        Board savedBoard = boardRepository.save(board);
        return new BoardDto(
                savedBoard.getId(),
                savedBoard.getTitle(),
                savedBoard.getPosition(),
                savedBoard.getId(),
                savedBoard.getUsers().stream().map(User::getId).collect(Collectors.toList()),
                convertToColumnsDto(savedBoard.getColumns())
        );
    }

    public BoardDto getBoardByPosition(int position, String authHeader) {
        String username = extractUsernameFromToken(authHeader);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        User user = userOptional.get();

        Optional<Board> boardOptional = boardRepository.findByUsersContainingAndPosition(user, position);
        if (boardOptional.isPresent()) {
            Board board = boardOptional.get();
            return new BoardDto(
                    board.getId(),
                    board.getTitle(),
                    board.getPosition(),
                    board.getId(),
                    board.getUsers().stream().map(User::getId).collect(Collectors.toList()),
                    convertToColumnsDto(board.getColumns())
            );
        } else {
            throw new RuntimeException("Board with position " + position + " not found for user " + username);
        }
    }

    private List<MyColumnsDto> convertToColumnsDto(List<MyColumn> columns) {
        if (columns == null) {
            columns = new ArrayList<>();
        }
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

    public Board createDefaultBoard(User user) {
        Board defaultBoard = new Board();
        defaultBoard.setTitle("Board 1");
        defaultBoard.setPosition(1);
        defaultBoard.getUsers().add(user);
        return boardRepository.save(defaultBoard);
    }

    public BoardDto updateBoardTitle(Long boardId, String newTitle, String authHeader) {
        String username = extractUsernameFromToken(authHeader);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found."));
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new RuntimeException("Board not found."));

        if (!board.getUsers().contains(user)) {
            throw new RuntimeException("User is not authorized to modify this board.");
        }
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Board title cannot be empty");
        }
        board.setTitle(newTitle);
        Board updatedBoard = boardRepository.save(board);
        return new BoardDto(
                updatedBoard.getId(),
                updatedBoard.getTitle(),
                updatedBoard.getPosition(),
                updatedBoard.getId(),
                updatedBoard.getUsers().stream().map(User::getId).collect(Collectors.toList()),
                convertToColumnsDto(updatedBoard.getColumns())
        );
    }

    @Transactional
    public void deleteBoard(Long boardId, String authHeader) {
        String username = extractUsernameFromToken(authHeader);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found."));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found."));
        if (!board.getUsers().contains(user)) {
            throw new RuntimeException("User is not authorized to modify this board.");
        }
        if (board.getColumns() != null) {
            board.getColumns().forEach(column -> {
                if (column.getTasks() != null) {
                    column.getTasks().clear();
                }
            });
            board.getColumns().clear();
        }
        boardRepository.delete(board);
    }
}
