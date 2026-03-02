package com.example.library.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateBookSuccessfully() throws Exception {
        mockMvc.perform(post("/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Clean Code",
                                  "isbn": "9780132350884",
                                  "author": "Robert C. Martin",
                                  "category": "Software",
                                  "quantity": 3,
                                  "shelfStatus": "ON_SHELF"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.book.isbn").value("9780132350884"));
    }

    @Test
    void shouldRejectDuplicateIsbn() throws Exception {
        String payload = """
                {
                  "title": "Domain-Driven Design",
                  "isbn": "9780321125217",
                  "author": "Eric Evans",
                  "category": "Software",
                  "quantity": 2,
                  "shelfStatus": "ON_SHELF"
                }
                """;
        mockMvc.perform(post("/library/books").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        mockMvc.perform(post("/library/books").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A0002"));
    }

    @Test
    void shouldBorrowAndReturnBook() throws Exception {
        mockMvc.perform(post("/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Designing Data-Intensive Applications",
                                  "isbn": "9781449373320",
                                  "author": "Martin Kleppmann",
                                  "category": "Software",
                                  "quantity": 1,
                                  "shelfStatus": "ON_SHELF"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/library/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "readerId": "reader-001",
                                  "isbn": "9781449373320"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        mockMvc.perform(post("/library/loans/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isbn": "9781449373320",
                                  "readerId": "reader-001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));
    }

    @Test
    void shouldRejectBorrowWhenDueDateIsPast() throws Exception {
        mockMvc.perform(post("/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Refactoring",
                                  "isbn": "9780201485677",
                                  "author": "Martin Fowler",
                                  "category": "Software",
                                  "quantity": 1,
                                  "shelfStatus": "ON_SHELF"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/library/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "readerId": "reader-002",
                                  "isbn": "9780201485677",
                                  "dueDate": "2020-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A0003"));
    }

    @Test
    void shouldListAndSearchBooks() throws Exception {
        mockMvc.perform(post("/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Patterns of Enterprise Application Architecture",
                                  "isbn": "9780321127426",
                                  "author": "Martin Fowler",
                                  "category": "Software",
                                  "quantity": 2,
                                  "shelfStatus": "ON_SHELF"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/library/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        mockMvc.perform(get("/library/books/search").param("keyword", "Patterns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));
    }

    @Test
    void shouldReturnA0003WhenSearchKeywordMissing() throws Exception {
        mockMvc.perform(get("/library/books/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0003"));
    }

    @Test
    void shouldReturnA0007WhenBookReturnedOverdue() throws Exception {
        LocalDate today = LocalDate.now();
        String dueDate = today.toString();
        String returnedAt = OffsetDateTime.of(today.plusDays(1).atStartOfDay(), ZoneOffset.UTC).toString();

        mockMvc.perform(post("/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Working Effectively with Legacy Code",
                                  "isbn": "9780131177055",
                                  "author": "Michael Feathers",
                                  "category": "Software",
                                  "quantity": 1,
                                  "shelfStatus": "ON_SHELF"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/library/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "readerId": "reader-003",
                                  "isbn": "9780131177055",
                                  "dueDate": "%s"
                                }
                                """, dueDate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        mockMvc.perform(post("/library/loans/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "isbn": "9780131177055",
                                  "readerId": "reader-003",
                                  "returnedAt": "%s"
                                }
                                """, returnedAt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A0007"))
                .andExpect(jsonPath("$.data.returnRecord.overdueDays").value(1));
    }
}
