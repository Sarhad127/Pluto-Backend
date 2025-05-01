package org.tutorial.springemailtutorial.responses;

import lombok.*;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private long expiresIn;

}