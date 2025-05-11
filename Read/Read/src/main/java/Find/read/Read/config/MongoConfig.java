package Find.read.Read.config;

import Find.read.Read.models.Novel;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;



    @Configuration
    public class MongoConfig {

        @Autowired
        private MongoTemplate mongoTemplate;

        @PostConstruct
        public void initIndexes() {
            // Print existing indexes for debugging
            mongoTemplate.indexOps(Novel.class).getIndexInfo().forEach(index -> {
                System.out.println("Existing index: " + index);
            });

            // Create text index
            mongoTemplate.indexOps(Novel.class).ensureIndex(
                    new TextIndexDefinition.TextIndexDefinitionBuilder()
                            .onField("name") // Higher weight for name
                            .onField("category")
                            .onField("description")
                            .build()
            );
        }
    }
