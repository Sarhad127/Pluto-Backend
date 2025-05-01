package org.tutorial.springemailtutorial.customExceptions;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
