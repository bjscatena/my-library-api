package br.com.brunoscatena.libraryapi.api.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.brunoscatena.libraryapi.api.dto.BookDTO;
import br.com.brunoscatena.libraryapi.exception.BusinessException;
import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.service.BookService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    private BookDTO createNewBook() {
	return BookDTO.builder().author("Artur").title("As aventuras").isbn("001").build();
    }

    @Test
    @DisplayName("Should create book")
    public void createBookTest() throws Exception {

	BookDTO dto = createNewBook();
	Book savedBook = Book.builder()
		.id(10L)
		.author("Artur")
		.title("As aventuras")
		.isbn("001")
		.build();

	BDDMockito.given(service.save(any(Book.class))).willReturn(savedBook);

	String json = new ObjectMapper().writeValueAsString(dto);

	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.content(json);

	this.mvc.perform(request)
		.andExpect(status().isCreated())
		.andExpect(jsonPath("id").isNotEmpty())
		.andExpect(jsonPath("title").value(dto.getTitle()))
		.andExpect(jsonPath("author").value(dto.getAuthor()))
		.andExpect(jsonPath("isbn").value(dto.getIsbn()));
    }

    @Test
    @DisplayName("Should throw validation error when not enough data available")
    public void createInvalidBookTest() throws Exception {

	BookDTO dto = new BookDTO();
	String json = new ObjectMapper().writeValueAsString(dto);

	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.content(json);

	mvc.perform(request)
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("errors", Matchers.hasSize(3)));

    }

    @Test
    @DisplayName("Should throw error when trying to save book with ISBN already used")
    public void createBookWithDuplicatedIsbn() throws Exception {

	BookDTO dto = createNewBook();
	String json = new ObjectMapper().writeValueAsString(dto);

	String errorMessage = "ISBN already being used";
	BDDMockito.given(service.save(any(Book.class)))
		.willThrow(new BusinessException(errorMessage));

	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.content(json);

	mvc.perform(request)
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("errors", Matchers.hasSize(1)))
		.andExpect(jsonPath("errors[0]").value(errorMessage));

    }

    @Test
    @DisplayName("Should get a book from id")
    public void getBookByIdTest() throws Exception {

	// given
	Long id = 1L;
	Book book = Book.builder()
		.id(id)
		.title("Adventures")
		.author("Some One")
		.isbn("1234")
		.build();
	BDDMockito.given(service.findById(id)).willReturn(Optional.of(book));

	// when
	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API + "/" + id)
		.accept(MediaType.APPLICATION_JSON);

	// then
	mvc.perform(request)
		.andExpect(status().isOk())
		.andExpect(jsonPath("title").value("Adventures"))
		.andExpect(jsonPath("author").value("Some One"))
		.andExpect(jsonPath("isbn").value("1234"))
		.andExpect(jsonPath("id").value(id));

    }

    @Test
    @DisplayName("Should return not found when the book doesn't exists")
    public void bookNotFoundTest() throws Exception {

	BDDMockito.given(service.findById(anyLong())).willReturn(Optional.empty());

	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API + "/1")
		.accept(MediaType.APPLICATION_JSON);

	mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @DisplayName("Should update book")
    public void updateBookTest() throws Exception {

	Long id = 1L;

	Book oldBook = Book.builder()
		.id(id)
		.title("As aventuras")
		.author("Fulano")
		.isbn("12345")
		.build();

	BookDTO newBookValues = BookDTO.builder()
		.author("Fulano Editado")
		.title("As aventuras Editado")
		.isbn("54321")
		.build();

	Book editedBook = Book.builder()
		.id(id)
		.title(newBookValues.getTitle())
		.author(newBookValues.getAuthor())
		.isbn(newBookValues.getIsbn())
		.build();

	String json = new ObjectMapper().writeValueAsString(newBookValues);

	when(service.findById(id)).thenReturn(Optional.of(oldBook));
	when(service.update(any(Book.class))).thenReturn(editedBook);

	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API + "/1")
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.content(json);

	mvc.perform(request)
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("title").value(newBookValues.getTitle()))
		.andExpect(
			MockMvcResultMatchers.jsonPath("author").value(newBookValues.getAuthor()))
		.andExpect(MockMvcResultMatchers.jsonPath("isbn").value(newBookValues.getIsbn()))
		.andExpect(MockMvcResultMatchers.jsonPath("id").value(id));

	verify(service, times(1)).findById(id);
	verify(service, times(1)).update(any(Book.class));
	verifyNoMoreInteractions(service);

    }

    @Test
    @DisplayName("Should return 404 when update a non existent book")
    public void updateInexistentBookTest() throws Exception {

	BookDTO newBookValues = BookDTO.builder()
		.author("Fulano Editado")
		.title("As aventuras Editado")
		.isbn("54321")
		.build();

	String json = new ObjectMapper().writeValueAsString(newBookValues);

	when(service.findById(anyLong())).thenReturn(Optional.empty());

	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API + "/1")
		.content(json)
		.accept(MediaType.APPLICATION_JSON)
		.contentType(MediaType.APPLICATION_JSON);

	mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());

	verify(service, never()).update(any(Book.class));

    }

    @Test
    @DisplayName("Should delete book by id")
    public void bookDeleteTest() throws Exception {

	Book bookMock = Book.builder().id(1L).build();

	when(service.findById(anyLong())).thenReturn(Optional.of(bookMock));
	doNothing().when(service).delete(any(Book.class));

	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API + "/1");

	mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());

	verify(service, times(1)).findById(1L);
	verify(service, times(1)).delete(any(Book.class));
	verifyNoMoreInteractions(service);

    }

    @Test
    @DisplayName("Should return not found when deleting a non existent entity")
    public void deleteInexistentBookTest() throws Exception {

	when(service.findById(anyLong())).thenReturn(Optional.empty());

	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API + "/1")
		.accept(MediaType.APPLICATION_JSON);

	mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());

	verify(service, times(1)).findById(1L);
	verify(service, never()).delete(any(Book.class));

    }

    @Test
    @DisplayName("Should find book using filters")
    public void findBookTest() throws Exception {

	Book book = Book.builder()
		.id(1L)
		.author("Bruno")
		.title("As aventuras")
		.isbn("1234")
		.build();

	List<Book> books = Arrays.asList(book);
	PageImpl<Book> bookPage = new PageImpl<Book>(books, PageRequest.of(0, 100), 1);

	when(service.find(any(Book.class), any(Pageable.class))).thenReturn(bookPage);

	MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API)
		.queryParam("author", book.getAuthor())
		.queryParam("title", book.getTitle())
		.queryParam("page", "0")
		.queryParam("size", "100")
		.accept(MediaType.APPLICATION_JSON);

	mvc.perform(request)
		.andExpect(status().isOk())
		.andExpect(jsonPath("content", Matchers.hasSize(1)))
		.andExpect(jsonPath("content[0].id").value(book.getId()))
		.andExpect(jsonPath("totalElements").value(1));

    }

}
