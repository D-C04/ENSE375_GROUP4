package com.example;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAuth {

    private static final Logger log = LoggerFactory.getLogger(UserAuth.class);

    private final UserTable userTable;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserAuth(UserTable userTable) {
        this.userTable = userTable;
    }

    // Username: at least 8 characters, unique. Password: at least 8 characters.
    // Email: valid-looking and unique.
    public boolean signUp(String username, String email, String password) {
        if (username == null || email == null || password == null) return false;

        String name = username.trim().toLowerCase();
        String mail = email.trim().toLowerCase();

        if (name.length() < 8) return false;
        if (password.isBlank() || password.length() < 8) return false;
        if (!mail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) return false;

        try {
            if (userTable.doesUsernameExist(name) || userTable.doesEmailExist(mail)) {
                return false;
            }
            userTable.addUser(new UserModel(name, encoder.encode(password), mail));
            return true;
        } catch (SQLException e) {
            // Also catches the rare case where two people grab the same name at once
            log.error("Sign up failed", e);
            return false;
        }
    }

    public boolean authorizeUser(String username, String password) {
        if (username == null || password == null) return false;

        try {
            UserModel user = userTable.getUserByUsername(username.trim().toLowerCase());
            return user != null && encoder.matches(password, user.getPasswordHash());
        } catch (SQLException e) {
            log.error("Login lookup failed", e);
            return false;
        }
    }
}