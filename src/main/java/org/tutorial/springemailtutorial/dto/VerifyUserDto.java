package org.tutorial.springemailtutorial.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyUserDto {

    private String email;
    private String verificationCode;

}