package br.com.brunoscatena.libraryapi.service;

import br.com.brunoscatena.libraryapi.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {
    Book save(Book any);
    Optional<Book> findById(Long id);
    void delete(Book book);
    Book update(Book book);
    Page<Book> find(Book book, Pageable pageRequest);
}
