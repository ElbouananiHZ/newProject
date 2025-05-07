package Find.read.Read.Controller;

import Find.read.Read.models.Novel;
import Find.read.Read.models.Page;
import Find.read.Read.service.NovelService;
import Find.read.Read.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/novels/{novelId}/pages")
public class PageController {

    private final NovelService novelService;
    private final PageService pageService;

    @Autowired
    public PageController(NovelService novelService, PageService pageService) {
        this.novelService = novelService;
        this.pageService = pageService;
    }

    @GetMapping("/{pageNumber}")
    public String viewPage(@PathVariable String novelId,
                           @PathVariable int pageNumber,
                           Model model) {
        Novel novel = novelService.getNovelById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found"));

        Page currentPage = novel.getPages().stream()
                .filter(p -> p.getPageNumber() == pageNumber)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Page not found"));

        model.addAttribute("novel", novel);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", novel.getPages().size());

        int nextPageNum = pageNumber + 1;
        int prevPageNum = pageNumber - 1;

        Optional<Page> nextPage = novel.getPages().stream()
                .filter(p -> p.getPageNumber() == nextPageNum)
                .findFirst();

        Optional<Page> previousPage = novel.getPages().stream()
                .filter(p -> p.getPageNumber() == prevPageNum)
                .findFirst();

        model.addAttribute("nextPage", nextPage.orElse(null));
        model.addAttribute("previousPage", previousPage.orElse(null));

        return "page-reader";
    }

    @GetMapping("/new")
    public String showAddPageForm(@PathVariable String novelId, Model model) {
        Novel novel = novelService.getNovelById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found"));

        Page page = new Page();
        int nextPageNumber = novel.getPages() != null ? novel.getPages().size() + 1 : 1;
        page.setPageNumber(nextPageNumber);

        model.addAttribute("novelId", novelId);
        model.addAttribute("novelName", novel.getName());
        model.addAttribute("page", page);

        return "add-page";
    }

    @PostMapping("/save")
    public String addPage(@PathVariable String novelId, @ModelAttribute Page page) {
        Novel novel = novelService.getNovelById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found"));

        page.setId(UUID.randomUUID().toString());
        page.setNovelId(novelId);
        page.setNovel(novel);

        if (novel.getPages() == null) {
            novel.setPages(new ArrayList<>());
        }

        novel.getPages().add(page);
        novelService.savePage(page);
        novelService.saveNovel(novel);

        return "redirect:/novels/detail/" + novelId;
    }

    @GetMapping("/{pageNumber}/edit")
    public String showEditPageForm(@PathVariable String novelId,
                                   @PathVariable int pageNumber,
                                   Model model) {
        Novel novel = novelService.getNovelById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found"));

        Page page = pageService.getPageByNovelIdAndPageNumber(novelId, pageNumber)
                .orElseThrow(() -> new RuntimeException("Page not found"));

        model.addAttribute("novelId", novelId);
        model.addAttribute("novelName", novel.getName());
        model.addAttribute("page", page);

        return "edit-page";
    }

    @PostMapping("/{pageNumber}/edit")
    public String editPage(@PathVariable String novelId,
                           @PathVariable int pageNumber,
                           @ModelAttribute Page updatedPage) {
        pageService.updatePageContent(novelId, pageNumber, updatedPage.getContent());
        return "redirect:/novels/" + novelId + "/pages/" + pageNumber + "?success=page_updated";
    }

    @GetMapping("/{pageNumber}/delete")
    public String deletePage(@PathVariable String novelId,
                             @PathVariable int pageNumber) {
        pageService.deletePageAndRenumber(novelId, pageNumber);
        return "redirect:/novels/detail/" + novelId + "?success=page_deleted";
    }

}