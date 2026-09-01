package com.example.library;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryApiIntegrationTest {

    private static final String BOOKS_PATH = "/books";
    private static final String LOANS_PATH = "/loans";
    private static final String RETURNS_PATH = "/loans/returns";
    private static final String CLEAN_CODE_ISBN = "9780132350884";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateBookAndListIt() throws Exception {
        String requestBody = """
                {
                  "title": "Clean Code",
                  "isbn": "%s",
                  "author": "Robert C. Martin",
                  "category": "technology",
                  "quantity": 2,
                  "isActive": true
                }
                """.formatted(CLEAN_CODE_ISBN);

        mockMvc.perform(post(BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.availableCount").value(2));

        mockMvc.perform(get(BOOKS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data[0].isbn").value(CLEAN_CODE_ISBN));
    }

    @Test
    void shouldRejectDuplicateBookAndUnavailableCheckout() throws Exception {
        String requestBody = """
                {
                  "title": "Duplicate Book",
                  "isbn": "9780306406157",
                  "category": "science",
                  "quantity": 1,
                  "isActive": true
                }
                """;

        mockMvc.perform(post(BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A0003"));

        String checkoutBody = """
                {
                  "readerId": "reader-001",
                  "isbn": "9780306406157"
                }
                """;

        mockMvc.perform(post(LOANS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("00000"));

        mockMvc.perform(post(LOANS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A0004"));
    }

    @Test
    void shouldCheckoutAndReturnBook() throws Exception {
        String createBody = """
                {
                  "title": "Domain-Driven Design",
                  "isbn": "9780321125217",
                  "category": "technology",
                  "quantity": 1,
                  "isActive": true
                }
                """;
        mockMvc.perform(post(BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        String checkoutBody = """
                {
                  "readerId": "reader-002",
                  "isbn": "9780321125217"
                }
                """;
        mockMvc.perform(post(LOANS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.returned").value(false));

        String returnBody = """
                {
                  "isbn": "9780321125217",
                  "readerId": "reader-002"
                }
                """;
        mockMvc.perform(post(RETURNS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.book.availableCount").value(1));
    }

    @Test
    void shouldAllowFrontendCorsPreflight() throws Exception {
        mockMvc.perform(options(BOOKS_PATH)
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("POST")));
    }
}
