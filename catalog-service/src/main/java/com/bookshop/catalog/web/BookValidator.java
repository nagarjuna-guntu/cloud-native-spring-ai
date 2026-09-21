package com.bookshop.catalog.web;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Map;

@Component
public class BookValidator implements Validator {

    private static void verifyFieldErrors(Errors errors, String key, Object value) {
        switch (value) {
            case String s when s.isBlank() ->
                    errors.rejectValue(key, "field.blank", "The " + key + " value should not be empty");
            case Double d when d <= 0.0 -> errors.rejectValue(key, "field.positive", "The price must be positive");
            case null -> errors.rejectValue(key, "field.null", "The " + key + " value should not be null");
            case Object _ -> {
            }
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Map<String, Object> updates = (Map<String, Object>) target;
        if (updates.isEmpty()) {
            errors.reject("map.empty", "The update request must contain at least one field.");
            return; // Stop further validation since there are no keys to check
        }
        updates.forEach((key, value) -> verifyFieldErrors(errors, key, value));
    }
}
