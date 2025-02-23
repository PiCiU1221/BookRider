package edu.zut.bookrider.exception;

public class LibraryCardNotFoundException extends RuntimeException {
    public LibraryCardNotFoundException(String message) {
        super(message);
    }
}
