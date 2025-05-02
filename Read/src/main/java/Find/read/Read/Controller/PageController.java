package Find.read.Read.Controller;

import Find.read.Read.models.Page;
import Find.read.Read.repository.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/novels")
public class PageController {

    @Autowired
    private PageRepository pageRepository; // Inject PageRepository directly

    // Add a page to a novel
    @PostMapping("/{novelId}/add-page")
    @ResponseBody
    public ResponseEntity<?> addPageToNovel(@PathVariable String novelId, @RequestBody Page page) {
        // Check for invalid inputs
        if (novelId == null || novelId.isEmpty()) {
            return ResponseEntity.badRequest().body("Novel ID is required.");
        }

        if (page == null || page.getContent() == null || page.getContent().isEmpty()) {
            return ResponseEntity.badRequest().body("Page content is required.");
        }

        try {
            // Set the reference to the novel
            page.setNovelId(novelId);

            // Save the page directly through the repository
            Page savedPage = pageRepository.save(page); // Save without using PageService

            // Return the saved page with 201 Created status
            return new ResponseEntity<>(savedPage, HttpStatus.CREATED);
        } catch (Exception e) {
            // Handle any error that occurs while saving the page
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving the page: " + e.getMessage());
        }
    }
}
