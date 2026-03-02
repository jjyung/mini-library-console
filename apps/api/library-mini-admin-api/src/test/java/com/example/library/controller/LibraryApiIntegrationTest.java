package com.example.library.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:sqlite:target/library-mini-admin-api-test.db",
    "spring.datasource.driver-class-name=org.sqlite.JDBC"
})
@AutoConfigureMockMvc
class LibraryApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM borrow_records");
        jdbcTemplate.execute("DELETE FROM books");
    }

    @Test
    void shouldCompleteCreateBorrowReturnFlow() throws Exception {
        MvcResult createBookResult = mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Clean Code",
                      "author": "Robert C. Martin",
                      "totalCopies": 1
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.book.title").value("Clean Code"))
            .andReturn();

        long bookId = extractLong(createBookResult, "$.data.book.id");

        MvcResult borrowResult = mockMvc.perform(post("/api/v1/borrow-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "bookId": %d,
                      "borrowerName": "Samson"
                    }
                    """.formatted(bookId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.borrowRecord.status").value("BORROWED"))
            .andReturn();

        long borrowRecordId = extractLong(borrowResult, "$.data.borrowRecord.id");

        mockMvc.perform(patch("/api/v1/borrow-records/{borrowRecordId}/return", borrowRecordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "returnedBy": "Samson"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.borrowRecord.status").value("RETURNED"));

        mockMvc.perform(get("/api/v1/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.books[0].availableCopies").value(1));
    }

    @Test
    void shouldReturnA0000WhenBorrowingWithoutStock() throws Exception {
        MvcResult createBookResult = mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Clean Code",
                      "author": "Robert C. Martin",
                      "totalCopies": 1
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andReturn();

        long bookId = extractLong(createBookResult, "$.data.book.id");

        mockMvc.perform(post("/api/v1/borrow-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "bookId": %d,
                      "borrowerName": "Samson"
                    }
                    """.formatted(bookId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"));

        mockMvc.perform(post("/api/v1/borrow-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "bookId": %d,
                      "borrowerName": "Alex"
                    }
                    """.formatted(bookId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("A0000"))
            .andExpect(jsonPath("$.error.errorSubCode").value("A0004"));
    }

    private long extractLong(MvcResult mvcResult, String jsonPathExpression) throws Exception {
        String responseBody = mvcResult.getResponse().getContentAsString();
        Number extractedNumber = JsonPath.read(responseBody, jsonPathExpression);
        return extractedNumber.longValue();
    }
}
