package com.bookshop.catalog.domain;

import com.bookshop.catalog.web.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class DocumentMapper {

    public Document toDocument(BookResponse bookResponse) {
        log.debug("Mapping BookResponse to Document for ISBN: {}", bookResponse.isbn());

        // A. Generate a Stable UUID from the ISBN
        // This ensures that "ISBN-123" ALWAYS equals UUID "abc-123..."
        // allowing us to overwrite the old vector when the book updates.
        String stableId = UUID.nameUUIDFromBytes(bookResponse.isbn().getBytes(StandardCharsets.UTF_8)).toString();
        log.debug("Generated stable ID for the document: {}", stableId);

        // B. Construct the Context
        String content = """
                Title: %s.
                Author: %s.
                Publisher: %s.
                Price: %.2f.
                """.formatted(
                bookResponse.title(),
                bookResponse.author(),
                bookResponse.publisher(),
                bookResponse.price()
        );

        // C. Metadata (Store ISBN for reverse lookup)
        Map<String, Object> metadata = Map.of(
                "isbn", bookResponse.isbn(),
                "price", bookResponse.price(),
                "title", bookResponse.title().toLowerCase(),
                "author", bookResponse.author().toLowerCase(),
                "publisher", bookResponse.publisher().toLowerCase()
        );

        // D. Create Document with the STABLE ID, content, and metadata
        return Document.builder()
                .id(stableId)
                .text(content)
                .metadata(metadata)
                .build();
    }
}
