package com.example;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

public class ProfileController {
    // Get entered username, email and password from the user 
    @GetMapping("/profile")
    public String getProfile() {
        // Implementation for profile-related endpoints
        return "Profile data";
    }

    @PostMapping("/editDetails")
    public String updateProfile() {
        return "redirect:EditProfile.html";
    }

    @PostMapping("/updateProfile")
    public String updateProfileDetails() {
        // Implementation for updating user profile details
        // Implementation for updating user profile information
        // This could include updating the username, email, or password
        return "Profile updated";
    }

    // Logout user profile and redirect to logout page
    @GetMapping("/logout")
    public String logout() {
        // Implementation for logout functionality
        // Redirect the user to the logout page after logging out
        return "redirect:Logout.html";
    }

    // Delete user profile
    @DeleteMapping("/profile")
    public String deleteProfile() {
        // Implementation for deleting user profile
        return "Profile deleted";
    }
}
