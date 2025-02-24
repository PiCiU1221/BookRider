package edu.zut.bookrider.exception;

public class MissingAddressException extends RuntimeException {
    public MissingAddressException(String message) {
        super(message);
    }
}
