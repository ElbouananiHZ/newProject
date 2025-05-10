package Find.read.Read.controller;


import Find.read.Read.models.Novel;
import Find.read.Read.repository.NovelRepository;
import Find.read.Read.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Set;

@Controller
public class LibraryController {

    @Autowired
    private UserService userService;

    @Autowired
    private NovelRepository novelRepository;

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userId") != null;
    }


    @PostMapping("/library/add/{novelId}")
    public String addToLibrary(@PathVariable String novelId, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        userService.addNovelToLibrary(userId, novelId);
        return "redirect:/novels/detail/" + novelId;
    }

    @PostMapping("/library/remove/{novelId}")
    public String removeFromLibrary(@PathVariable String novelId, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        userService.removeNovelFromLibrary(userId, novelId);
        return "redirect:/novels/detail/" + novelId;
    }
    @GetMapping("/library")
    public String showLibrary(Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }

        String userId = (String) session.getAttribute("userId");
        Set<String> libraryNovelIds = userService.getUserLibrary(userId);
        List<Novel> libraryNovels = novelRepository.findAllById(libraryNovelIds);

        model.addAttribute("novels", libraryNovels);
        return "library";
    }


}
