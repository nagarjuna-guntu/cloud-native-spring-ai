--changeset nguntu:add-unique-constraint-on-book-isbn

ALTER TABLE book
    ADD CONSTRAINT uk_book_isbn UNIQUE (isbn);