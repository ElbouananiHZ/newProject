package Find.read.Read.service;

import Find.read.Read.models.Comment;
import Find.read.Read.models.Novel;
import Find.read.Read.models.Rating;
import Find.read.Read.repository.NovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NovelService {

    private final NovelRepository novelRepository;

    @Autowired
    public NovelService(NovelRepository novelRepository) {
        this.novelRepository = novelRepository;
    }

    // === CRUD ===

    public List<Novel> getAllNovels() {
        return novelRepository.findAll();
    }

    public Optional<Novel> getNovelById(String id) {
        return novelRepository.findById(id);
    }

    public Novel saveNovel(Novel novel) {
        return novelRepository.save(novel);
    }

    public void deleteNovel(String id) {
        novelRepository.deleteById(id);
    }

    // === Rating ===

    public void rateNovel(String novelId, String userId, int score) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found with ID: " + novelId));

        Rating existing = novel.getRatings().stream()
                .filter(r -> r.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            novel.setTotalRating(novel.getTotalRating() - existing.getScore() + score);
            existing.setScore(score);
        } else {
            Rating rating = new Rating();
            rating.setUserId(userId);
            rating.setScore(score);
            novel.getRatings().add(rating);

            novel.setTotalRating(novel.getTotalRating() + score);
            novel.setRatingCount(novel.getRatingCount() + 1);
        }

        novelRepository.save(novel);
    }

    // === Comments ===

    public void addComment(String novelId, String userId, String content) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found with ID: " + novelId));

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        novel.getComments().add(comment);
        novelRepository.save(novel);
    }
}
