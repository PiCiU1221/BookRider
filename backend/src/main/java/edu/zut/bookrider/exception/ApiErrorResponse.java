package edu.zut.bookrider.exception;

public class ApiErrorResponse {
    private String errorCode;
    private String message;

    public ApiErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

}

