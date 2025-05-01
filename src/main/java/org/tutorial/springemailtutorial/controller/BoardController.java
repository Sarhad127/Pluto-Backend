package org.tutorial.springemailtutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.MyColumnsDto;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.MyColumn;
import org.tutorial.springemailtutorial.service.BoardService;
import org.tutorial.springemailtutorial.service.myColumnsService;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    @Autowired
    private BoardService boardService;
    @Autowired
    private myColumnsService columnsService;

    @PostMapping("/boards/{boardId}/columns")
    public ResponseEntity<MyColumn> createColumn(@PathVariable Long boardId,
                                                 @RequestBody MyColumnsDto columnDto,
                                                 @RequestHeader("Authorization") String authHeader) {
        MyColumn column = columnsService.saveColumn(columnDto, boardId, authHeader);
        return new ResponseEntity<>(column, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Board>> getBoards(@RequestHeader("Authorization") String authHeader) {
        List<Board> boards = boardService.getBoards(authHeader);
        return ResponseEntity.ok(boards);
    }
}
