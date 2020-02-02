package br.com.brunoscatena.libraryapi.model.repository;

import br.com.brunoscatena.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository bookRepository;


    private Book createBook() {
        return Book.builder().author("Bruno").title("As aventuras").isbn("1234").build();
    }

    @Test
    @DisplayName("Should return true when there is alreay a ISBN on database")
    public void returnTrueWhenIsbnExists() {

        // arrange
        String isbn = "1234";
        Book book = Book.builder().author("Some One").title("Adventures").isbn(isbn).build();
        entityManager.persistAndFlush(book);

        // act
        boolean exists = this.bookRepository.existsByIsbn(isbn);

        // assert
        assertThat(exists).isTrue();

    }

    @Test
    @DisplayName("Should return false when there is no ISBN on database")
    public void returnFalseWhenIsbnDoesNotExists() {
        // arrange
        String isbn = "1234";

        // act
        boolean exists = this.bookRepository.existsByIsbn(isbn);

        // assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find book by id")
    public void findByIdTest() {

        Book book = createBook();
        book = entityManager.persist(book);
        Optional<Book> foundBook = bookRepository.findById(book.getId());
        assertThat(foundBook.isPresent()).isTrue();

    }


    @Test
    @DisplayName("Should save book")
    public void saveTest() {
        Book savedBook = bookRepository.save(createBook());
        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should delete book")
    public void deleteTest() {

        Book savedBook = entityManager.persist(createBook());

        Book foundBook = entityManager.find(Book.class, savedBook.getId());

        bookRepository.delete(foundBook);

        Book deletedBook = entityManager.find(Book.class, savedBook.getId());

        assertThat(deletedBook).isNull();

    }

}
