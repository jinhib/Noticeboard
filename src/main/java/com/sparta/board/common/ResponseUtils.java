package com.sparta.board.common;

public class ResponseUtils {

    // if request is succeed
    public static <T> ApiResponseDto<T> ok(T response) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .response(response)
                .build();
    }

    // if error occurs
    public static <T> ApiResponseDto<T> error(ErrorResponse response) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .error(response)
                .build();
    }

}
