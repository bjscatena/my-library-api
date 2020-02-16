package br.com.brunoscatena.libraryapi.model.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.model.entity.Loan;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should verify if there is a not returned loan for a book ")
    public void existsByBookIdAndReturnedTest() {

	Book book = createBookWithoutId();
	entityManager.persist(book);

	Loan loan = Loan.builder().book(book).customer("Rafael").loanDate(LocalDate.now()).build();
	entityManager.persist(loan);

	boolean exists = loanRepository.existsByBookIdAndNotReturned(book);

	assertTrue(exists);

    }

    private Book createBookWithoutId() {
	return Book.builder().author("Bruno").isbn("1234").title("As aventuras").build();
    }

}
