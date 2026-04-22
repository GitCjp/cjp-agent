package com.example.cjpagent.controller;

import com.example.cjpagent.app.LoveApp;
import com.example.cjpagent.app.LoveApp.LoveReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {

    private final LoveApp loveApp;

    @Autowired
    public ChatController(LoveApp loveApp) {
        this.loveApp = loveApp;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        try {
            String chatId = request.getChatId();
            String message = request.getMessage();

            if (chatId == null || chatId.trim().isEmpty()) {
                chatId = UUID.randomUUID().toString();
            }

            String response = loveApp.doChat(message, chatId);

            ChatResponse chatResponse = new ChatResponse(response, chatId);

            log.info("Chat response: {}", response);
            return ResponseEntity.ok(chatResponse);

        } catch (Exception e) {
            log.error("Error in chat endpoint", e);
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/chat/report")
    public ResponseEntity<?> generateReport(@RequestBody ChatRequest request) {
        try {
            String chatId = request.getChatId();
            String message = request.getMessage();

            if (chatId == null || chatId.trim().isEmpty()) {
                chatId = UUID.randomUUID().toString();
            }

            LoveReport report = loveApp.doChatWithReport(message, chatId);

            ReportResponse reportResponse = new ReportResponse(
                report.title(),
                report.possibleCauses()
            );

            log.info("Generated report: {}", report.title());
            return ResponseEntity.ok(reportResponse);

        } catch (Exception e) {
            log.error("Error generating report", e);
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Error generating report: " + e.getMessage()));
        }
    }

    @PostMapping("/chat/rag")
    public ResponseEntity<?> chatWithRag(@RequestBody ChatRequest request) {
        try {
            String chatId = request.getChatId();
            String message = request.getMessage();

            if (chatId == null || chatId.trim().isEmpty()) {
                chatId = UUID.randomUUID().toString();
            }

            String response = loveApp.doChatWithRag(message, chatId);

            ChatResponse chatResponse = new ChatResponse(response, chatId);

            log.info("RAG chat response: {}", response);
            return ResponseEntity.ok(chatResponse);

        } catch (Exception e) {
            log.error("Error in RAG chat endpoint", e);
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/chat/rag/cloud")
    public ResponseEntity<?> chatWithRagAndCloud(@RequestBody ChatRequest request) {
        try {
            String chatId = request.getChatId();
            String message = request.getMessage();

            if (chatId == null || chatId.trim().isEmpty()) {
                chatId = UUID.randomUUID().toString();
            }

            String response = loveApp.doChatWithRagAndCloud(message, chatId);

            ChatResponse chatResponse = new ChatResponse(response, chatId);

            log.info("Cloud RAG chat response: {}", response);
            return ResponseEntity.ok(chatResponse);

        } catch (Exception e) {
            log.error("Error in cloud RAG chat endpoint", e);
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    // Request DTOs
    public static class ChatRequest {
        private String message;
        private String chatId;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }
    }

    // Response DTOs
    public static class ChatResponse {
        private String content;
        private String chatId;

        public ChatResponse(String content, String chatId) {
            this.content = content;
            this.chatId = chatId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }
    }

    public static class ReportResponse {
        private String title;
        private List<String> possibleCauses;

        public ReportResponse(String title, List<String> possibleCauses) {
            this.title = title;
            this.possibleCauses = possibleCauses;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getPossibleCauses() {
            return possibleCauses;
        }

        public void setPossibleCauses(List<String> possibleCauses) {
            this.possibleCauses = possibleCauses;
        }
    }

    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
