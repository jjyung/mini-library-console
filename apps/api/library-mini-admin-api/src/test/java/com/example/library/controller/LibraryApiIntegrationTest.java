package com.example.library.controller;

import com.example.library.dao.BookDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.dao.DataAccessResourceFailureException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LibraryApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SpyBean
    private BookDao bookDao;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM loans");
        jdbcTemplate.update("DELETE FROM books");
        reset(bookDao);
    }

    @Test
    void createBorrowListAndReturnKeepInventoryConsistent() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code","isbn":"9780132350884","author":"Robert C. Martin","category":"Software","quantity":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.availableCount").value(1));

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"readerId":"reader-1","isbn":"9780132350884","dueDate":"2026-09-30"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.readerId").value("reader-1"))
                .andExpect(jsonPath("$.data.dueDate").value("2026-09-30"));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data[0].status").value("BORROWED"))
                .andExpect(jsonPath("$.data[0].availableCount").value(0));

        mockMvc.perform(post("/api/loans/return")
                        .contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"9780132350884\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.returnedAt").isNotEmpty());

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data[0].availableCount").value(1));
    }

    @Test
    void emptyCatalogueReturnsSuccessWithEmptyData() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void validationAndDuplicateUseBusinessErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"\",\"isbn\":\"isbn\",\"category\":\"Category\",\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0000"))
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.details").isArray());

        String validBook = """
                {"title":"Title","isbn":"duplicate-isbn","category":"Category","quantity":1}
                """;
        mockMvc.perform(post("/api/books").contentType(APPLICATION_JSON).content(validBook))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/books").contentType(APPLICATION_JSON).content(validBook))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0000"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void noCopyInactiveNoLoanAndAmbiguousReturnUseClientError() throws Exception {
        createBook("no-copy-isbn", 1, true);
        borrow("no-copy-isbn", "reader-1");
        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(APPLICATION_JSON)
                        .content("{\"readerId\":\"reader-2\",\"isbn\":\"no-copy-isbn\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0000"));

        createBook("inactive-isbn", 1, false);
        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(APPLICATION_JSON)
                        .content("{\"readerId\":\"reader-1\",\"isbn\":\"inactive-isbn\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0000"));

        createBook("ambiguous-isbn", 2, true);
        borrow("ambiguous-isbn", "reader-a");
        borrow("ambiguous-isbn", "reader-b");
        mockMvc.perform(post("/api/loans/return")
                        .contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"ambiguous-isbn\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0000"));

        mockMvc.perform(post("/api/loans/return")
                        .contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"ambiguous-isbn\",\"readerId\":\"reader-a\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        mockMvc.perform(post("/api/loans/return")
                        .contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"missing-isbn\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0000"));

        createBook("no-active-loan-isbn", 1, true);
        mockMvc.perform(post("/api/loans/return")
                        .contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"no-active-loan-isbn\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0000"));
    }

    @Test
    void failedBorrowRollsBackLoanInsertAndCountChange() throws Exception {
        createBook("rollback-borrow-isbn", 1, true);
        doThrow(new DataAccessResourceFailureException("forced book update failure"))
                .when(bookDao).decrementAvailableCount(any(UUID.class));

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(APPLICATION_JSON)
                        .content("{\"readerId\":\"reader-1\",\"isbn\":\"rollback-borrow-isbn\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("B0000"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());

        reset(bookDao);
        Integer loanCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loans", Integer.class);
        Integer availableCount = jdbcTemplate.queryForObject(
                "SELECT available_count FROM books WHERE isbn = ?", Integer.class, "rollback-borrow-isbn");
        org.assertj.core.api.Assertions.assertThat(loanCount).isZero();
        org.assertj.core.api.Assertions.assertThat(availableCount).isEqualTo(1);
    }

    @Test
    void failedReturnRollsBackReturnedAtAndCountChange() throws Exception {
        createBook("rollback-return-isbn", 1, true);
        borrow("rollback-return-isbn", "reader-1");
        doThrow(new DataAccessResourceFailureException("forced book update failure"))
                .when(bookDao).incrementAvailableCount(any(UUID.class));

        mockMvc.perform(post("/api/loans/return")
                        .contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"rollback-return-isbn\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("B0000"));

        reset(bookDao);
        Integer availableCount = jdbcTemplate.queryForObject(
                "SELECT available_count FROM books WHERE isbn = ?", Integer.class, "rollback-return-isbn");
        Integer activeLoans = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM loans WHERE returned_at IS NULL", Integer.class);
        org.assertj.core.api.Assertions.assertThat(availableCount).isZero();
        org.assertj.core.api.Assertions.assertThat(activeLoans).isEqualTo(1);
    }

    private void createBook(String isbn, int quantity, boolean active) throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Title","isbn":"%s","category":"Category","quantity":%d,"isActive":%s}
                                """.formatted(isbn, quantity, active)))
                .andExpect(status().isCreated());
    }

    private void borrow(String isbn, String readerId) throws Exception {
        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(APPLICATION_JSON)
                        .content("{\"readerId\":\"%s\",\"isbn\":\"%s\"}".formatted(readerId, isbn)))
                .andExpect(status().isOk());
    }
}
