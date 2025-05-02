package Find.read.Read.repository;




import Find.read.Read.models.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PageRepository extends MongoRepository<Page, String> {
    List<Page> findByNovelId(String novelId);
}
