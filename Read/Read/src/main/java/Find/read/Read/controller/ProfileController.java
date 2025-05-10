package Find.read.Read.controller;

import Find.read.Read.models.User;
import Find.read.Read.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User user = userService.findUserById(userId);
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("lastUsernameChangeDate", user.getLastUsernameChangeDate());
        model.addAttribute("hasProfilePicture", user.getProfilePicture() != null);

        // Calculate days remaining until next allowed change
        if (user.getLastUsernameChangeDate() != null) {
            long daysSinceLastChange = ChronoUnit.DAYS.between(
                    user.getLastUsernameChangeDate(),
                    LocalDate.now()
            );
            model.addAttribute("daysRemaining", 30 - daysSinceLastChange);
        } else {
            model.addAttribute("daysRemaining", 0);
        }

        return "profile";
    }

    @GetMapping("/profile/picture")
    @ResponseBody
    public byte[] getProfilePicture(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return null;
        }

        User user = userService.findUserById(userId);
        return user != null ? user.getProfilePicture() : null;
    }

    @PostMapping("/profile/update-username")
    public String updateUsername(
            @RequestParam String newUsername,
            HttpSession session,
            Model model) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User user = userService.findUserById(userId);
        if (user == null) {
            return "redirect:/auth/login";
        }

        // Check if 30 days have passed since last change
        if (user.getLastUsernameChangeDate() != null) {
            long daysSinceLastChange = ChronoUnit.DAYS.between(
                    user.getLastUsernameChangeDate(),
                    LocalDate.now()
            );

            if (daysSinceLastChange < 30) {
                model.addAttribute("error",
                        "Vous ne pouvez changer votre nom d'utilisateur que tous les 30 jours. " +
                                "Prochain changement possible dans " + (30 - daysSinceLastChange) + " jours.");
                return showProfile(session, model);
            }
        }

        // Check if username is available
        if (userService.findUserByUsername(newUsername) != null) {
            model.addAttribute("error", "Ce nom d'utilisateur est déjà pris");
            return showProfile(session, model);
        }

        // Update username
        user.setUsername(newUsername);
        user.setLastUsernameChangeDate(LocalDate.now());
        userService.updateUser(user);

        // Update session
        session.setAttribute("username", newUsername);

        model.addAttribute("success", "Nom d'utilisateur mis à jour avec succès");
        return showProfile(session, model);
    }

    @PostMapping("/profile/update-picture")
    public String updateProfilePicture(
            @RequestParam("profilePicture") MultipartFile file,
            HttpSession session,
            Model model) throws IOException {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User user = userService.findUserById(userId);
        if (user == null) {
            return "redirect:/auth/login";
        }

        // Validate file
        if (file.isEmpty()) {
            model.addAttribute("error", "Veuillez sélectionner une image");
            return showProfile(session, model);
        }

        if (file.getSize() > 2 * 1024 * 1024) { // 2MB limit
            model.addAttribute("error", "L'image ne doit pas dépasser 2MB");
            return showProfile(session, model);
        }

        // Update profile picture
        user.setProfilePicture(file.getBytes());
        userService.updateUser(user);

        model.addAttribute("success", "Photo de profil mise à jour avec succès");
        return showProfile(session, model);
    }

    @PostMapping("/profile/remove-picture")
    public String removeProfilePicture(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User user = userService.findUserById(userId);
        if (user == null) {
            return "redirect:/auth/login";
        }

        // Remove profile picture
        user.setProfilePicture(null);
        userService.updateUser(user);

        model.addAttribute("success", "Photo de profil supprimée avec succès");

        return showProfile(session, model);
    }

}