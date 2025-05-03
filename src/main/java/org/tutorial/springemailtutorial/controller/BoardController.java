package org.tutorial.springemailtutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.BoardDto;
import org.tutorial.springemailtutorial.dto.MyColumnsDto;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.MyColumn;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.UserRepository;
import org.tutorial.springemailtutorial.service.BoardService;
import org.tutorial.springemailtutorial.service.myColumnsService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private myColumnsService columnsService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{boardId}/columns")
    public ResponseEntity<MyColumn> createColumn(@PathVariable Long boardId,
                                                 @RequestBody MyColumnsDto columnDto,
                                                 @RequestHeader("Authorization") String authHeader) {
        MyColumn column = columnsService.saveColumn(columnDto, boardId, authHeader);
        return new ResponseEntity<>(column, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BoardDto>> getBoards(@RequestHeader("Authorization") String authHeader) {
        List<BoardDto> boards = boardService.getBoards(authHeader);
        return ResponseEntity.ok(boards);
    }

    @PostMapping
    public ResponseEntity<BoardDto> createBoard(@RequestBody Board board,
                                                @RequestHeader("Authorization") String authHeader) {
        String username = boardService.extractUsernameFromToken(authHeader);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = userOptional.get();
        board.getUsers().add(user);
        BoardDto createdBoard = boardService.createBoard(board, authHeader);
        return new ResponseEntity<>(createdBoard, HttpStatus.CREATED);
    }

    @GetMapping("/{position}")
    public ResponseEntity<BoardDto> getBoardByPosition(@PathVariable int position,
                                                       @RequestHeader("Authorization") String authHeader) {
        try {
            BoardDto board = boardService.getBoardByPosition(position, authHeader);
            List<MyColumnsDto> columns = columnsService.getColumnsForBoard(board.getId());
            board.setColumns(columns);
            return ResponseEntity.ok(board);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{boardId}/title")
    public ResponseEntity<BoardDto> updateBoardTitle(@PathVariable Long boardId,
                                                     @RequestBody Map<String, String> requestBody,
                                                     @RequestHeader("Authorization") String authHeader) {
        String newTitle = requestBody.get("title");
        BoardDto updatedBoard = boardService.updateBoardTitle(boardId, newTitle, authHeader);
        return ResponseEntity.ok(updatedBoard);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId,
                                            @RequestHeader("Authorization") String authHeader) {
        boardService.deleteBoard(boardId, authHeader);
        return ResponseEntity.noContent().build();
    }
}
