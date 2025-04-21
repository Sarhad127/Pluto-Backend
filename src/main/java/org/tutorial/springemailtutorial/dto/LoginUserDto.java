package org.tutorial.springemailtutorial.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDto {

    private String email;
    private String password;

}