package com.example.cjpagent.app;


import com.example.cjpagent.advisor.MyloggerAdvisor;
import com.example.cjpagent.chatmemory.FileBasedChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /**
     * 初始化ChatClient，设置系统提示和聊天记忆顾问。系统提示定义了AI的角色和行为，聊天记忆顾问使用内存存储会话历史，实现连续对话。
     * @param dashscopeChatModel
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        //初始化基于文件的聊天记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat_memory";

        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        //ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义的日志记录顾问，记录每次请求和响应的内容
                        new MyloggerAdvisor()
                        //new ReReadingAdvisor()
                )
                .build();
    }

    record ActorsFilms(String actor, List<String> movies) {}

    /**
     * AI基础聊天接口，输入用户消息和会话ID，返回AI回复内容。会话ID用于关联上下文，实现连续对话。
     * @param message
     * @param chatId
     * @return
     */

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    public record LoveReport(String title, List<String> possibleCauses) {}
    /**
     * Ai 恋爱报告
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport response = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + """
                        你是一个恋爱专家，分析用户的恋爱问题并生成一份恋爱报告。报告包括以下几个部分：
                        1. 问题描述：简要描述用户的恋爱问题。
                        2. 可能原因：分析导致该问题的可能原因，考虑双方的性格、沟通方式、生活习惯等因素。
                        3. 解决建议：针对每个可能原因，提供具体的解决建议和行动步骤。
                        4. 注意事项：提醒用户在处理该问题时需要注意的事项，避免常见的误区。
                        请根据用户输入的问题，生成一份详细的恋爱报告，帮助用户更好地理解和解决他们的恋爱困扰。
                        """)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", response);
        return response;
    }



    @Resource
    private VectorStore loveAppVectorStore;

    /**
     * RAG问答接口---基于本地向量存储的检索增强生成，输入用户消息和会话ID，返回AI回复内容。通过RAG问答顾问，AI在生成回答前会先检索相关文档，增强回答的准确性和丰富性。
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //开启日志记录顾问，记录每次请求和响应的内容
                .advisors(new MyloggerAdvisor())
                //添加RAG问答顾问，使用向量存储进行相关文档检索，增强AI的回答能力
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * RAG问答接口---基于云端RAG服务的检索增强生成
     */
    @Resource
    private Advisor loveAppRagCloudAdvisor;

    public String doChatWithRagAndCloud(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                //设置聊天记忆顾问参数，指定会话ID和检索大小，实现连续对话和上下文关联
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //开启日志记录顾问，记录每次请求和响应的内容
                .advisors(new MyloggerAdvisor())
                //基于云端RAG服务的检索增强生成顾问，调用外部API进行相关文档检索，进一步提升AI的回答质量和覆盖范围
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}