package com.example.cjpagent.config;

import com.example.cjpagent.chatmemory.FileBasedChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public FileBasedChatMemory fileBasedChatMemory() {
        String fileDir = System.getProperty("user.dir") + "/tmp/chat_memory";
        return new FileBasedChatMemory(fileDir);
    }
}
