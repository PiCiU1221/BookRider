package edu.zut.bookrider.exception;

import edu.zut.bookrider.dto.ApiErrorResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleConstraintViolationExceptions(ConstraintViolationException ex) {
        List<String> errorMessages = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(400, String.join(", ", errorMessages));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleMissingParams(MissingServletRequestParameterException ex) {
        String errorMessage = "Required request parameter '" + ex.getParameterName() + "' is missing.";
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(400, errorMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTransportProfileException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidTransportProfile(InvalidTransportProfileException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(400, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCoordinatesException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidCoordinatesException(InvalidCoordinatesException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(400, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoRouteFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleNoRouteFoundException(NoRouteFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(404, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleExternalApiException(ExternalApiException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(503, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleBookNotFoundException(BookNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(404, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LibraryNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleLibraryNotFoundException(LibraryNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(404, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleCategoryNotFoundException(CategoryNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(404, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PublisherNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handlePublisherNotFoundException(PublisherNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(404, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LanguageNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleLanguageNotFoundException(LanguageNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(404, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleBadCredentialsExceptions(BadCredentialsException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(401, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAuthorizationDeniedExceptions(AuthorizationDeniedException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(401, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(400, errors.toString());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        String message = "Required part '" + ex.getRequestPartName() + "' is not present";
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(400,  message);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(400,  ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleOrderNotFoundException(OrderNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(404,  ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DriverApplicationNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleDriverApplicationNotFoundException(DriverApplicationNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(404,  ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(404,  ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PasswordNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handlePasswordNotValidException(PasswordNotValidException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(400,  ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingAddressException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleMissingAddressException(MissingAddressException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(422,  ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(400,  ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleEmptyCartException(EmptyCartException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(400,  ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleIllegalStateException(IllegalStateException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(409,  ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDTO> handleGenericExceptions(Exception ex) {
        // for testing only, remove in prod
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(500,  "An unexpected error occurred: " + ex + " " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
