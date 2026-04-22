package com.example.cjpagent.service;

import com.example.cjpagent.chatmemory.FileBasedChatMemory;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatHistoryService {

    @Resource
    private FileBasedChatMemory fileBasedChatMemory;

    public List<SessionItem> listSessions(String prefix) {
        List<String> ids = fileBasedChatMemory.listConversationIds();
        List<SessionItem> sessions = new ArrayList<>();

        for (String id : ids) {
            if (prefix != null && !prefix.isBlank() && !id.startsWith(prefix)) {
                continue;
            }
            List<Message> all = fileBasedChatMemory.getAll(id);
            String title = buildTitle(id, all);
            sessions.add(new SessionItem(
                    id,
                    title,
                    fileBasedChatMemory.getConversationLastModified(id),
                    all.size()
            ));
        }
        return sessions;
    }

    public List<MessageItem> getSessionMessages(String chatId) {
        List<Message> all = fileBasedChatMemory.getAll(chatId);
        List<MessageItem> result = new ArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            Message message = all.get(i);
            result.add(new MessageItem(
                    i,
                    resolveRole(message),
                    message.getText() == null ? "" : message.getText()
            ));
        }
        return result;
    }

    private String buildTitle(String chatId, List<Message> messages) {
        for (Message message : messages) {
            if (message instanceof UserMessage) {
                String text = message.getText() == null ? "" : message.getText().trim();
                if (text.isEmpty()) {
                    continue;
                }
                return text.length() > 24 ? text.substring(0, 24) + "..." : text;
            }
        }
        return chatId;
    }

    private String resolveRole(Message message) {
        if (message instanceof UserMessage) {
            return "user";
        }
        if (message instanceof AssistantMessage) {
            return "assistant";
        }
        return "system";
    }

    public record SessionItem(String chatId, String title, long updatedAt, int messageCount) {
    }

    public record MessageItem(int index, String role, String content) {
    }
}
