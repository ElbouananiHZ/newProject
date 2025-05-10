package Find.read.Read.controller;

import Find.read.Read.models.User;
import Find.read.Read.security.JwtUtil;
import Find.read.Read.service.AuthService;
import Find.read.Read.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;


@Controller
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService; // A service that interacts with the database


    @Autowired
    private AuthService authService;

    // ... existing login methods ...

    @GetMapping("/auth/signup")
    public String showSignupPage() {
        return "signup";  // Returns the signup.html page
    }

    @PostMapping("/auth/signup")
    public String signup(@RequestParam("username") String username,
                         @RequestParam("email") String email,
                         @RequestParam("password") String password,
                         HttpServletResponse response,
                         Model model) {

        // Check if username or email already exists
        if (userService.findUserByUsername(username) != null) {
            model.addAttribute("error", "Username already exists");
            return "signup";
        }

        if (userService.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exists");
            return "signup";
        }

        // Create new user
        User newUser = new User(username, email, password, "VISITOR");

        try {
            // Register the user (this will hash the password)
            authService.registerUser(newUser);

            // Automatically log in the new user
            String token = jwtUtil.generateToken(newUser.getUsername(), newUser.getRole());

            // Set the JWT token in a cookie
            Cookie cookie = new Cookie("jwtToken", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400);  // 1 day
            response.addCookie(cookie);

            return "redirect:/auth/login";  // Redirect to home page after successful signup
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "signup";
        }
    }
    // Handle GET requests for the login page
    @GetMapping("/auth/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/auth/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model,
                        HttpServletResponse response,
                        HttpSession session) {

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty() || !userService.checkPassword(password, userOpt.get().getPassword())) {
            model.addAttribute("error", "Invalid email or password");
            model.addAttribute("isAuthenticated", true);
            return "login";
        }

        User user = userOpt.get();

        // Set session attributes
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // Set JWT in cookie
        Cookie cookie = new Cookie("jwtToken", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 1 day
        response.addCookie(cookie);

        return "redirect:/novels";
    }
    @GetMapping("/auth/logout")
    public String logout(HttpServletResponse response, HttpSession session) {
        session.invalidate();
        Cookie cookie = new Cookie("jwtToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/novels";
    }
}




