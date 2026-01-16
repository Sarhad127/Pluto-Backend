package org.tutorial.springemailtutorial.dto;

import lombok.Data;

@Data
public class VerifyUserByUsernameDto {
    private String username;
    private String verificationCode;
}
