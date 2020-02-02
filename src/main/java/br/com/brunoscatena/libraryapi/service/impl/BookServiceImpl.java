package br.com.brunoscatena.libraryapi.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.brunoscatena.libraryapi.exception.BusinessException;
import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.model.repository.BookRepository;
import br.com.brunoscatena.libraryapi.service.BookService;

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
    public Page<Book> find(Book filter, Pageable pageRequest) {

	Example<Book> bookEx = Example.of(filter,
		ExampleMatcher.matching()
			.withIgnoreCase()
			.withIgnoreNullValues()
			.withStringMatcher(StringMatcher.CONTAINING));

	return repository.findAll(bookEx, pageRequest);
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
	return repository.findByIsbn(isbn);
    }
}
