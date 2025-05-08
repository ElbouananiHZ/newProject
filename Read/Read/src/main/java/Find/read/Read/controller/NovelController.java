package Find.read.Read.controller;

import Find.read.Read.enums.NovelCategory;
import Find.read.Read.enums.NovelTag;
import Find.read.Read.models.Novel;
import Find.read.Read.repository.NovelRepository;
import Find.read.Read.service.NovelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/novels")
public class NovelController {

    private final NovelRepository novelRepository;
    private final NovelService novelService;

    @Autowired
    public NovelController(NovelRepository novelRepository, NovelService novelService) {
        this.novelRepository = novelRepository;
        this.novelService = novelService;
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userId") != null;
    }

    private boolean isOwner(HttpSession session, String novelId) {
        if (!isLoggedIn(session)) {
            return false;
        }
        String userId = (String) session.getAttribute("userId");
        return novelService.getNovelById(novelId)
                .map(novel -> novel.getAuthorId().equals(userId))
                .orElse(false);
    }

    @GetMapping("/unauthorized")
    public String unauthorizedPage() {
        return "unauthorized";
    }

    @GetMapping("/my-novels")
    public String showUserNovels(Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        String userId = (String) session.getAttribute("userId");
        List<Novel> userNovels = novelService.getNovelsByAuthorId(userId);
        model.addAttribute("novels", userNovels);
        return "novel/my-novels"; // New template for user's novels
    }

    @GetMapping("/{id}")
    public String showNovel(@PathVariable String id, Model model) {
        Novel novel = novelService.getNovelById(id)
                .orElseThrow(() -> new RuntimeException("Novel not found"));
        model.addAttribute("novel", novel);
        return "novel/detail";
    }

    @GetMapping
    public String listNovels(Model model) {
        model.addAttribute("novels", novelService.getAllNovels());
        return "novel/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("novel", new Novel());
        model.addAttribute("categories", NovelCategory.values());
        model.addAttribute("tags", NovelTag.values());
        return "novel/create";
    }

    @PostMapping("/save")
    public String saveNovel(@ModelAttribute Novel novel,
                            @RequestParam("imageFile") MultipartFile imageFile,
                            HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        String userId = (String) session.getAttribute("userId");
        novel.setAuthorId(userId);
        novel.setId(UUID.randomUUID().toString());

        if (!imageFile.isEmpty()) {
            try {
                byte[] imageBytes = imageFile.getBytes();
                novel.setImageData(imageBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        novelService.saveNovel(novel);
        return "redirect:/novels/detail/" + novel.getId();
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model, HttpSession session) {
        if (!isOwner(session, id)) {
            return "redirect:/novels/unauthorized"; // Redirect if not the owner
        }

        Novel novel = novelService.getNovelById(id)
                .orElseThrow(() -> new RuntimeException("Novel not found"));
        model.addAttribute("novel", novel);
        model.addAttribute("categories", NovelCategory.values());
        model.addAttribute("tags", NovelTag.values());
        return "novel/edit";
    }

    @PostMapping("/update/{id}")
    public String updateNovel(@PathVariable String id,
                              @ModelAttribute Novel updatedNovel,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              HttpSession session) {
        if (!isOwner(session, id)) {
            return "redirect:/novels/unauthorized"; // Redirect if not the owner
        }

        Novel existingNovel = novelService.getNovelById(id)
                .orElseThrow(() -> new RuntimeException("Novel not found"));

        // Preserve author ID
        updatedNovel.setAuthorId(existingNovel.getAuthorId());
        updatedNovel.setId(id);

        if (!imageFile.isEmpty()) {
            try {
                updatedNovel.setImageData(imageFile.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            updatedNovel.setImageData(existingNovel.getImageData());
        }

        novelService.saveNovel(updatedNovel);
        return "redirect:/novels/detail/" + id;
    }

    @GetMapping("/detail/{id}")
    public String showNovelDetail(@PathVariable String id, Model model) {
        Novel novel = novelService.getNovelById(id)
                .orElseThrow(() -> new RuntimeException("Novel not found"));
        model.addAttribute("novel", novel);
        return "novel/detail";
    }

    @PostMapping("/delete/{id}")
    public String deleteNovel(@PathVariable String id, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        Optional<Novel> novel = novelService.getNovelById(id);
        if (novel.isPresent()) {
            // Ensure the logged-in user is the owner of the novel
            if (isOwner(session, id)) {
                novelService.deleteNovel(id); // Perform the delete operation
                return "redirect:/novels"; // Redirect to the list of novels after deletion
            } else {
                return "redirect:/novels/unauthorized";
            }
        } else {
            throw new RuntimeException("Novel not found");
        }
    }

    @PostMapping("/{id}/rate")
    public String rateNovel(@PathVariable String id, @RequestParam int score, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            novelService.rateNovel(id, userId, score);
        }
        return "redirect:/novels/detail/" + id;
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] serveImage(@PathVariable String id) {
        Novel novel = novelService.getNovelById(id)
                .orElseThrow(() -> new RuntimeException("Novel not found"));

        return novel.getImageData();
    }

    @PostMapping("/{id}/comment")
    public String commentNovel(@PathVariable String id,
                               @RequestParam String content,
                               HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            novelService.addComment(id, userId, content);
        }
        return "redirect:/novels/detail/" + id;
    }
}
