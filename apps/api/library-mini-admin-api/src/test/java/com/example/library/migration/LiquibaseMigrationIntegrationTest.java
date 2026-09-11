package com.example.library.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LiquibaseMigrationIntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void migrationCreatesBooksAndLoansTablesWithRequiredIndexes() {
        Integer booksTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BOOKS'",
                Integer.class
        );
        Integer loansTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'LOANS'",
                Integer.class
        );
        Integer bookIndexCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES WHERE INDEX_NAME = 'UK_BOOKS_ISBN'",
                Integer.class
        );

        assertThat(booksTableCount).isEqualTo(1);
        assertThat(loansTableCount).isEqualTo(1);
        assertThat(bookIndexCount).isEqualTo(1);
    }
}
