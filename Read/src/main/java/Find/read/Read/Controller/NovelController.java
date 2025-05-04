package Find.read.Read.Controller;

import Find.read.Read.enums.NovelCategory;
import Find.read.Read.enums.NovelTag;
import Find.read.Read.models.Novel;
import Find.read.Read.models.Page;
import Find.read.Read.models.PageNumberOnly;
import Find.read.Read.repository.PageRepository;
import Find.read.Read.service.NovelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import Find.read.Read.models.PageNumberOnly;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/novels")
public class NovelController {

    private final NovelService novelService;
    private final PageRepository pageRepository;

    @Autowired
    public NovelController(NovelService novelService, PageRepository pageRepository) {
        this.novelService = novelService;
        this.pageRepository = pageRepository;
    }

    /** ------------------------- CREATE NOVEL ------------------------- **/

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
                novel.setImageData(imageFile.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        novelService.saveNovel(novel);
        return "redirect:/novels/detail/" + novel.getId();
    }

    /** ------------------------- UPDATE NOVEL ------------------------- **/

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

        try {
            updatedNovel.setImageData(!imageFile.isEmpty() ? imageFile.getBytes() : existingNovel.getImageData());
        } catch (IOException e) {
            e.printStackTrace();
        }

        novelService.saveNovel(updatedNovel);
        return "redirect:/novels/detail/" + id;
    }

    /** ------------------------- DELETE NOVEL ------------------------- **/

    @GetMapping("/delete/{id}")
    public String deleteNovel(@PathVariable String id) {
        novelService.deleteNovel(id);
        return "redirect:/novels";
    }

    /** ------------------------- LIST & DETAIL VIEW ------------------------- **/

    @GetMapping
    public String listNovels(Model model) {
        model.addAttribute("novels", novelService.getAllNovels());
        return "novel/list";
    }

    @GetMapping("/detail/{id}")
    public String showNovelDetail(@PathVariable String id, Model model) {
        Novel novel = novelService.getNovelById(id)
                .orElseThrow(() -> new RuntimeException("Novel not found with ID: " + id));

        List<Page> chapters = pageRepository.findByNovelIdOrderByPageNumberDesc(id);

        model.addAttribute("novel", novel);
        model.addAttribute("pageCount", chapters.size());
        model.addAttribute("chapters", chapters);
        model.addAttribute("novelId", id); // Add the novelId to the model

        return "novel/detail";
    }


    /** ------------------------- IMAGE DISPLAY ------------------------- **/

    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] getImage(@PathVariable String id) {
        return novelService.getNovelById(id)
                .map(Novel::getImageData)
                .orElse(null);
    }

    /** ------------------------- PAGE MANAGEMENT ------------------------- **/

    @GetMapping("/{novelId}/pages/new")
    public String showAddPageForm(@PathVariable String novelId, Model model) {
        Page page = new Page();
        page.setNovelId(novelId);
        model.addAttribute("page", page);
        model.addAttribute("novelId", novelId);
        return "page/add";
    }

    /** Sauvegarde de la page liée au roman **/
    @PostMapping("/{novelId}/pages/save")
    public String savePage(@PathVariable String novelId,
                           @ModelAttribute Page page) {
        // l’ID est déjà dans `page.novelId` si tu as bien mis le hidden field
        pageRepository.save(page);
        return "redirect:/novels/detail/" + novelId;
    }


    @PostMapping("/pages/add")
    public String addPage(@RequestParam String novelId,
                          @RequestParam String content,
                          @RequestParam String pageName,
                          Model model) {
        Novel novel = novelService.getNovelById(novelId)
                .orElseThrow(() -> new RuntimeException("Roman introuvable avec l'ID : " + novelId));

        String novelName = novel.getName(); // Or getName(), depending on your model

        int nextPageNumber = pageRepository.findPageNumbersByNovelId(novelId)
                .stream()
                .map(PageNumberOnly::getPageNumber)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        Page page = new Page();
        page.setNovelId(novelId);
        page.setPageNumber(nextPageNumber);
        page.setName(pageName);
        page.setContent(content);

        pageRepository.save(page);

        return "redirect:/novels/detail/" + novelId;
    }



    @PostMapping("/pages/add-by-name")
    public String addPageByName(@RequestParam String novelName,
                                @RequestParam String content,
                                @RequestParam String pageName,
                                Model model) {
        // Get the novel by its name
        Novel novel = novelService.getNovelByName(novelName);

        if (novel == null) {
            model.addAttribute("error", "Novel not found");
            return "page/add-by-name";  // Handle error if novel is not found
        }

        // Find the next page number based on existing pages
        List<Integer> pageNumbers = pageRepository.findPageNumbersByNovelId(novel.getId())
                .stream().map(PageNumberOnly::getPageNumber).toList();

        int nextPageNumber = pageNumbers.isEmpty() ? 1 : pageNumbers.stream().max(Integer::compare).orElse(0) + 1;

        // Create a new Page object and set its properties
        Page page = new Page();
        page.setNovelId(novel.getId());
        page.setPageNumber(nextPageNumber);
        page.setName(pageName);
        page.setContent(content);

        // Save the new page in the repository
        pageRepository.save(page);

        // Redirect to the novel's detail page
        return "redirect:/novels/detail/" + novel.getId();
    }


    @GetMapping("/{id}/pages/{pageNumber}")
    public String readNovelPage(@PathVariable String id,
                                @PathVariable int pageNumber,
                                Model model) {
        Novel novel = novelService.getNovelById(id)
                .orElseThrow(() -> new RuntimeException("Roman introuvable avec l'ID : " + id));

        Page page = pageRepository.findByNovelIdAndPageNumber(id, pageNumber)
                .orElseThrow(() -> new RuntimeException("Page introuvable."));

        List<Integer> pageNumbers = pageRepository.findPageNumbersByNovelId(id)
                .stream().map(PageNumberOnly::getPageNumber).toList();

        model.addAttribute("novel", novel);
        model.addAttribute("page", page);
        model.addAttribute("pageName", page.getName());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("nextPageNumber", pageNumber + 1);
        model.addAttribute("previousPageNumber", pageNumber - 1);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("totalPages", pageNumbers.size());

        return "novel/read";
    }

    /** ------------------------- INTERACTIONS (RATE & COMMENT) ------------------------- **/

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
