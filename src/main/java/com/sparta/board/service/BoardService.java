package com.sparta.board.service;

import com.sparta.board.common.ApiResponseDto;
import com.sparta.board.common.ResponseUtils;
import com.sparta.board.common.SuccessResponse;
import com.sparta.board.dto.BoardRequestsDto;
import com.sparta.board.dto.BoardResponseDto;
import com.sparta.board.dto.CommentResponseDto;
import com.sparta.board.entity.Board;
import com.sparta.board.entity.Comment;
import com.sparta.board.entity.User;
import com.sparta.board.entity.enumSet.ErrorType;
import com.sparta.board.entity.enumSet.UserRoleEnum;
import com.sparta.board.exception.RestApiException;
import com.sparta.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    // View all postings
    @Transactional(readOnly = true)
    public ApiResponseDto<List<BoardResponseDto>> getPosts() {

        List<Board> boardList = boardRepository.findAllByOrderByModifiedAtDesc();
        List<BoardResponseDto> responseDtoList = new ArrayList<>();

        for (Board board : boardList) {
            // Order comments by date
            board.getCommentList()
                    .sort(Comparator.comparing(Comment::getModifiedAt)
                            .reversed());

            List<CommentResponseDto> commentList = new ArrayList<>();
            for (Comment comment : board.getCommentList()) {
                if (comment.getParentCommentId() == null) {
                    commentList.add(CommentResponseDto.from(comment));
                }
            }

            responseDtoList.add(BoardResponseDto.from(board, commentList));
        }

        return ResponseUtils.ok(responseDtoList);

    }

    // Write posting
    @Transactional
    public ApiResponseDto<BoardResponseDto> createPost(BoardRequestsDto requestsDto, User user) {

        // Save posting
        Board board = boardRepository.save(Board.of(requestsDto, user));

        // conver to BoardResponseDto and return in responseEntity body
        return ResponseUtils.ok(BoardResponseDto.from(board));

    }

    // Retrieve posting
    @Transactional(readOnly = true)
    public ApiResponseDto<BoardResponseDto> getPost(Long id) {
        // check if there is posting matched with the id
        Optional<Board> board = boardRepository.findById(id);
        if (board.isEmpty()) { // if there is no postings in the search
            throw new RestApiException(ErrorType.NOT_FOUND_WRITING);
        }

        // Order comments by dates
        board.get()
                .getCommentList()
                .sort(Comparator.comparing(Comment::getModifiedAt)
                        .reversed());

        List<CommentResponseDto> commentList = new ArrayList<>();
        for (Comment comment : board.get().getCommentList()) {
            if (comment.getParentCommentId() == null) {
                commentList.add(CommentResponseDto.from(comment));
            }
        }

        // board 를 responseDto 로 변환 후, ResponseEntity body 에 dto 담아 리턴
        return ResponseUtils.ok(BoardResponseDto.from(board.get(), commentList));
    }

    // Edit posting
    @Transactional
    public ApiResponseDto<BoardResponseDto> updatePost(Long id, BoardRequestsDto requestsDto, User user) {

        // check the posting is in the DB
        Optional<Board> board = boardRepository.findById(id);
        if (board.isEmpty()) {
            throw new RestApiException(ErrorType.NOT_FOUND_WRITING);
        }

        // check if the user is matched with writer
        // if the user is ADMIN, that user can edit
        Optional<Board> found = boardRepository.findByIdAndUser(id, user);
        if (found.isEmpty() && user.getRole() == UserRoleEnum.USER) { // if there is no matched
            throw new RestApiException(ErrorType.NOT_WRITER);
        }

        // edit posting if user info is matched with posting's id
        board.get().update(requestsDto, user);
        boardRepository.flush(); // use flush to give modifiedAt updates to responseDto

        return ResponseUtils.ok(BoardResponseDto.from(board.get()));

    }

    // Remove posting
    @Transactional
    public ApiResponseDto<SuccessResponse> deletePost(Long id, User user) {

        // check the posting is in the DB
        Optional<Board> found = boardRepository.findById(id);
        if (found.isEmpty()) {
            throw new RestApiException(ErrorType.NOT_FOUND_WRITING);
        }

        // check if the user is matched with writer
        // if the user is ADMIN, that user can remove
        Optional<Board> board = boardRepository.findByIdAndUser(id, user);
        if (board.isEmpty() && user.getRole() == UserRoleEnum.USER) { // if there is no matched
            throw new RestApiException(ErrorType.NOT_WRITER);
        }

        // remove posting if user info is matched with posting's id
        boardRepository.deleteById(id);
        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "Posting is removed"));

    }

}
