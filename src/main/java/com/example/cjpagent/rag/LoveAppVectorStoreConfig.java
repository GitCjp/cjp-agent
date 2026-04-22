package com.example.cjpagent.rag;



import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 本地AI向量存储配置类，负责配置和管理恋爱大师应用的向量存储相关设置。
 */
@Configuration
public class LoveAppVectorStoreConfig {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel databaseEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(databaseEmbeddingModel).build();
        // 加载文档并添加到向量存储中
        simpleVectorStore.add(loveAppDocumentLoader.loadDocuments());
        return simpleVectorStore;
    }
}
