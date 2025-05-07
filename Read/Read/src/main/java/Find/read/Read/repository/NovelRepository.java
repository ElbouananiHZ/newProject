package Find.read.Read.repository;

import Find.read.Read.models.Novel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NovelRepository extends MongoRepository<Novel, String> {
    Optional<Novel> findById(String id);
    List<Novel> findByNameContainingIgnoreCase(String name);
    List<Novel> findByCategory(String category);
    List<Novel> findByTagsIn(List<String> tags);
}
