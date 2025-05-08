package Find.read.Read.controller;
import Find.read.Read.security.JwtUtil;
import Find.read.Read.models.User;
import Find.read.Read.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;



        @GetMapping
        public String showProfilePage(Model model, Principal principal) {
            if (principal == null) {
                return "redirect:/auth/login";
            }

            // Changed to findByUsername since JWT uses username
            User user = userService.findUserByUsername(principal.getName());

            if (user == null) {
                return "redirect:/auth/login";
            }

            model.addAttribute("user", user);
            return "profile";
        }


    @PostMapping("/upload-picture")
    public String uploadProfilePicture(@RequestParam("file") MultipartFile file,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Please select a file to upload");
            }

            // Validate image size (max 2MB)
            if (file.getSize() > 2 * 1024 * 1024) {
                throw new RuntimeException("File size must be less than 2MB");
            }

            // Validate content type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Only image files are allowed");
            }

            User user = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setProfilePicture(file.getBytes());
            userService.saveUser(user);

            redirectAttributes.addFlashAttribute("success", "Profile picture updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/update-info")
    public String updateUserInfo(@RequestParam String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(username);
        userService.saveUser(user);

        return "redirect:/profile";
    }
}