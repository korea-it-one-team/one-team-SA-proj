package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.BoardRepository;
import com.lyj.proj.oneteamsaproj.vo.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public Board getBoardById(int boardId) {
        return boardRepository.getBoardById(boardId);
    }

}