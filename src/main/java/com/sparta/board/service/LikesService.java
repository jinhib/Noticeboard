package com.sparta.board.service;

import com.sparta.board.common.ApiResponseDto;
import com.sparta.board.common.ResponseUtils;
import com.sparta.board.dto.BoardResponseDto;
import com.sparta.board.dto.CommentResponseDto;
import com.sparta.board.entity.Board;
import com.sparta.board.entity.Comment;
import com.sparta.board.entity.Likes;
import com.sparta.board.entity.User;
import com.sparta.board.entity.enumSet.ErrorType;
import com.sparta.board.exception.RestApiException;
import com.sparta.board.repository.BoardRepository;
import com.sparta.board.repository.CommentRepository;
import com.sparta.board.repository.LikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikesService {

    private final LikesRepository likesRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    // LIKES posting
     public ApiResponseDto<BoardResponseDto> likePost(Long id, User user) {
        // check in the DB
        Optional<Board> board = boardRepository.findById(id);
        if (board.isEmpty()) {
            throw new RestApiException(ErrorType.NOT_FOUND_WRITING);
        }

        // check if it's already liked
        Optional<Likes> found = likesRepository.findByBoardAndUser(board.get(), user);
        if (found.isEmpty()) {  // if it's not likes
            Likes likes = Likes.of(board.get(), user);
            likesRepository.save(likes);
        } else { // if it's likes already
            likesRepository.delete(found.get()); // remove likes
            likesRepository.flush();
        }

        return ResponseUtils.ok(BoardResponseDto.from(board.get()));
    }

    // LIKES comments
    public ApiResponseDto<CommentResponseDto> likeComment(Long id, User user) {
        // check in the DB
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isEmpty()) {
            throw new RestApiException(ErrorType.NOT_FOUND_WRITING);
        }

        // check if it's already liked
        Optional<Likes> found = likesRepository.findByCommentAndUser(comment.get(), user);
        if (found.isEmpty()) {  // if it's not likes
            Likes likes = Likes.of(comment.get(), user);
            likesRepository.save(likes);
        } else { // if it's likes already
            likesRepository.delete(found.get()); // remove likes
            likesRepository.flush();
        }

        return ResponseUtils.ok(CommentResponseDto.from(comment.get()));
    }
}
