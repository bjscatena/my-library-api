package br.com.brunoscatena.libraryapi.api.resource;

import br.com.brunoscatena.libraryapi.api.dto.BookDTO;
import br.com.brunoscatena.libraryapi.api.exceptions.ApiError;
import br.com.brunoscatena.libraryapi.exception.BusinessException;
import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;


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
        return bookService
                .findById(id)
                .map( entity -> modelMapper.map(entity, BookDTO.class) )
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
        Book book = modelMapper.map(dto, Book.class);
        Page<Book> pageBook = bookService.find(book, pageRequest);

        List<BookDTO> dtoList = pageBook.getContent()
                                    .stream()
                                    .map(entity -> modelMapper.map(entity, BookDTO.class) )
                                    .collect(Collectors.toList());

        Page<BookDTO> dtoPage = new PageImpl<BookDTO>( dtoList, pageRequest, pageBook.getTotalElements() );

        System.out.println(dtoPage);

        return dtoPage;
    }

    @PutMapping("/{id}")
    public BookDTO update(@PathVariable Long id, @RequestBody BookDTO dto) {
        Book book = bookService.findById(id)
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );

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
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
        bookService.delete(book);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        return new ApiError(bindingResult);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerBusinessExceptions(BusinessException ex) {
        return new ApiError(ex);
    }
}
