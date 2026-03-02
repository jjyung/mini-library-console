package com.example.library.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LibraryApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM borrow_records");
        jdbcTemplate.update("DELETE FROM books");
        jdbcTemplate.update("DELETE FROM sqlite_sequence WHERE name IN ('borrow_records', 'books')");
    }

    @Test
    void shouldCreateBookAndListBook() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Clean Code",
                                  "author": "Robert C. Martin",
                                  "totalQuantity": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.book.title").value("Clean Code"))
                .andExpect(jsonPath("$.data.book.availableQuantity").value(2));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.books[0].title").value("Clean Code"));
    }

    @Test
    void shouldBorrowAndReturnBook() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Domain-Driven Design",
                                  "author": "Eric Evans",
                                  "totalQuantity": 1
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/borrow-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 1,
                                  "borrowerName": "Samson"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.book.availableQuantity").value(0))
                .andExpect(jsonPath("$.data.borrowRecord.status").value("BORROWED"));

        mockMvc.perform(patch("/borrow-records/{borrowRecordId}/return", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.book.availableQuantity").value(1))
                .andExpect(jsonPath("$.data.borrowRecord.status").value("RETURNED"));
    }

    @Test
    void shouldRejectBorrowWhenNoAvailableQuantity() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Designing Data-Intensive Applications",
                                  "author": "Martin Kleppmann",
                                  "totalQuantity": 1
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/borrow-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 1,
                                  "borrowerName": "Samson"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/borrow-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 1,
                                  "borrowerName": "Alex"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A0000"));
    }

    @Test
    void shouldRejectReturnWhenBorrowRecordAlreadyReturned() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Refactoring",
                                  "author": "Martin Fowler",
                                  "totalQuantity": 1
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/borrow-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 1,
                                  "borrowerName": "Samson"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/borrow-records/{borrowRecordId}/return", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/borrow-records/{borrowRecordId}/return", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A0000"));
    }
}
