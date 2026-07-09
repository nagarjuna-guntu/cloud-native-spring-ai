package com.bookshop.catalog.domain;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends ListCrudRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);
    List<Book> findAllByIsbnIn(List<String> isbns);
}
