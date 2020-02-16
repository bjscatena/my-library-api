package br.com.brunoscatena.libraryapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.brunoscatena.libraryapi.exception.BusinessException;
import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.model.entity.Loan;
import br.com.brunoscatena.libraryapi.model.repository.LoanRepository;
import br.com.brunoscatena.libraryapi.service.impl.LoanServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    @MockBean
    private LoanRepository loanRepository;

    private LoanServiceImpl loanService;

    private Book createValidBook() {
	return Book.builder().id(1L).isbn("123").build();
    }

    private Loan createValidLoanWithId(Book book) {
	return Loan.builder().book(book).customer("Bruno").id(1L).loanDate(LocalDate.now()).build();
    }

    private Loan createValidLoan(Book book) {
	return Loan.builder().book(book).customer("Bruno").loanDate(LocalDate.now()).build();
    }

    @BeforeEach
    public void setUp() {
	this.loanService = new LoanServiceImpl(loanRepository);
    }

    @Test
    @DisplayName("Should save loan")
    public void saveTest() {

	// Arrange
	Book book = createValidBook();
	Loan newLoan = createValidLoan(book);
	Loan savedLoan = createValidLoanWithId(book);

	when(loanRepository.existsByBookIdAndNotReturned(book)).thenReturn(false);
	when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

	// Act
	Loan returnedLoan = loanService.save(newLoan);

	// Assert
	assertEquals(returnedLoan.getId(), savedLoan.getId());
	assertEquals(returnedLoan.getBook(), savedLoan.getBook());
	assertEquals(returnedLoan.getCustomer(), savedLoan.getCustomer());
	assertEquals(returnedLoan.getLoanDate(), savedLoan.getLoanDate());
	assertEquals(returnedLoan.getReturned(), savedLoan.getReturned());

	verify(loanRepository, times(1)).save(newLoan);

    }

    @Test
    @DisplayName("Should throw error when creating loan with an already loaned book")
    public void saveLoanedBookTest() {

	// Arrange
	Book book = createValidBook();
	Loan newLoan = createValidLoan(book);

	when(loanRepository.existsByBookIdAndNotReturned(book)).thenReturn(true);

	// Act
	Executable saveExecutable = () -> {
	    loanService.save(newLoan);
	};

	// Assert
	assertThrows(BusinessException.class, saveExecutable);

	verify(loanRepository, times(1)).existsByBookIdAndNotReturned(book);
    }

    @Test
    @DisplayName("Should find Loan by ID")
    public void findByIdTest() {

	// Arrange
	long id = 2L;

	Book book = createValidBook();
	Loan loan = createValidLoanWithId(book);

	when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

	// Act
	Optional<Loan> optLoan = loanService.findById(id);

	// Assert
	assertTrue(optLoan.isPresent());
	verify(loanRepository, times(1)).findById(id);

    }

    @Test
    @DisplayName("Should update Loan")
    public void updateLoanTest() {

	// Arrange
	Book book = createValidBook();
	Loan loan = createValidLoanWithId(book);
	loan.setReturned(true);

	when(loanRepository.save(loan)).thenReturn(loan);

	// Act
	Loan updatedLoan = loanService.update(loan);

	// Assert
	assertEquals(updatedLoan.getReturned(), loan.getReturned());
	verify(loanRepository, times(1)).save(loan);

    }

}
