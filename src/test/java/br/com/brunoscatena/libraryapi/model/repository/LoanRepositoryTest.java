package br.com.brunoscatena.libraryapi.model.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private Book createBookWithoutId() {
	return Book.builder().author("Bruno").isbn("1234").title("As aventuras").build();
    }

    private Loan createValidLoan(Book book) {
	return Loan.builder().book(book).customer("Rafael").loanDate(LocalDate.now()).build();
    }

    @Test
    @DisplayName("Should verify if there is a not returned loan for a book ")
    public void existsByBookIdAndReturnedTest() {

	Book book = createBookWithoutId();
	entityManager.persist(book);

	Loan loan = createValidLoan(book);
	entityManager.persist(loan);

	boolean exists = loanRepository.existsByBookIdAndNotReturned(book);

	assertTrue(exists);

    }

    public void findBookTestSetup() {

	Book book1 = createBookWithoutId();

	Book book2 = createBookWithoutId();
	book2.setIsbn("1111");

	entityManager.persist(book1);
	entityManager.persist(book2);

	Loan loan1 = createValidLoan(book1);
	entityManager.persist(loan1);

	Loan loan2 = createValidLoan(book1);
	entityManager.persist(loan2);

	Loan loan3 = createValidLoan(book2);
	loan3.setCustomer("Bruno");
	entityManager.persist(loan3);

    }

    @Test
    @DisplayName("Should find loan when passing isbn but no customer")
    public void findPassingIsbnAndNoCustomerTest() {

	findBookTestSetup();

	Pageable pageRequest = PageRequest.of(0, 10);

	Page<Loan> result = loanRepository.findByBookIsbnOrCustomer("1234", null, pageRequest);

	assertEquals(result.getNumberOfElements(), 2);
	assertEquals(result.getPageable().getPageNumber(), 0);
	assertEquals(result.getPageable().getPageSize(), 10);

    }

    @Test
    @DisplayName("Should find loan when passing customer but no isbn")
    public void findPassingCustomerAndNoIsbnTest() {

	findBookTestSetup();

	Pageable pageRequest = PageRequest.of(0, 10);

	Page<Loan> result = loanRepository.findByBookIsbnOrCustomer(null, "Rafael", pageRequest);

	assertEquals(result.getNumberOfElements(), 2);
	assertEquals(result.getPageable().getPageNumber(), 0);
	assertEquals(result.getPageable().getPageSize(), 10);

    }

    @Test
    @DisplayName("Should find loan when passing isbn and customer")
    public void findPassingCustomerAndIsbnTest() {

	findBookTestSetup();

	Pageable pageRequest = PageRequest.of(0, 10);

	Page<Loan> result = loanRepository.findByBookIsbnOrCustomer("1111", "Bruno", pageRequest);

	assertEquals(result.getNumberOfElements(), 1);
	assertEquals(result.getPageable().getPageNumber(), 0);
	assertEquals(result.getPageable().getPageSize(), 10);

    }

}
