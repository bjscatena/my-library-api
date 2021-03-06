package br.com.brunoscatena.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.brunoscatena.libraryapi.exception.BusinessException;
import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.model.repository.BookRepository;
import br.com.brunoscatena.libraryapi.service.impl.BookServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
	this.bookService = new BookServiceImpl(bookRepository);
    }

    private Book createValidBook() {
	return Book.builder().author("Bruno").title("As aventuras").isbn("1234").build();
    }

    private Book createValidBookWithId() {
	return Book.builder().id(1L).author("Bruno").title("As aventuras").isbn("1234").build();
    }

    @Test
    @DisplayName("Should save book")
    public void saveBookTest() {

	// cenário
	Book book = createValidBook();

	when(bookRepository.existsByIsbn(anyString())).thenReturn(false);

	when(bookRepository.save(book)).thenReturn(
		Book.builder().id(1L).author("Bruno").title("As aventuras").isbn("1234").build());

	// execução
	Book savedBook = bookService.save(book);

	// verificação
	assertThat(savedBook.getId()).isNotNull();
	assertThat(savedBook.getAuthor()).isEqualTo("Bruno");
	assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
	assertThat(savedBook.getIsbn()).isEqualTo("1234");

    }

    @Test
    @DisplayName("Should throw error when trying to save book with ISBN already being used")
    public void saveBookWithDuplicatedIsbn() {

	// cenário
	Book book = createValidBook();
	when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

	// execução
	assertThrows(BusinessException.class, () -> {
	    bookService.save(book);
	});

	// verificação
	// assertThat(exception)
	// .isInstanceOf(BusinessException.class)
	// .hasMessage("ISBN already being used");

    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should find book using properties")
    public void find() {

	// Arrange
	Book book = createValidBook();

	PageRequest pageReq = PageRequest.of(0, 10);
	List<Book> books = Arrays.asList(book);

	Page<Book> page = new PageImpl<Book>(books, pageReq, 1);

	when(bookRepository.findAll(any(Example.class), any(PageRequest.class))).thenReturn(page);

	// Act
	Page<Book> result = bookService.find(book, pageReq);

	// Assert
	assertThat(result.getTotalElements()).isEqualTo(1);
	assertThat(result.getContent()).isEqualTo(books);
	assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
	assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    @Test
    @DisplayName("Should find book by id")
    public void findByIdTest() {
	Long id = 1L;

	Book bookMock = createValidBook();
	bookMock.setId(id);

	when(bookRepository.findById(id)).thenReturn(Optional.of(bookMock));

	Optional<Book> foundBook = bookService.findById(id);

	verify(bookRepository, times(1)).findById(id);
	verifyNoMoreInteractions(bookRepository);

	assertThat(foundBook.isPresent()).isTrue();
	assertThat(foundBook.get().getId()).isEqualTo(bookMock.getId());
	assertThat(foundBook.get().getTitle()).isEqualTo(bookMock.getTitle());
	assertThat(foundBook.get().getAuthor()).isEqualTo(bookMock.getAuthor());
	assertThat(foundBook.get().getIsbn()).isEqualTo(bookMock.getIsbn());

    }

    @Test
    @DisplayName("Should return empty when book does not exists on database")
    public void findInexistentByIdTest() {
	Long id = 1L;
	when(bookRepository.findById(id)).thenReturn(Optional.empty());
	Optional<Book> returnedBook = bookService.findById(id);
	assertThat(returnedBook.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should delete book")
    public void bookDeleteTest() {

	Book validBook = createValidBookWithId();

	doNothing().when(bookRepository).delete(validBook);

	assertDoesNotThrow(() -> bookService.delete(validBook));

	verify(bookRepository, times(1)).delete(validBook);
	verifyNoMoreInteractions(bookRepository);

    }

    @Test
    @DisplayName("Should throw exception when deleting invalid book")
    public void deleteInvalidBookTest() {

	Book bookWithoutId = createValidBook();

	doNothing().when(bookRepository).delete(any(Book.class));

	assertThrows(IllegalArgumentException.class, () -> {
	    bookService.delete(bookWithoutId);
	});

	assertThrows(IllegalArgumentException.class, () -> {
	    bookService.delete(null);
	});

	verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    @DisplayName("Should update book")
    public void bookUpdateTest() {
	Book book = createValidBookWithId();
	when(bookRepository.save(book)).thenReturn(book);

	Book updatedBook = bookService.update(book);

	assertThat(updatedBook.getId()).isEqualTo(book.getId());
	assertThat(updatedBook.getAuthor()).isEqualTo(book.getAuthor());
	assertThat(updatedBook.getTitle()).isEqualTo(book.getTitle());
	assertThat(updatedBook.getIsbn()).isEqualTo(book.getIsbn());

    }

    @Test
    @DisplayName("Should throw exception when updating invalid book")
    public void updateInvalidBookTest() {

	Book book = createValidBook();

	assertThrows(IllegalArgumentException.class, () -> {
	    bookService.update(book);
	});

	assertThrows(IllegalArgumentException.class, () -> {
	    bookService.update(null);
	});

	verify(bookRepository, never()).save(book);

    }

    @Test
    @DisplayName("Should find book by ISBN")
    public void findByIsbnTest() {

	// Arrange
	String isbn = "123";

	Book mockBook = createValidBookWithId();
	when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(mockBook));

	// Act
	Optional<Book> optBook = bookService.findByIsbn(isbn);

	// Assert
	assertTrue(optBook.isPresent());

	Book foundBook = optBook.get();

	assertEquals(foundBook.getId(), mockBook.getId());
	assertEquals(foundBook.getAuthor(), mockBook.getAuthor());
	assertEquals(foundBook.getTitle(), mockBook.getTitle());
	assertEquals(foundBook.getIsbn(), mockBook.getIsbn());

	verify(bookRepository, times(1)).findByIsbn(isbn);

    }
}
