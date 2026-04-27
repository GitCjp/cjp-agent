package com.example.cjpagent.controller;

import com.example.cjpagent.rag.PgVectorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pgvector")
@CrossOrigin(origins = "http://localhost:3000")
@ConditionalOnProperty(prefix = "app", name = "use-remote-pg", havingValue = "true")
/**
 * PG 向量库管理接口：
 * 1. 查看向量库统计
 * 2. 重建向量索引
 * 3. 执行相似度检索
 */
public class PgVectorController {

    @Resource
    private PgVectorService pgVectorService;

    /**
     * 查询向量库统计信息（当前记录数等）。
     */
    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        try {
            return ResponseEntity.ok(pgVectorService.stats());
        } catch (Exception e) {
            log.error("Failed to query pgvector stats", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 重建向量索引：
     * clearFirst=true 时先清空再重建。
     */
    @PostMapping("/reindex")
    public ResponseEntity<?> reindex(@RequestBody(required = false) ReindexRequest request) {
        try {
            boolean clearFirst = request == null || request.getClearFirst() == null || request.getClearFirst();
            int added = pgVectorService.reindex(clearFirst);
            long count = pgVectorService.count();
            return ResponseEntity.ok(Map.of(
                    "added", added,
                    "count", count,
                    "clearFirst", clearFirst
            ));
        } catch (Exception e) {
            log.error("Failed to reindex pgvector", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 相似检索接口：
     * 根据 query 从 pgvector 中检索最相关文档片段。
     */
    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {
        try {
            if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "query 不能为空"));
            }

            int topK = request.getTopK() == null ? 4 : request.getTopK();
            double similarityThreshold = request.getSimilarityThreshold() == null ? 0.5 : request.getSimilarityThreshold();

            List<Document> docs = pgVectorService.search(request.getQuery(), topK, similarityThreshold);
            List<Map<String, Object>> result = docs.stream().map(doc -> Map.of(
                    "id", doc.getId() == null ? "" : doc.getId(),
                    "text", doc.getText() == null ? "" : doc.getText(),
                    "metadata", doc.getMetadata() == null ? Map.of() : doc.getMetadata()
            )).toList();

            return ResponseEntity.ok(Map.of(
                    "query", request.getQuery(),
                    "topK", topK,
                    "similarityThreshold", similarityThreshold,
                    "size", result.size(),
                    "records", result
            ));
        } catch (Exception e) {
            log.error("Failed to search pgvector", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 重建索引请求体。
     */
    public static class ReindexRequest {
        private Boolean clearFirst;

        public Boolean getClearFirst() {
            return clearFirst;
        }

        public void setClearFirst(Boolean clearFirst) {
            this.clearFirst = clearFirst;
        }
    }

    /**
     * 相似检索请求体。
     */
    public static class SearchRequest {
        private String query;
        private Integer topK;
        private Double similarityThreshold;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Integer getTopK() {
            return topK;
        }

        public void setTopK(Integer topK) {
            this.topK = topK;
        }

        public Double getSimilarityThreshold() {
            return similarityThreshold;
        }

        public void setSimilarityThreshold(Double similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
        }
    }
}
