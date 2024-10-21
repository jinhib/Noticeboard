package com.sparta.board.entity.enumSet;

import lombok.Getter;

@Getter
public enum ErrorType {
    NOT_VALID_TOKEN(400, "Token is not valid"),
    NOT_WRITER(400, "Only user can edit/remove the post."),
    DUPLICATED_USERNAME(400, "Duplicated username."),
    NOT_MATCHING_INFO(400, "Cannot find username."),
    NOT_MATCHING_PASSWORD(400, "Password is not matched."),
    NOT_FOUND_USER(400, "Username is not existed."),
    NOT_FOUND_WRITING(400, "Post/comment is not existed.");

    private int code;
    private String message;

    ErrorType(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
