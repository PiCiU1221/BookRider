package edu.zut.bookrider.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message){
        super(message);
    }
}
