package br.com.brunoscatena.libraryapi.api.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.brunoscatena.libraryapi.api.dto.LoanDTO;
import br.com.brunoscatena.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.brunoscatena.libraryapi.exception.BusinessException;
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

    private LoanDTO createLoanDTO(String isbn) {
	return LoanDTO.builder().isbn(isbn).customer("Bruno").build();
    }

    private Loan createLoanWithBook() {

	Book book = createValidBook();

	Loan loan = Loan.builder()
		.id(1L)
		.customer("José")
		.book(book)
		.returned(false)
		.loanDate(LocalDate.now())
		.build();

	return loan;
    }

    private Book createValidBook() {
	Book book = Book.builder()
		.id(1L)
		.author("Bruno")
		.title("As aventuras")
		.isbn("1234")
		.build();
	return book;
    }

    private String createReturnedJson(boolean returned) throws JsonProcessingException {
	ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(returned).build();
	String json = new ObjectMapper().writeValueAsString(dto);
	return json;
    }

    private MockHttpServletRequestBuilder createJsonPostRequest(String json) {

	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
		.accept(APPLICATION_JSON)
		.contentType(APPLICATION_JSON)
		.content(json);

	return request;
    }

    private MockHttpServletRequestBuilder createJsonPatchRequest(String json) {
	return MockMvcRequestBuilders.patch(LOAN_API + "/1")
		.accept(APPLICATION_JSON)
		.contentType(APPLICATION_JSON)
		.content(json);
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
	resultActions.andExpect(status().isBadRequest())
		.andExpect(jsonPath("errors", Matchers.hasSize(1)))
		.andExpect(jsonPath("errors[0]").value("Book not found for passed ISBN"));

	verify(bookService, times(1)).findByIsbn(dto.getIsbn());
	verify(loanService, never()).save(any(Loan.class));

    }

    @Test
    @DisplayName("Should throw error when loaning an already loaned book")
    public void createLoanWihtAlreadyLoanedBookTest() throws Exception {

	String isbn = "123";

	LoanDTO dto = createLoanDTO(isbn);
	String json = new ObjectMapper().writeValueAsString(dto);

	Book book = Book.builder().id(1L).isbn(isbn).build();

	when(bookService.findByIsbn(isbn)).thenReturn(Optional.of(book));
	when(loanService.save(any(Loan.class)))
		.thenThrow(new BusinessException("Book already loaned"));

	MockHttpServletRequestBuilder request = createJsonPostRequest(json);

	mvc.perform(request)
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("errors", Matchers.hasSize(1)))
		.andExpect(jsonPath("errors[0]").value("Book already loaned"));

	verify(bookService, times(1)).findByIsbn(isbn);
	verify(loanService, times(1)).save(any(Loan.class));

    }

    @Test
    @DisplayName("Should return loaned book")
    public void returnLoanTest() throws Exception {

	// Arrange
	boolean returned = true;
	String json = createReturnedJson(returned);

	Loan loan = createLoanWithBook();

	when(loanService.findById(any(Long.class))).thenReturn(Optional.of(loan));

	// Act
	MockHttpServletRequestBuilder patchRequest = createJsonPatchRequest(json);
	ResultActions resultActions = mvc.perform(patchRequest);

	// Assert
	resultActions.andExpect(status().isOk());

	ArgumentCaptor<Loan> argumentCaptor = ArgumentCaptor.forClass(Loan.class);
	verify(loanService).update(argumentCaptor.capture());
	Loan capturedArgument = argumentCaptor.getValue();

	verify(loanService, times(1)).findById(any(Long.class));
	verify(loanService, times(1)).update(any(Loan.class));

	assertEquals(capturedArgument.getReturned(), returned);

    }

    @Test
    @DisplayName("Should return not found when returning a book with a inexistent loan id")
    public void returnInvalidLoanTest() throws Exception {

	String json = createReturnedJson(true);

	when(loanService.findById(any(Long.class))).thenReturn(Optional.empty());

	// Act
	MockHttpServletRequestBuilder patchRequest = createJsonPatchRequest(json);
	ResultActions resultActions = mvc.perform(patchRequest);

	// Assert
	resultActions.andExpect(status().isNotFound());

	verify(loanService, never()).update(any(Loan.class));

    }

    @Test
    @DisplayName("Should filter loans")
    public void findBooksTest() throws Exception {

	Book book = createValidBook();

	String customer = "José";
	String isbn = book.getIsbn();

	Loan loan = Loan.builder().book(book).customer(customer).build();
	Pageable pageRequest = PageRequest.of(0, 100);

	String parameters = String.format("?isbn=%s&customer=%s&page=0&size=100", isbn, customer);

	MockHttpServletRequestBuilder getRequest = MockMvcRequestBuilders.get(LOAN_API + parameters)
		.accept(APPLICATION_JSON);

	ResultActions resultActions = mvc.perform(getRequest);

	// Find Loans using passed parameters

	// Assert
	resultActions.andExpect(status().isOk())
		.andExpect(jsonPath("content", Matchers.hasSize(1)))
		.andExpect(jsonPath("content[0].id").value(loan.getId()))
		.andExpect(jsonPath("totalElements").value(1));

	verify(loanService, times(1)).find(loan, pageRequest);

	// Return found Loans

    }

}
