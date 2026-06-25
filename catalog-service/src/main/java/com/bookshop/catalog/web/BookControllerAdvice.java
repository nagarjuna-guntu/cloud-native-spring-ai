package com.bookshop.catalog.web;

import com.bookshop.bookcatalogservice.domain.BookAlreadyExistsException;
import com.bookshop.bookcatalogservice.domain.BookNotFoundException;
import com.bookshop.bookcatalogservice.domain.MapFieldValidationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class BookControllerAdvice {

    @ExceptionHandler(BookNotFoundException.class)
    public ProblemDetail handleBookNotFoundException(BookNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, e.getMessage()
        );
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    public ProblemDetail handleBookAlreadyExistsException(BookAlreadyExistsException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage()
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(ValidationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation Errors");
        problemDetail.setProperty("fieldErrors", e.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(MapFieldValidationException.class)
    public ProblemDetail handleMapFieldValidationException(MapFieldValidationException e) {
        var errorDetails = e.getMapBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(FieldError::getField,
                        Collectors.filtering(fieldError -> Objects.nonNull(fieldError.getDefaultMessage()),
                                Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList()))));
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setProperty("fieldErrors", errorDetails);
        return problemDetail;

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var errorDetails = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(FieldError::getField,
                        Collectors.filtering(fieldError -> Objects.nonNull(fieldError.getDefaultMessage()),
                                Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList()))));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation Errors");
        problemDetail.setProperty("fieldErrors", errorDetails);
        return problemDetail;

    }
}
