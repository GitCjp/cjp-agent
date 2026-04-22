package com.example.cjpagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;


import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;

    private static final Kryo kryo = new Kryo();

    static {
        //设置不需要求注册类，允许序列化任意类型的对象
        kryo.setRegistrationRequired(false);
        //设置实例化
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }
    //构造函数，接受一个参数baseDir，表示存储聊天记录的基础目录
    public FileBasedChatMemory(String baseDir) {
        this.BASE_DIR = baseDir;
        File baseDirFile = new File(baseDir);
        if (!baseDirFile.exists()) {
            baseDirFile.mkdirs();
        }
    }

    @Override
    public synchronized void add(String conversationId, Message message) {
        List<Message> existingMessages = getOrCreateConversationFile(conversationId);
        existingMessages.add(message);
        saveConversation(conversationId, existingMessages);
    }

    @Override
    public synchronized void add(String conversationId, List<Message> messages) {
        List<Message> existingMessages = getOrCreateConversationFile(conversationId);
        existingMessages.addAll(messages);
        saveConversation(conversationId, existingMessages);
    }

    @Override
    public synchronized List<Message> get(String conversationId, int lastN) {
       List<Message> messages = getOrCreateConversationFile(conversationId);
       if (messages.size() <= lastN) {
           return messages;
       } else {
           return messages.subList(messages.size() - lastN, messages.size());
       }
    }

    @Override
    public synchronized void clear(String conversationId) {
        File conversationFile = getConversationFile(conversationId);
        if (conversationFile.exists()) {
            conversationFile.delete();
        }
    }

    public synchronized List<String> listConversationIds() {
        File baseDirFile = new File(BASE_DIR);
        File[] files = baseDirFile.listFiles((dir, name) -> name.endsWith(".kryo"));
        if (files == null || files.length == 0) {
            return List.of();
        }
        return Arrays.stream(files)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .map(file -> file.getName().replaceFirst("\\.kryo$", ""))
                .collect(Collectors.toList());
    }

    public synchronized long getConversationLastModified(String conversationId) {
        File conversationFile = getConversationFile(conversationId);
        return conversationFile.exists() ? conversationFile.lastModified() : 0L;
    }

    public synchronized List<Message> getAll(String conversationId) {
        return getOrCreateConversationFile(conversationId);
    }

    /**
     * 将会话消息列表保存到文件中
     * @param conversationId
     * @param messages
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        File conversationFile = getConversationFile(conversationId);
        File tmpFile = new File(conversationFile.getAbsolutePath() + ".tmp");
        try {
            System.out.println("[FileBasedChatMemory] saveConversation: conversationId=" + conversationId + ", messages size=" + messages.size());
            Output output = new Output(new FileOutputStream(tmpFile));
            kryo.writeObject(output, messages);
            output.close();
            // 写入成功后原子性替换正式文件
            if (conversationFile.exists()) {
                conversationFile.delete();
            }
            tmpFile.renameTo(conversationFile);
        } catch (Exception e) {
            if (tmpFile.exists()) tmpFile.delete();
            throw new RuntimeException("Failed to save conversation: " + conversationId, e);
        }
    }

    /**
     * 获取或创建会话消息列表
     * @param conversationId
     * @return
     */
    private List<Message> getOrCreateConversationFile(String conversationId) {
        File conversationFile = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (conversationFile.exists()) {
            try (Input input = new Input(new FileInputStream(conversationFile))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (Exception e) {
                // 如果文件损坏或读取失败，删除损坏文件并返回空列表，防止死循环和异常
                conversationFile.delete();
                messages = new ArrayList<>();
            }
        }
        return messages;
    }

    /**
     * 每个回话文件单独保存
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }
}
