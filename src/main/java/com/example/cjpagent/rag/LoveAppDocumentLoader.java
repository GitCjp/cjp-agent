package com.example.cjpagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import org.springframework.ai.document.Document;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
/**
 * 恋爱大师应用文档加载器，负责加载恋爱大师应用的相关文档和资料，以供应用使用。
 * 该类可以实现从文件系统、数据库或远程服务器等不同来源加载
 */
public class LoveAppDocumentLoader {
    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载文档的方法，返回一个包含所有加载文档的列表。该方法可以根据实际需求进行扩展，例如添加过滤条件、支持不同格式的文档等。
     * @return
     */
    public List<Document> loadDocuments() {
        List<Document> allDocuments = new ArrayList<>();
        // 加载文档
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (var resource : resources) {
                String filename = resource.getFilename();
                log.info("Loaded document: {}", filename);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        } catch (Exception e) {
            log.error("Failed to load documents", e);
        }
        return allDocuments;
    }
}
