package br.com.brunoscatena.libraryapi.service.impl;

import br.com.brunoscatena.libraryapi.exception.BusinessException;
import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.model.repository.BookRepository;
import br.com.brunoscatena.libraryapi.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if (repository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("ISBN already being used");
        }
        return repository.save(book);
    }

    @Override
    public Optional<Book> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if (book == null || book.getId() == null) {
            throw new IllegalArgumentException("Can't delete book with null id");
        }

        repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if (book == null || book.getId() == null) {
            throw new IllegalArgumentException("Can't update book with null id");
        }

        return repository.save(book);
    }

    @Override
    public Page<Book> find(Book book, Pageable pageRequest) {
        return null;
    }
}
