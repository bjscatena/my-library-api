package br.com.brunoscatena.libraryapi.api.resource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.brunoscatena.libraryapi.api.dto.BookDTO;
import br.com.brunoscatena.libraryapi.api.dto.LoanDTO;
import br.com.brunoscatena.libraryapi.api.dto.LoanFilterDTO;
import br.com.brunoscatena.libraryapi.api.dto.ReturnedLoanDTO;
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
    private final ModelMapper modelMapper;

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

    @PatchMapping("{id}")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
	Loan loan = loanService.findById(id)
		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

	loan.setReturned(dto.getReturned());
	loanService.update(loan);
    }

    @GetMapping()
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest) {
	Page<Loan> result = loanService.find(dto, pageRequest);
	List<LoanDTO> loans = result.getContent().stream().map(entity -> {
	    Book book = entity.getBook();
	    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
	    LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
	    loanDTO.setBook(bookDTO);
	    return loanDTO;
	}).collect(Collectors.toList());

	return new PageImpl<LoanDTO>(loans, pageRequest, result.getTotalElements());
    }

}
