package com.example.cjpagent.controller;

import com.example.cjpagent.app.LoveApp;
import com.example.cjpagent.app.LoveApp.LoveReport;
import com.example.cjpagent.service.ChatHistoryService;
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
/**
 * 聊天业务 Controller：
 * 1. 提供基础聊天、结构化报告、RAG 聊天接口
 * 2. 提供会话历史列表和会话消息查询接口
 */
public class ChatController {

    private final LoveApp loveApp;
    private final ChatHistoryService chatHistoryService;

    @Autowired
    public ChatController(LoveApp loveApp, ChatHistoryService chatHistoryService) {
        this.loveApp = loveApp;
        this.chatHistoryService = chatHistoryService;
    }

    /**
     * 基础聊天接口（无 RAG）。
     */
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

    /**
     * 结构化输出接口：返回恋爱报告（标题 + 可能原因）。
     */
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

    /**
     * 本地向量库 RAG 聊天接口。
     */
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

    /**
     * 云端 RAG 聊天接口。
     */
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

    /**
     * 查询历史会话列表。
     * @param prefix 会话前缀过滤（可选）
     */
    @GetMapping("/chat/history/sessions")
    public ResponseEntity<?> listSessions(@RequestParam(required = false) String prefix) {
        try {
            return ResponseEntity.ok(chatHistoryService.listSessions(prefix));
        } catch (Exception e) {
            log.error("Error listing chat sessions", e);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    /**
     * 根据 chatId 查询该会话的全部消息。
     */
    @GetMapping("/chat/history/{chatId}/messages")
    public ResponseEntity<?> getSessionMessages(@PathVariable String chatId) {
        try {
            return ResponseEntity.ok(chatHistoryService.getSessionMessages(chatId));
        } catch (Exception e) {
            log.error("Error fetching chat session messages", e);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    /**
     * PostgreSQL 向量库 RAG 聊天接口。
     */
    @PostMapping("/chat/rag/pg")
    public ResponseEntity<?> chatWithRagAndPg(@RequestBody ChatRequest request) {
        try {
            String chatId = request.getChatId();
            String message = request.getMessage();

            if (chatId == null || chatId.trim().isEmpty()) {
                chatId = UUID.randomUUID().toString();
            }

            String response = loveApp.doChatWithRagAndPGSQL(message, chatId);

            ChatResponse chatResponse = new ChatResponse(response, chatId);

            log.info("PG RAG chat response: {}", response);
            return ResponseEntity.ok(chatResponse);

        } catch (Exception e) {
            log.error("Error in PG RAG chat endpoint", e);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    /**
     * 通用聊天请求：
     * message 为本次输入；chatId 为空时后端自动生成会话 ID。
     */
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

    /**
     * 通用聊天响应：
     * content 为模型回复；chatId 为本次会话 ID（前端需保存用于续聊）。
     */
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

    /**
     * 结构化报告响应。
     */
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

    /**
     * 通用错误响应。
     */
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
