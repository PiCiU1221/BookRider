package edu.zut.bookrider.exception;

public class LibraryRequestNotFoundException extends RuntimeException {
  public LibraryRequestNotFoundException(String message) {
    super(message);
  }
}
