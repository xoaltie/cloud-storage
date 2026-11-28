package com.github.xoaltie.cloudstorage.runner;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DevDataInitializerRunner implements ApplicationRunner {
    private final JdbcClient jdbcClient;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializerRunner(JdbcClient jdbcClient, PasswordEncoder passwordEncoder) {
        this.jdbcClient = jdbcClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        String hashedPassword = passwordEncoder.encode("password");
        String username = "test_username";

        String userSeedSql = """
                INSERT INTO users (username, password)
                VALUES (:username, :password)
                ON CONFLICT (username) DO NOTHING
                """;
        jdbcClient.sql(userSeedSql)
                .param("username", username)
                .param("password", hashedPassword)
                .update();

        String resourcesSeedSql = """
                INSERT INTO resources (id, name, type, size, parent_id, user_id)
                VALUES
                    (1,  'Documents',          'DIRECTORY', NULL,     NULL, 1),
                    (2,  'Media',              'DIRECTORY', NULL,     NULL, 1),
                    (3,  'readme.md',          'FILE',      2048,     NULL, 1),
                    (4,  'Work',               'DIRECTORY', NULL,     1,    1),
                    (5,  'Personal',           'DIRECTORY', NULL,     1,    1),
                    (6,  'Projects',           'DIRECTORY', NULL,     4,    1),
                    (7,  'Reports',            'DIRECTORY', NULL,     4,    1),
                    (8,  'Backend_API',        'DIRECTORY', NULL,     6,    1),
                    (9,  'schema.sql',         'FILE',      15360,    8,    1),
                    (10, 'docker-compose.yml', 'FILE',      4096,     8,    1),
                    (11, 'Q3_summary.pdf',     'FILE',      1048576,  7,    1),
                    (12, 'budget_2026.xlsx',   'FILE',      524288,   7,    1),
                    (13, 'passport_scan.pdf',  'FILE',      2097152,  5,    1),
                    (14, 'notes.txt',          'FILE',      1024,     5,    1),
                    (15, 'Photos',             'DIRECTORY', NULL,     2,    1),
                    (16, 'Videos',             'DIRECTORY', NULL,     2,    1),
                    (17, 'Vacation_2026',      'DIRECTORY', NULL,     15,   1),
                    (18, 'mountains.jpg',      'FILE',      4194304,  17,   1),
                    (19, 'beach.png',          'FILE',      6291456,  17,   1),
                    (20, 'demo_recording.mp4', 'FILE',      52428800, 16,   1)
                ON CONFLICT DO NOTHING
                """;
        jdbcClient.sql(resourcesSeedSql)
                .update();

        String syncResourceSequenceSql = """
                SELECT setval(pg_get_serial_sequence('resources', 'id'), (SELECT MAX(id) FROM resources))
                """;
        jdbcClient.sql(syncResourceSequenceSql)
                .query()
                .singleValue();
    }
}
