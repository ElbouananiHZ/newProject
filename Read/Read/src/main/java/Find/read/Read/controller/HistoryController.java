// HistoryController.java
package Find.read.Read.controller;

import Find.read.Read.service.NovelService;
import Find.read.Read.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HistoryController {

    private final UserService userService;
    private final NovelService novelService;

    public HistoryController(UserService userService, NovelService novelService) {
        this.userService = userService;
        this.novelService = novelService;
    }

    @GetMapping("/history")
    public String showHistory(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        List<String> novelIds = userService.getUserHistory(userId);
        model.addAttribute("novels", novelService.getNovelsByIds(novelIds));

        return "history";
    }
}