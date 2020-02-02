package br.com.brunoscatena.libraryapi.service;

import br.com.brunoscatena.libraryapi.exception.BusinessException;
import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.model.repository.BookRepository;
import br.com.brunoscatena.libraryapi.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.bookService = new BookServiceImpl(repository);
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

        when(repository.existsByIsbn(anyString())).thenReturn(false);

        when(repository.save(book)).thenReturn(
                Book.builder()
                        .id(1L)
                        .author("Bruno")
                        .title("As aventuras")
                        .isbn("1234")
                        .build()
        );

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
        when(repository.existsByIsbn(anyString())).thenReturn(true);

        // execução
        assertThrows(BusinessException.class, () -> {
            bookService.save(book);
        });

        // verificação
        // assertThat(exception)
        //     .isInstanceOf(BusinessException.class)
        //     .hasMessage("ISBN already being used");

    }

    @Test
    @DisplayName("Should find book by id")
    public void findByIdTest() {
        Long id = 1L;

        Book bookMock = createValidBook();
        bookMock.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(bookMock));

        Optional<Book> foundBook = bookService.findById(id);

        verify(repository, times(1)).findById(id);
        verifyNoMoreInteractions(repository);

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
        when(repository.findById(id)).thenReturn(Optional.empty());
        Optional<Book> returnedBook = bookService.findById(id);
        assertThat(returnedBook.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should delete book")
    public void bookDeleteTest() {

        Book validBook = createValidBookWithId();

        doNothing().when(repository).delete(validBook);

        assertDoesNotThrow(() -> bookService.delete(validBook));

        verify(repository, times(1)).delete(validBook);
        verifyNoMoreInteractions(repository);

    }

    @Test
    @DisplayName("Should throw exception when deleting invalid book")
    public void deleteInvalidBookTest() {

        Book bookWithoutId = createValidBook();

        doNothing().when(repository).delete(any(Book.class));

        assertThrows(IllegalArgumentException.class, () -> {
            bookService.delete(bookWithoutId);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            bookService.delete(null);
        });

        verify(repository, never()).delete(any(Book.class));
    }

    @Test
    @DisplayName("Should update book")
    public void bookUpdateTest() {
        Book book = createValidBookWithId();
        when(repository.save(book)).thenReturn(book);

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

        verify(repository, never()).save(book);

    }
}
