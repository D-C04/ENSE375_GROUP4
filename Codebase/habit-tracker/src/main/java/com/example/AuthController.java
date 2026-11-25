package com.example;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserAuth userAuth;

    public AuthController(UserAuth userAuth) {
        this.userAuth = userAuth;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    // ---------- Login ----------

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";      // already logged in
        }
        return "login";                        // templates/login.html
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        if (userAuth.authorizeUser(username, password)) {
            session.setAttribute("user", username.trim().toLowerCase());
            return "redirect:/dashboard";
        }
        return "redirect:/login?error";
    }

    // ---------- Sign up ----------

    @GetMapping("/signup")
    public String signupPage() {
        return "CreateAccount";                // templates/CreateAccount.html
    }

@PostMapping("/signup")
public String signup(@RequestParam String username,
                     @RequestParam String email,
                     @RequestParam String password) {
    if (userAuth.signUp(username, email, password)) {
        return "redirect:/login?registered";
    }
    return "redirect:/signup?error";
}

    // ---------- Dashboard (protected) ----------

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String user = (String) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";          // not logged in
        }
        model.addAttribute("username", user);
        return "dashboard";                    // templates/dashboard.html
    }

    // ---------- Logout ----------

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
