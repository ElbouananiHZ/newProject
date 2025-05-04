package Find.read.Read.repository;




import Find.read.Read.models.Novel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NovelRepository extends MongoRepository<Novel, String> {Novel findByName(String name);
}
