package com.sparta.board.service;

import com.sparta.board.common.ApiResponseDto;
import com.sparta.board.common.ResponseUtils;
import com.sparta.board.common.SuccessResponse;
import com.sparta.board.dto.CommentRequestDto;
import com.sparta.board.dto.CommentResponseDto;
import com.sparta.board.entity.Board;
import com.sparta.board.entity.Comment;
import com.sparta.board.entity.User;
import com.sparta.board.entity.enumSet.ErrorType;
import com.sparta.board.entity.enumSet.UserRoleEnum;
import com.sparta.board.exception.RestApiException;
import com.sparta.board.repository.BoardRepository;
import com.sparta.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    // Write comments
    @Transactional
    public ApiResponseDto<CommentResponseDto> createComment(Long id, CommentRequestDto requestDto, User user) {

        // Search in the DB
        Optional<Board> board = boardRepository.findById(id);
        if (board.isEmpty()) {
            throw new RestApiException(ErrorType.NOT_FOUND_WRITING);
        }

        Long parentCommentId = requestDto.getParentCommentId();
        Comment comment = Comment.of(requestDto, board.get(), user);
        if (parentCommentId == null) {  // if no parentComment
            commentRepository.save(comment);    // Save it
            return ResponseUtils.ok(CommentResponseDto.from(comment));
        }
        // if parentComment, add childComment to parent comment
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_WRITING));
        parentComment.addChildComment(comment); // add childComment to parentComment
        commentRepository.save(comment);

        return ResponseUtils.ok(CommentResponseDto.from(comment));

    }

    // Edit comment
    @Transactional
    public ApiResponseDto<CommentResponseDto> updateComment(Long id, CommentRequestDto requestDto, User user) {

        // Search in the DB
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isEmpty()) {
            throw new RestApiException(ErrorType.NOT_FOUND_WRITING);
        }

        // check if the user is matched with writer
        // if the user is ADMIN, that user can remove
        Optional<Comment> found = commentRepository.findByIdAndUser(id, user);
        if (found.isEmpty() && user.getRole() == UserRoleEnum.USER) {
            throw new RestApiException(ErrorType.NOT_WRITER);
        }

        // edit comments
        comment.get().update(requestDto, user);
        commentRepository.flush();   // use saveAndFlush to give modifiedAt update to responseDto

        // return dto with ResponseEntity
        return ResponseUtils.ok(CommentResponseDto.from(comment.get()));

    }

    // Remove comment
    @Transactional
    public ApiResponseDto<SuccessResponse> deleteComment(Long id, User user) {

        // Search in the DB
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isEmpty()) {
            throw new RestApiException(ErrorType.NOT_FOUND_WRITING);
        }

        // check if the user is matched with writer
        // if the user is ADMIN, that user can remove
        Optional<Comment> found = commentRepository.findByIdAndUser(id, user);
        if (found.isEmpty() && user.getRole() == UserRoleEnum.USER) {
            throw new RestApiException(ErrorType.NOT_WRITER);
        }

        // remove comment
        commentRepository.deleteById(id);

        // return ResponseEntity with status code and DTO with message
        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "Removed comment"));

    }

}
