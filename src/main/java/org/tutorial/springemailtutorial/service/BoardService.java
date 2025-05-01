package org.tutorial.springemailtutorial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.BoardRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    public List<Board> getBoards(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        List<Board> boards = boardRepository.findByUser(userOptional.get());

        if (boards.isEmpty()) {
            return List.of(createDefaultBoard(userOptional.get()));
        }

        return boards;
    }

    public Board createDefaultBoard(User user) {

        Board defaultBoard = new Board();
        defaultBoard.setTitle("Default Board");
        defaultBoard.setPosition(1);
        defaultBoard.setUser(user);

        return boardRepository.save(defaultBoard);
    }

    public Board createBoard(Board board, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
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
        return boardRepository.save(board);
    }

    public Board getBoardByPosition(int position, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        User user = userOptional.get();
        Optional<Board> boardOptional = boardRepository.findByUserAndPosition(user, position);
        if (boardOptional.isPresent()) {
            return boardOptional.get();
        } else {
            throw new RuntimeException("Board with position " + position + " not found for user " + username);
        }
    }
}
