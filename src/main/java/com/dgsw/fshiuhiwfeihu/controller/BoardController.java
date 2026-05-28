package com.dgsw.fshiuhiwfeihu.controller;

import com.dgsw.fshiuhiwfeihu.entity.Board;
import com.dgsw.fshiuhiwfeihu.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
class BoardController {
    private final BoardRepository boardRepository;

    @GetMapping("/boards")
    public ResponseEntity<List<Board>> getBoards() {
        Board board = new Board("asdasdsa", "content");
        boardRepository.save(board);
        return ResponseEntity.ok(boardRepository.findAll());
    }
}
