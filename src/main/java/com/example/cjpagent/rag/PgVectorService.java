package com.example.cjpagent.rag;

import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "app", name = "use-remote-pg", havingValue = "true")
public class PgVectorService {

    @Resource(name = "pgVectorVectorStore")
    private VectorStore pgVectorVectorStore;

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private JdbcTemplate jdbcTemplate;

    public int reindex(boolean clearFirst) {
        if (clearFirst) {
            jdbcTemplate.update("DELETE FROM public.vector_store");
        }
        List<Document> documents = loveAppDocumentLoader.loadDocuments();
        if (documents.isEmpty()) {
            return 0;
        }
        pgVectorVectorStore.add(documents);
        return documents.size();
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.vector_store", Long.class);
        return count == null ? 0 : count;
    }

    public List<Document> search(String query, int topK, double similarityThreshold) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();

        List<Document> docs = pgVectorVectorStore.similaritySearch(request);
        if (docs == null) {
            return Collections.emptyList();
        }
        return docs;
    }

    public Map<String, Object> stats() {
        return Map.of(
                "table", "public.vector_store",
                "count", count()
        );
    }
}
