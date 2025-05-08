package Find.read.Read.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("message", "Welcome to your Dashboard!");
        return "dashboard"; // Looks for dashboard.html in templates
    }
}
