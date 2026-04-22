package com.example.cjpagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        //1
        String message = "我最近单身了，感觉很孤独，不知道该怎么扩大社交圈。";
        String response = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(response);
        //2
        assertNotNull(response);
        message = "我和男朋友沟通总是有问题，感觉我们之间有很多矛盾。";
        response = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(response);
    }

    @Test
    void doChatWithEmptyMessage() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好,我是陈建平，我想让我对象更爱我";
        LoveApp.LoveReport response = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(response);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "怎样维护婚后夫妻间的亲密关系？";
        String  response = loveApp.doChatWithRagAndPGSQL(message, chatId);
        Assertions.assertNotNull(response);
    }

    @Test
    void doChatWithRagAndCloud() {
        String chatId = UUID.randomUUID().toString();
        String message = "怎样维护婚后夫妻间的亲密关系？";
        String  response = loveApp.doChatWithRagAndCloud(message, chatId);
        Assertions.assertNotNull(response);
    }
}