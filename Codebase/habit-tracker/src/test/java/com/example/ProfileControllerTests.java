package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.ui.ExtendedModelMap;

public class ProfileControllerTests {

    @TempDir
    Path tempDir;

    private DatabaseSetup databaseSetup;
    private UserTable userTable;
    private ProfileController controller;

    @BeforeEach
    void setUp() throws Exception {
        databaseSetup = new DatabaseSetup(tempDir, "profile-controller-test.db");
        databaseSetup.createTables();
        userTable = new UserTable(databaseSetup);
        controller = new ProfileController(userTable);
    }

    @Test
    void getProfileWithoutLoginRedirectsToLogin() {
        MockHttpSession session = new MockHttpSession();
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("redirect:/login", controller.getProfile(session, model));
    }

    @Test
    void getProfileLoadsCurrentUser() throws SQLException {
        userTable.addUser(new UserModel("testuser", "oldhash", "test@example.com"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testuser");
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("Profile", controller.getProfile(session, model));
        assertEquals("testuser", model.get("username"));
        assertEquals("test@example.com", model.get("email"));
        assertEquals("••••••••••••••", model.get("password"));
    }

    @Test
    void editDetailsRequiresLogin() {
        MockHttpSession session = new MockHttpSession();
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("redirect:/login", controller.editDetailsPage(session, model));
    }

    @Test
    void editDetailsLoadsCurrentUser() throws SQLException {
        userTable.addUser(new UserModel("testuser", "oldhash", "test@example.com"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testuser");
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("EditProfile", controller.editDetailsPage(session, model));
        assertEquals("testuser", model.get("username"));
        assertEquals("test@example.com", model.get("email"));
    }

    @Test
    void updateProfileRejectsShortUsername() throws SQLException {
        userTable.addUser(new UserModel("testuser", "oldhash", "test@example.com"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testuser");

        assertEquals(
                "redirect:/editDetails?error=invalidUsername",
                controller.updateProfileDetails(session, "short", "test@example.com", ""));
    }

    @Test
    void updateProfileRejectsInvalidEmail() throws SQLException {
        userTable.addUser(new UserModel("testuser", "oldhash", "test@example.com"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testuser");

        assertEquals(
                "redirect:/editDetails?error=invalidEmail",
                controller.updateProfileDetails(session, "testuser", "invalid-email", ""));
    }

    @Test
    void updateProfileUpdatesEmailAndPasswordAndSession() throws SQLException {
        userTable.addUser(new UserModel("testuser", "oldhash", "test@example.com"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testuser");

        assertEquals(
                "redirect:/profile",
                controller.updateProfileDetails(session, "testuser", "new@example.com", "newpassword"));

        UserModel updated = userTable.getUserByUsername("testuser");
        assertNotNull(updated);
        assertEquals("new@example.com", updated.getUserEmail());
        assertTrue(BCrypt.checkpw("newpassword", updated.getPasswordHash()));
        assertEquals("testuser", session.getAttribute("user"));
    }

    @Test
    void updateProfileChangesUsername() throws SQLException {
        userTable.addUser(new UserModel("testuser", "oldhash", "test@example.com"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testuser");

        assertEquals(
                "redirect:/profile",
                controller.updateProfileDetails(session, "newtestuser", "test@example.com", ""));

        assertNull(userTable.getUserByUsername("testuser"));
        assertNotNull(userTable.getUserByUsername("newtestuser"));
        assertEquals("newtestuser", session.getAttribute("user"));
    }

    @Test
    void updateProfileRejectsTakenUsername() throws SQLException {
        userTable.addUser(new UserModel("testuser", "oldhash", "test@example.com"));
        userTable.addUser(new UserModel("anotheruser", "oldhash", "other@example.com"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testuser");

        assertEquals(
                "redirect:/editDetails?error=usernameTaken",
                controller.updateProfileDetails(session, "anotheruser", "test@example.com", ""));
        assertNotNull(userTable.getUserByUsername("testuser"));
    }

    @Test
    void deleteAccountDeletesUserAndInvalidatesSession() throws SQLException {
        userTable.addUser(new UserModel("testuser", "oldhash", "test@example.com"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testuser");

        assertEquals("redirect:/login?deleted", controller.deleteAccount(session));
        assertNull(userTable.getUserByUsername("testuser"));
        assertTrue(session.isInvalid());
    }

    @Test
    void logoutInvalidatesSessionAndRedirects() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testuser");

        assertEquals("redirect:/login?logout", controller.logout(session));
        assertTrue(session.isInvalid());
    }
}
