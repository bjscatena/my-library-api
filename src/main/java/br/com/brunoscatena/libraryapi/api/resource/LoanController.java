package br.com.brunoscatena.libraryapi.api.resource;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.model.entity.Loan;
import br.com.brunoscatena.libraryapi.service.BookService;
import br.com.brunoscatena.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final BookService bookService;
    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto) {

	Book book = bookService.findByIsbn(dto.getIsbn())
		.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
			"Book not found for passed ISBN"));

	Loan loan = Loan.builder()
		.book(book)
		.customer(dto.getCustomer())
		.loanDate(LocalDate.now())
		.build();

	Loan savedLoan = loanService.save(loan);

	return savedLoan.getId();

    }

}
