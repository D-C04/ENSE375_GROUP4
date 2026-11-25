package com.example;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {
    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final UserTable userTable;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ProfileController(UserTable userTable) {
        this.userTable = userTable;
    }

    @GetMapping("/profile")
    public String getProfile(HttpSession session, Model model) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        try {
            UserModel user = userTable.getUserByUsername(username);
            if (user == null) {
                session.invalidate();
                return "redirect:/login";
            }

            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getUserEmail());
            model.addAttribute("password", "••••••••••••••");
            return "Profile";
        } catch (SQLException exception) {
            log.error("Unable to load profile for {}", username, exception);
            model.addAttribute("error", "Unable to load profile");
            return "Profile";
        }
    }

    // Kept as a small compatibility overload for direct Java callers.
    public String getProfile() {
        return "Profile";
    }

    @GetMapping("/editDetails")
    public String editDetailsPage(HttpSession session, Model model) {
        return loadEditPage(session, model);
    }

    @PostMapping("/editDetails")
    public String editDetails(HttpSession session, Model model) {
        return loadEditPage(session, model);
    }

    private String loadEditPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        try {
            UserModel user = userTable.getUserByUsername(username);
            if (user == null) {
                session.invalidate();
                return "redirect:/login";
            }

            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getUserEmail());
            model.addAttribute("password", "");
            return "EditProfile";
        } catch (SQLException exception) {
            log.error("Unable to load edit profile page for {}", username, exception);
            return "redirect:/profile?error";
        }
    }

    @PostMapping("/updateProfile")
    public String updateProfileDetails(HttpSession session,
                                       @RequestParam(required = false) String username,
                                       @RequestParam(required = false) String email,
                                       @RequestParam(required = false) String password) {
        String oldUsername = (String) session.getAttribute("user");
        if (oldUsername == null) {
            return "redirect:/login";
        }

        String newUsername = (username == null || username.isBlank())
                ? oldUsername
                : username.trim().toLowerCase();
        String newEmail = (email == null || email.isBlank()) ? null : email.trim().toLowerCase();

        if (newUsername.length() < 8) {
            return "redirect:/editDetails?error=invalidUsername";
        }
        if (newEmail != null && !newEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "redirect:/editDetails?error=invalidEmail";
        }
        if (password != null && !password.isBlank() && password.length() < 8) {
            return "redirect:/editDetails?error=invalidPassword";
        }

        try {
            UserModel current = userTable.getUserByUsername(oldUsername);
            if (current == null) {
                session.invalidate();
                return "redirect:/login";
            }
            if (newEmail == null) {
                newEmail = current.getUserEmail();
            }

            if (!newUsername.equals(oldUsername)
                    && (userTable.doesUsernameExist(newUsername)
                    || userTable.updateUsername(oldUsername, newUsername) == 0)) {
                return "redirect:/editDetails?error=usernameTaken";
            }

            if (!newEmail.equalsIgnoreCase(current.getUserEmail())
                    && (userTable.doesEmailExist(newEmail)
                    || userTable.updateEmail(newUsername, newEmail) == 0)) {
                return "redirect:/editDetails?error=emailTaken";
            }

            if (password != null && !password.isBlank()) {
                userTable.updatePasswordHash(newUsername, passwordEncoder.encode(password));
            }

            session.setAttribute("user", newUsername);
            return "redirect:/profile";
        } catch (SQLException exception) {
            log.error("Unable to update profile for {}", oldUsername, exception);
            return "redirect:/editDetails?error=database";
        }
    }

    // Compatibility overload for direct Java callers.
    public String updateProfileDetails() {
        return "Profile updated";
    }

    // Compatibility overload for direct Java callers.
    public String updateProfile() {
        return "redirect:/editDetails";
    }

    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    @GetMapping("/logout")
    public String logoutPage(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    @PostMapping("/deleteAccount")
    public String deleteAccount(HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        try {
            userTable.deleteUser(username);
        } catch (SQLException exception) {
            log.error("Unable to delete profile {}", username, exception);
            return "redirect:/profile?error=database";
        }

        session.invalidate();
        return "redirect:/login?deleted";
    }

    @DeleteMapping("/profile")
    public String deleteProfile(HttpSession session) {
        return deleteAccount(session);
    }
}
