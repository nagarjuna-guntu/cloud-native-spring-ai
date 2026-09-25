package com.bookshop.catalog.web;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class IsbnValidator {

    public boolean isIsbnCandidate(String isbn) {

        var normalizeISBN = isbn.trim();
        var isbnLength = normalizeISBN.length();

        if (!StringUtils.hasText(normalizeISBN)) {
            return false;
        }

        return normalizeISBN.chars().allMatch(Character::isDigit) &&
                (isbnLength == 10 || isbnLength == 13);
    }
}
