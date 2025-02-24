package edu.zut.bookrider.exception;

public class RentalReturnNotFoundException extends RuntimeException {
    public RentalReturnNotFoundException(String message) {
        super(message);
    }
}
