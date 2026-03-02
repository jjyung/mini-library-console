package com.example.library.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IdSequenceRepository {
    private final JdbcTemplate jdbcTemplate;

    public IdSequenceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public synchronized long nextValue(String sequenceName) {
        jdbcTemplate.update("UPDATE id_sequences SET current_value = current_value + 1 WHERE sequence_name = ?", sequenceName);
        Long value = jdbcTemplate.queryForObject(
                "SELECT current_value FROM id_sequences WHERE sequence_name = ?",
                Long.class,
                sequenceName
        );
        return value == null ? 1L : value;
    }
}
