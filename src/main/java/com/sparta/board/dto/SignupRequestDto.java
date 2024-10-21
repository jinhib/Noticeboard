package com.sparta.board.dto;

import lombok.Getter;

import javax.validation.constraints.Pattern;

@Getter
public class SignupRequestDto {

    // Check username requirements
    @Pattern(regexp = "^[a-z0-9]{4,10}$", message = "Must be 4-10 characters(a-z 0-9)")
    private String username;

    // Check password requirements
    @Pattern(regexp = "^[a-zA-Z0-9~!@#$%^&*()_+=?,./<>{}\\[\\]\\-]{8,15}$", message = " Must be 8-15 characters(a-z A-Z 0-9) and include at least one symbol")
    private String password;

    private Boolean admin;

}
