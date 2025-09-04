package com.example.carins.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class H2IdentityReset implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public H2IdentityReset(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long maxId = jdbc.queryForObject(
                "select coalesce(max(id), 0) from insurancepolicy", Long.class);
        long next = (maxId == null ? 0L : maxId) + 1L;
        jdbc.execute("ALTER TABLE insurancepolicy ALTER COLUMN id RESTART WITH " + next);

        jdbc.execute("""
            update insurancepolicy
               set end_date = dateadd('YEAR', 1, start_date)
             where end_date is null
        """);
    }
}