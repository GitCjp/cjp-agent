package com.example.cjpagent;

import com.example.cjpagent.rag.LoveAppDocumentLoader;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@SpringBootTest
class LoveAppDocumentLoaderTest {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Test
    void testLoadDocuments() {
        var documents = loveAppDocumentLoader.loadDocuments();
        assertNotNull(documents);
        assertFalse(documents.isEmpty(), "Loaded documents should not be empty");
        log.info("Loaded {} documents", documents.size());
    }
}