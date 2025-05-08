package Find.read.Read.service;

import Find.read.Read.models.Novel;
import Find.read.Read.models.Page;
import Find.read.Read.repository.NovelRepository;
import Find.read.Read.repository.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NovelService {

    private final NovelRepository novelRepository;
    private final PageRepository pageRepository;

    @Autowired
    public NovelService(NovelRepository novelRepository, PageRepository pageRepository) {
        this.novelRepository = novelRepository;
        this.pageRepository = pageRepository;
    }

    public Optional<Novel> getNovelById(String id) {
        return novelRepository.findById(id);
    }

    public List<Novel> getAllNovels() {
        return novelRepository.findAll();
    }

    public void saveNovel(Novel novel) {
        novelRepository.save(novel);
    }

    public void deleteNovel(String id) {
        novelRepository.deleteById(id);
    }

    public void rateNovel(String novelId, String userId, int score) {
        // Logic for saving user rating for the novel
        Optional<Novel> novel = novelRepository.findById(novelId);
        novel.ifPresent(n -> {
            // Save rating logic here
        });
    }

    public void addComment(String novelId, String userId, String content) {
        // Logic for adding a comment for the novel
        Optional<Novel> novel = novelRepository.findById(novelId);
        novel.ifPresent(n -> {
            // Save comment logic here
        });
    }

    public void savePage(Page page) {
        pageRepository.save(page);
    }

    public void updatePageContent(String novelId, int pageNumber, String content) {
        Optional<Page> page = pageRepository.findByNovelIdAndPageNumber(novelId, pageNumber);
        page.ifPresent(p -> {
            p.setContent(content);
            pageRepository.save(p);
        });
    }

    public void deletePageAndRenumber(String novelId, int pageNumber) {
        System.out.println("Attempting to delete page " + pageNumber + " from novel " + novelId);

        Optional<Page> pageToDelete = pageRepository.findByNovelIdAndPageNumber(novelId, pageNumber);
        if (pageToDelete.isPresent()) {
            System.out.println("Page found, deleting...");
            pageRepository.delete(pageToDelete.get());

            List<Page> pages = pageRepository.findByNovelId(novelId);
            System.out.println("Found " + pages.size() + " remaining pages to renumber");

            for (Page page : pages) {
                if (page.getPageNumber() > pageNumber) {
                    System.out.println("Renumbering page " + page.getPageNumber() + " to " + (page.getPageNumber() - 1));
                    page.setPageNumber(page.getPageNumber() - 1);
                    pageRepository.save(page);
                }
            }
        } else {
            System.out.println("Page not found for deletion");
        }
    }
    public List<Novel> getNovelsByAuthorId(String authorId) {
        return novelRepository.findByAuthorId(authorId);
    }
    // In NovelService.java
    @Transactional
    public void removePageFromNovel(String novelId, int pageNumber) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found"));

        System.out.println("Before removal: " + novel.getPages().size() + " pages");

        // Remove page reference
        novel.getPages().removeIf(p -> p.getPageNumber() == pageNumber);

        System.out.println("After removal: " + novel.getPages().size() + " pages");
        novelRepository.save(novel);
    }

}
