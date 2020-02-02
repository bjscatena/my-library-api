package br.com.brunoscatena.libraryapi.api.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.model.entity.Loan;
import br.com.brunoscatena.libraryapi.service.BookService;
import br.com.brunoscatena.libraryapi.service.LoanService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    private static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    public void test(String abcdefg1,
	    String abcdefg2,
	    String abcdefg3,
	    String abcdefg4,
	    String abcdefg5,
	    String abcdefg6) {
    }

    private LoanDTO createLoanDTO(String isbn) {
	return LoanDTO.builder().isbn(isbn).customer("Bruno").build();
    }

    private MockHttpServletRequestBuilder createJsonPostRequest(String json) {

	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
		.accept(APPLICATION_JSON)
		.contentType(APPLICATION_JSON)
		.content(json);

	return request;
    }

    @Test
    @DisplayName("Should create a loan")
    public void createLoanTest() throws Exception {

	String isbn = "123";

	LoanDTO dto = createLoanDTO(isbn);
	String json = new ObjectMapper().writeValueAsString(dto);

	Book book = Book.builder().id(1L).isbn(isbn).build();
	Loan loan = Loan.builder()
		.id(1L)
		.customer(dto.getCustomer())
		.book(book)
		.loanDate(LocalDate.now())
		.build();

	when(bookService.findByIsbn(isbn)).thenReturn(Optional.of(book));
	when(loanService.save(any(Loan.class))).thenReturn(loan);

	MockHttpServletRequestBuilder request = createJsonPostRequest(json);

	mvc.perform(request).andExpect(status().isCreated()).andExpect(content().string("1"));

	verify(bookService, times(1)).findByIsbn(isbn);
	verify(loanService, times(1)).save(any(Loan.class));

    }

    @Test
    @DisplayName("Should return 400 when creating loan with invalid ISBN")
    public void invalidIsbnCreateLoanTest() throws Exception {

	// Arrange
	LoanDTO dto = createLoanDTO("0");
	String json = new ObjectMapper().writeValueAsString(dto);

	when(bookService.findByIsbn(dto.getIsbn())).thenReturn(Optional.empty());

	// Act
	MockHttpServletRequestBuilder request = createJsonPostRequest(json);
	ResultActions resultActions = mvc.perform(request);

	// Assert
	resultActions.andExpect(status().isBadRequest());

	verify(bookService, times(1)).findByIsbn(dto.getIsbn());
	verify(loanService, never()).save(any(Loan.class));

    }

}
