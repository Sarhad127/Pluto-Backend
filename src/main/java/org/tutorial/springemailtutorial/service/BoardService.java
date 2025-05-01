package org.tutorial.springemailtutorial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.BoardDto;
import org.tutorial.springemailtutorial.dto.MyColumnsDto;
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

    private String extractUsernameFromToken(String authHeader) {
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
        List<Board> boards = boardRepository.findByUser(user);
        return boards.stream()
                .map(board -> new BoardDto(board.getId(), board.getTitle(), board.getPosition(), user.getId(), convertToColumnsDto(board.getColumns())))
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
        List<Board> userBoards = boardRepository.findByUser(user);
        int maxPosition = userBoards.stream()
                .mapToInt(Board::getPosition)
                .max()
                .orElse(0);
        board.setPosition(maxPosition + 1);
        board.setUser(user);
        Board savedBoard = boardRepository.save(board);
        return new BoardDto(savedBoard.getId(), savedBoard.getTitle(), savedBoard.getPosition(), user.getId(), convertToColumnsDto(savedBoard.getColumns()));
    }

    public BoardDto getBoardByPosition(int position, String authHeader) {
        String username = extractUsernameFromToken(authHeader);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        User user = userOptional.get();

        Optional<Board> boardOptional = boardRepository.findByUserAndPosition(user, position);
        if (boardOptional.isPresent()) {
            Board board = boardOptional.get();
            return new BoardDto(board.getId(), board.getTitle(), board.getPosition(), user.getId(), convertToColumnsDto(board.getColumns()));
        } else {
            throw new RuntimeException("Board with position " + position + " not found for user " + username);
        }
    }

    private List<MyColumnsDto> convertToColumnsDto(List<MyColumn> columns) {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        return columns.stream()
                .map(column -> new MyColumnsDto(column.getId(), column.getTitle(), column.getPlacement(), column.getTitleColor()))
                .collect(Collectors.toList());
    }

    public Board createDefaultBoard(User user) {
        Board defaultBoard = new Board();
        defaultBoard.setTitle("Default Board");
        defaultBoard.setPosition(1);
        defaultBoard.setUser(user);
        return boardRepository.save(defaultBoard);
    }
}
