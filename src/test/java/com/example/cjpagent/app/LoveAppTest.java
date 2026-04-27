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
        String  response = loveApp.doChatWithRagWithReWrite(message, chatId);
        Assertions.assertNotNull(response);
    }

    @Test
    void doChatWithRagAndCloud() {
        String chatId = UUID.randomUUID().toString();
        String message = "怎样维护婚后夫妻间的亲密关系？";
        String  response = loveApp.doChatWithRagAndCloud(message, chatId);
        Assertions.assertNotNull(response);
    }

    @Test
    void doChatWithMultipleTools() {

        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");


        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");


        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");


        testMessage("执行 Python3 脚本来生成数据分析报告");


        testMessage("保存我的恋爱档案为文件");


        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithMultipleTools(message, chatId);
        Assertions.assertNotNull(answer);
    }



    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();

        String message = "我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点";
        String answer =  loveApp.doChatWithMcp(message, chatId);
    }

}