package Find.read.Read.Controller;

import Find.read.Read.models.Page;
import Find.read.Read.repository.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/novels")
public class PageController {
    @PostMapping("/{novelId}/pages/save")
    public String savePage(@PathVariable String novelId,
                           @ModelAttribute Page page,
                           Model model) {
        page.setNovelId(novelId);
        pageRepository.save(page);
        return "redirect:/novels/detail/" + novelId;
    }


    @Autowired
    private PageRepository pageRepository; // Injection de PageRepository directement

    // Méthode pour afficher le formulaire d'ajout d'une page
    @GetMapping("/pages/add-by-name")
    public String showAddByNameForm(Model model) {
        model.addAttribute("page", new Page());
        return "page/add-by-name"; // Vue avec le formulaire pour ajouter une page
    }

    // Ajouter une page à un roman spécifique
    @PostMapping("/{novelId}/add-page")
    @ResponseBody
    public ResponseEntity<?> addPageToNovel(@PathVariable String novelId, @RequestBody Page page) {
        // Vérifier les entrées invalides
        if (novelId == null || novelId.isEmpty()) {
            return ResponseEntity.badRequest().body("Le ID du roman est requis.");
        }

        if (page == null || page.getContent() == null || page.getContent().isEmpty()) {
            return ResponseEntity.badRequest().body("Le contenu de la page est requis.");
        }

        try {
            // Définir la référence vers le roman
            page.setNovelId(novelId);

            // Sauvegarder la page directement via le repository
            Page savedPage = pageRepository.save(page); // Sauvegarde sans utiliser de service supplémentaire

            // Retourner la page sauvegardée avec un statut HTTP 201 (Created)
            return new ResponseEntity<>(savedPage, HttpStatus.CREATED);
        } catch (Exception e) {
            // Gérer les erreurs pendant l'enregistrement de la page
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'enregistrement de la page : " + e.getMessage());
        }
    }
}
