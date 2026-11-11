package com.example;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final UserAuth userAuth;

    public AuthController (UserAuth userAuth){
        this.userAuth = userAuth;
    }

    @PostMapping ("/signup")
    public String signup(@RequestParam String username, @RequestParam String password){
        if (userAuth.SignUp(username, password)){
            return "redirect:/login.html";
        }
        return "redirect:/CreateAccount.html";
    }

    @PostMapping ("/login")
    public String login(@RequestParam String username, @RequestParam String password, @RequestParam HttpSession session){
        if (userAuth.authorizeUser(username, password)){
            session.setAttribute("user", username.trim().toLowerCase());
            return "redirect:/dashboard.html";
        }
        return "redirect:/login.html";
    }

}
