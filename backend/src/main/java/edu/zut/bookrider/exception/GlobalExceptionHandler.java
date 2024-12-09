package edu.zut.bookrider.exception;

import edu.zut.bookrider.dto.ErrorApiResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorApiResponseDTO> handleConstraintViolationExceptions(ConstraintViolationException ex) {
        List<String> errorMessages = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ErrorApiResponseDTO errorResponse = new ErrorApiResponseDTO(400, String.join(", ", errorMessages));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorApiResponseDTO> handleMissingParams(MissingServletRequestParameterException ex) {
        String errorMessage = "Required request parameter '" + ex.getParameterName() + "' is missing.";
        ErrorApiResponseDTO errorResponse = new ErrorApiResponseDTO(400, errorMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTransportProfileException.class)
    public ResponseEntity<ErrorApiResponseDTO> handleInvalidTransportProfile(InvalidTransportProfileException ex) {
        ErrorApiResponseDTO errorResponse = new ErrorApiResponseDTO(400, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCoordinatesException.class)
    public ResponseEntity<ErrorApiResponseDTO> handleInvalidCoordinatesException(InvalidCoordinatesException ex) {
        ErrorApiResponseDTO errorResponse = new ErrorApiResponseDTO(400, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoRouteFoundException.class)
    public ResponseEntity<ErrorApiResponseDTO> handleNoRouteFoundException(NoRouteFoundException ex) {
        ErrorApiResponseDTO errorResponse = new ErrorApiResponseDTO(400, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorApiResponseDTO> handleExternalApiException(ExternalApiException ex) {
        ErrorApiResponseDTO errorResponse = new ErrorApiResponseDTO(400, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorApiResponseDTO> handleGenericExceptions(BadCredentialsException ex) {
        ErrorApiResponseDTO errorResponse = new ErrorApiResponseDTO(401,  ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApiResponseDTO> handleGenericExceptions(Exception ex) {
        ErrorApiResponseDTO errorResponse = new ErrorApiResponseDTO(500,  "An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
