package org.tutorial.springemailtutorial.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserDto {

    private String email;
    private String password;
    private String username;

}