package com.github.xoaltie.cloudstorage.runner;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializerRunner implements ApplicationRunner {
    private final JdbcClient jdbcClient;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializerRunner(JdbcClient jdbcClient, PasswordEncoder encoder) {
        this.jdbcClient = jdbcClient;
        this.passwordEncoder = encoder;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        String hashedPassword = passwordEncoder.encode("password");
        String username = "test_username";

        String sql = """
                INSERT INTO users (username, password)
                VALUES (:username, :password)
                ON CONFLICT (username) DO NOTHING
                """;
        jdbcClient.sql(sql)
                .param("username", username)
                .param("password", hashedPassword)
                .update();
    }
}
