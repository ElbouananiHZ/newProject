package Find.read.Read.Controller;

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
import java.util.UUID;

@Controller
@RequestMapping("/novels")
public class NovelController {

    private final NovelService novelService;

    @Autowired
    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @Autowired
    private NovelRepository novelRepo;

    @GetMapping("/novels/{id}")
    public String showNovel(@PathVariable String id, Model model) {
        Novel novel = novelRepo.findById(id).orElseThrow();
        model.addAttribute("novel", novel);
        return "novel"; // novel.html
    }

    @GetMapping
    public String listNovels(Model model) {
        model.addAttribute("novels", novelService.getAllNovels());
        return "novel/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("novel", new Novel());
        model.addAttribute("categories", NovelCategory.values());
        model.addAttribute("tags", NovelTag.values());
        return "novel/create";
    }

    @PostMapping("/save")
    public String saveNovel(@ModelAttribute Novel novel,
                            @RequestParam("imageFile") MultipartFile imageFile) {
        if (novel.getId() == null || novel.getId().isEmpty()) {
            novel.setId(UUID.randomUUID().toString());
        }

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
    public String showEditForm(@PathVariable String id, Model model) {
        Novel novel = novelService.getNovelById(id)
                .orElseThrow(() -> new RuntimeException("Roman introuvable avec l'ID : " + id));
        model.addAttribute("novel", novel);
        model.addAttribute("categories", NovelCategory.values());
        model.addAttribute("tags", NovelTag.values());
        return "novel/edit";
    }

    @PostMapping("/update/{id}")
    public String updateNovel(@PathVariable String id,
                              @ModelAttribute Novel updatedNovel,
                              @RequestParam("imageFile") MultipartFile imageFile) {

        Novel existingNovel = novelService.getNovelById(id)
                .orElseThrow(() -> new RuntimeException("Roman introuvable avec l'ID : " + id));

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
                .orElseThrow(() -> new RuntimeException("Roman introuvable avec l'ID : " + id));
        model.addAttribute("novel", novel);
        return "novel/detail";
    }

    @GetMapping("/delete/{id}")
    public String deleteNovel(@PathVariable String id) {
        novelService.deleteNovel(id);
        return "redirect:/novels";
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] getImage(@PathVariable String id) {
        return novelService.getNovelById(id)
                .map(Novel::getImageData)
                .orElse(null);
    }

    // ✅ Méthode pour noter un roman
    @PostMapping("/{id}/rate")
    public String rateNovel(@PathVariable String id,
                            @RequestParam int score,
                            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            novelService.rateNovel(id, userId, score);
        }
        return "redirect:/novels/detail/" + id;
    }

    // ✅ Méthode pour commenter un roman
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
