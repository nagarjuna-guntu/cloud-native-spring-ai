package com.bookshop.catalog.domain;

import lombok.Getter;
import org.springframework.validation.MapBindingResult;

@Getter
public class MapFieldValidationException extends RuntimeException {
    private final MapBindingResult mapBindingResult;

    public MapFieldValidationException(String message, MapBindingResult mapBindingResult) {
        super(message);
        this.mapBindingResult = mapBindingResult;
    }
}
