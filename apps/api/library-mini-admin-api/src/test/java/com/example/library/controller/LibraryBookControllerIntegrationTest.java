package com.example.library.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LibraryBookControllerIntegrationTest {
    private static final String BOOKS_PATH = "/api/books";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM loans");
        jdbcTemplate.update("DELETE FROM books");
    }

    @Test
    void AC_UI_001_returnsPagedCatalogueMetadata() throws Exception {
        createBook("978-0-13-235088-4", "Clean Code", "create-page-1");
        createBook("978-7-115-48570-5", "深度學習", "create-page-2");

        mockMvc.perform(get(BOOKS_PATH)
                        .param("page", "1")
                        .param("pageSize", "1")
                        .header("X-Correlation-Id", "test-page-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.traceId").value("test-page-1"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(1));

        mockMvc.perform(get(BOOKS_PATH)
                        .param("page", "2")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(1));
    }

    @Test
    void AC_003_AC_006_borrowAndReturnKeepInventoryConsistent() throws Exception {
        JsonNode createdBook = createBook("978-0-13-235088-4", "Clean Code", "create-transaction");
        String bookId = createdBook.path("data").path("bookId").asText();

        String borrowJson = "{\"readerId\":\"qa-reader-001\"}";
        JsonNode borrowed = objectMapper.readTree(mockMvc.perform(post(BOOKS_PATH + "/" + bookId + "/borrow")
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", "borrow-transaction")
                        .content(borrowJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.book.availableCount").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString());
        String loanId = borrowed.path("data").path("loan").path("loanId").asText();

        mockMvc.perform(post(BOOKS_PATH + "/" + bookId + "/return")
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", "return-transaction")
                        .content("{\"loanId\":\"" + loanId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.book.availableCount").value(2))
                .andExpect(jsonPath("$.data.book.status").value("available"))
                .andExpect(jsonPath("$.data.loan.status").value("RETURNED"));
    }

    @Test
    void AC_008_returnsEachActiveCopyIndependently() throws Exception {
        JsonNode createdBook = createBook("978-0-13-235088-4", "Two Copy Book", "create-two-copy");
        String bookId = createdBook.path("data").path("bookId").asText();

        JsonNode firstBorrow = borrowBook(bookId, "qa-reader-one", "borrow-two-copy-one");
        JsonNode secondBorrow = borrowBook(bookId, "qa-reader-two", "borrow-two-copy-two");

        mockMvc.perform(post(BOOKS_PATH + "/" + bookId + "/return")
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", "return-two-copy-one")
                        .content("{\"loanId\":\"" + firstBorrow.path("data").path("loan").path("loanId").asText() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.book.availableCount").value(1))
                .andExpect(jsonPath("$.data.book.status").value("borrowed"))
                .andExpect(jsonPath("$.data.loan.status").value("RETURNED"));

        mockMvc.perform(post(BOOKS_PATH + "/" + bookId + "/return")
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", "return-two-copy-two")
                        .content("{\"loanId\":\"" + secondBorrow.path("data").path("loan").path("loanId").asText() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.book.availableCount").value(2))
                .andExpect(jsonPath("$.data.book.status").value("available"))
                .andExpect(jsonPath("$.data.loan.status").value("RETURNED"));
    }

    @Test
    void AC_003_acceptsOptionalJsonNullableFieldsAcrossBorrowJourney() throws Exception {
        String createResponse = mockMvc.perform(post(BOOKS_PATH)
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", "create-json-nullable-fields")
                        .content("{\"title\":\"Nullable Book\",\"isbn\":\"978-0132350886\","
                                + "\"author\":\"Robert Martin\",\"category\":\"technology\","
                                + "\"quantity\":1,\"isActive\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.author").value("Robert Martin"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String bookId = objectMapper.readTree(createResponse).path("data").path("bookId").asText();

        mockMvc.perform(post(BOOKS_PATH + "/" + bookId + "/borrow")
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", "borrow-json-nullable-fields")
                        .content("{\"readerId\":\"qa-reader-optional\",\"dueDate\":\"2026-09-30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.loan.readerId").value("qa-reader-optional"))
                .andExpect(jsonPath("$.data.loan.dueDate").value("2026-09-30"));
    }

    @Test
    void AC_009_invalidPaginationReturnsClientBusinessCode() throws Exception {
        mockMvc.perform(get(BOOKS_PATH).param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0000"))
                .andExpect(jsonPath("$.details[0].field").value("page"));
    }

    @Test
    void AC_002_duplicateIsbnReturnsClientBusinessCode() throws Exception {
        createBook("978-0-13-235088-4", "Clean Code", "create-duplicate-1");

        mockMvc.perform(post(BOOKS_PATH)
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", "create-duplicate-2")
                        .content(bookJson("978-0-13-235088-4", "Another Book")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A0000"))
                .andExpect(jsonPath("$.details[0].field").value("isbn"));
    }

    private JsonNode createBook(String isbn, String title, String idempotencyKey) throws Exception {
        String response = mockMvc.perform(post(BOOKS_PATH)
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(bookJson(isbn, title)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("00000"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode borrowBook(String bookId, String readerId, String idempotencyKey) throws Exception {
        String response = mockMvc.perform(post(BOOKS_PATH + "/" + bookId + "/borrow")
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", idempotencyKey)
                        .content("{\"readerId\":\"" + readerId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private String bookJson(String isbn, String title) {
        return "{\"title\":\"" + title + "\",\"isbn\":\"" + isbn
                + "\",\"category\":\"technology\",\"quantity\":2,\"isActive\":true}";
    }
}
