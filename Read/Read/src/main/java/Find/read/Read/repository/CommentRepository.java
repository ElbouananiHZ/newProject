package Find.read.Read.repository;

import Find.read.Read.models.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByNovelId(String novelId);

}

