package Find.read.Read.repository;




import Find.read.Read.models.Page;
import Find.read.Read.models.PageNumberOnly;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PageRepository extends MongoRepository<Page, String> {
    List<Page> findByNovelId(String novelId);
    int countByNovelId(String novelId);
    Optional<Page> findByNovelIdAndPageNumber(String novelId, int pageNumber);
    @Query(value = "{ 'novelId': ?0 }", fields = "{ 'pageNumber' : 1, '_id' : 0 }")
    List<PageNumberOnly> findPageNumbersByNovelId(String novelId);
    List<Page> findByNovelIdOrderByPageNumberDesc(String novelId);
    List<Page> findByNovelIdOrderByPageNumberAsc(String novelId);


}
