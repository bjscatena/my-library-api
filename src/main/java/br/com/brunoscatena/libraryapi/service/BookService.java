package br.com.brunoscatena.libraryapi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.brunoscatena.libraryapi.model.entity.Book;

public interface BookService {
    Book save(Book any);

    Optional<Book> findById(Long id);

    void delete(Book book);

    Book update(Book book);

    Page<Book> find(Book book, Pageable pageRequest);

    Optional<Book> findByIsbn(String isbn);
}
