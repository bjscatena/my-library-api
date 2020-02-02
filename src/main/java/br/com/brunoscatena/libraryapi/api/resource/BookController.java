package br.com.brunoscatena.libraryapi.api.resource;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.brunoscatena.libraryapi.api.dto.BookDTO;
import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.service.BookService;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService bookService;
    private ModelMapper modelMapper;

    public BookController(BookService bookService, ModelMapper modelMapper) {
	this.bookService = bookService;
	this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
	Book entity = modelMapper.map(dto, Book.class);
	entity = bookService.save(entity);
	return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("/{id}")
    public BookDTO get(@PathVariable Long id) {
	return bookService.findById(id)
		.map(entity -> modelMapper.map(entity, BookDTO.class))
		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {

	Book book = modelMapper.map(dto, Book.class);

	Page<Book> pageBook = bookService.find(book, pageRequest);

	List<BookDTO> dtoList = pageBook.getContent()
		.stream()
		.map(entity -> modelMapper.map(entity, BookDTO.class))
		.collect(Collectors.toList());

	return new PageImpl<BookDTO>(dtoList, pageRequest, pageBook.getTotalElements());
    }

    @PutMapping("/{id}")
    public BookDTO update(@PathVariable Long id, @RequestBody BookDTO dto) {

	Book book = bookService.findById(id)
		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

	book.setAuthor(dto.getAuthor());
	book.setIsbn(dto.getIsbn());
	book.setTitle(dto.getTitle());

	Book editedBook = bookService.update(book);

	return modelMapper.map(editedBook, BookDTO.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

	Book book = bookService.findById(id)
		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

	bookService.delete(book);
    }

}
