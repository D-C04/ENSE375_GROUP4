package com.example;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    @Bean
    public DatabaseSetup databaseSetup() throws IOException, SQLException {
        DatabaseSetup db = new DatabaseSetup("habittracker.db");
        db.createTables();
        return db;
    }

    @Bean
    public UserTable userTable(DatabaseSetup db) {
        return new UserTable(db);
    }
}