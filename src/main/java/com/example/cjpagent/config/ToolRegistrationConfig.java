package com.example.cjpagent.config;

import com.example.cjpagent.tools.WebSearchTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具注册配置：
 * 统一注册可供大模型调用的工具对象。
 */
@Configuration
public class ToolRegistrationConfig {

    @Value("${search-api.api-key:}")
    private String searchApiKey;

    /**
     * 创建网页搜索工具实例。
     */
    @Bean
    public WebSearchTool webSearchTool() {
        return new WebSearchTool(searchApiKey);
    }

    /**
     * 注册工具回调（供需要 AI 自主工具调用的场景使用）。
     */
    @Bean
    public ToolCallback[] webSearchTools(WebSearchTool webSearchTool) {
        return ToolCallbacks.from(webSearchTool);
    }
}
